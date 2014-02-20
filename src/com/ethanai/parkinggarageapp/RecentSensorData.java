package com.ethanai.parkinggarageapp;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
//import android.util.Log;

import android.hardware.SensorManager;
import android.location.Location;

import java.io.Serializable;
import java.text.SimpleDateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Date;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class RecentSensorData implements Serializable { //must specify serializable so it can be passed by our intents neatly
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 5721779411217090251L;
	public int historyLength = 100;
    private final float ACCELEROMETER_NOISE = (float) 0.5;
    private String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss"; //implement this eventually for pretty date recording
    
    public Date initialDate; //date this structure initialized. 
	public ArrayList<AccelerometerReading> accRecent = new ArrayList<AccelerometerReading>();
	public ArrayList<MagnetReading> magnRecent = new ArrayList<MagnetReading>();
	public ArrayList<CompassReading> compassRecent = new ArrayList<CompassReading>();
	public ArrayList<HumidityReading> humidRecent = new ArrayList<HumidityReading>();
	public ArrayList<PressureReading> pressRecent = new ArrayList<PressureReading>();
	public ArrayList<DerivedOrientation> orientRecent = new ArrayList<DerivedOrientation>(); //will be dated with the time the two sensor readings got merged (here)
	
	//headers for each data type:
    public String orientHeader = "Time, location, acc, distance, azimuth, pitch, roll, inclination, turn degrees, quarter turns\n";
    public String accHeader = "Time, location, Xacc, Yacc, Zacc, MagAcc, Xjerk, Yjerk, Zjerk, MagJerk\n";
    public String magnHeader = "Date, location, x, y, z\n";
    public String compassHeader = "Date, location, x, y, z, total, accuracy\n";
	public String pressureHeader = "Date, location, Pressure(millibars)\n";

	//parts needed to be collected before creating a new DerivedOrientation
	private SensorEvent accRecentEvent = null;
	private SensorEvent magnRecentEvent = null;
	
	
	//absolute final result... what floor we parked on!
	public String parkedFloor = "";
	public String parkedDateString ="";

	RecentSensorData() {
		initialDate = new Date();  //date this structure initialized. Caller needs to be careful so this is close to the start of sensor polling   
	}
	
	RecentSensorData(Date manualInitialDate) {
		initialDate = manualInitialDate;
	}
	
	RecentSensorData(int newHistoryLength) {
		historyLength = newHistoryLength;
	}
	
	/*
	 * Call the constructor for whatever sensor type made this, build a sensor reading object
	 * Store object in the appropriate ArrayList
	 */
	public <E> void addUpToLimit(String dateString, Location location, SensorEvent event) {
		Sensor sensor = event.sensor;        
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        	addUpToLimit(accRecent, new AccelerometerReading(dateString, location, event));  
        	
        	//also try to create an orientation data record
        	accRecentEvent = event; //add this sensor event or overwrite stale data
        	if((accRecentEvent != null) && (magnRecentEvent != null)) { //if we not have both parts, build an orientation record and add it
        		addUpToLimit(orientRecent, new DerivedOrientation(new SimpleDateFormat(DATE_FORMAT_STRING).format(new Date()), location));
        		//flag that this most recent accelerometer reading was used for calculating a new orientation reading
        		accRecent.get(accRecent.size() - 1).createdOrientationReading = true; 
        		accRecentEvent = null; // require new readings for both sensors before building another
        		magnRecentEvent = null;
        		
        	}
        		        	
        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
        	addUpToLimit(magnRecent, new MagnetReading(dateString, location, event));
        	
        	//also try to create an orientation data record
        	magnRecentEvent = event; //add this sensor event or overwrite stale data
        	if((accRecentEvent != null) && (magnRecentEvent != null)) { //if we not have both parts, build an orientation record and add it
        		addUpToLimit(orientRecent, new DerivedOrientation(new SimpleDateFormat(DATE_FORMAT_STRING).format(new Date()), location));
        		//flag that this most recent magnet reading was used for calculating a new orientation reading
        		magnRecent.get(magnRecent.size() - 1).createdOrientationReading = true; 
        		accRecentEvent = null; // require new readings for both sensors before building another
        		magnRecentEvent = null;
        		
        	}
        	
        } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
        	addUpToLimit(compassRecent, new CompassReading(dateString, event));
        	
        } else if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
        	addUpToLimit(humidRecent, new HumidityReading(dateString, event));

        } else if (sensor.getType() == Sensor.TYPE_PRESSURE) {
        	addUpToLimit(pressRecent, new PressureReading(dateString, event));

        }		
        
    }	
	
	public <E> void addUpToLimit(ArrayList<E> arrayList, E newEntry) {
        if(arrayList != null && arrayList.size() == historyLength) {
        	arrayList.remove(0);
        }
        arrayList.add(newEntry);
    }	
	
	class MagnetReading {
		
		public String dateString;
		public Location location;
		public String locationString;
		public boolean createdOrientationReading = false;
		public float x;
		public float y;
		public float z;
		
		MagnetReading(String dateString, Location location, SensorEvent event) {
			this.dateString = dateString;
			this.location = location;
			this.locationString = location.getLatitude() + " " + location.getLongitude();
			this.x = event.values[0];
			this.y = event.values[1];
			this.z = event.values[2];
			
		}
		
		public String toFormattedString() {
			return dateString + ", " +
					locationString + ", " +
	                Float.toString(x) + "," + 
	                Float.toString(y) + "," +
	                Float.toString(z) + "," +
	                "\n";
		}
		
	}
	
	// Derived from coursera Class example CompassActivity
		//Also contains turn history. this will evolve to be more than just an angle. Might want to force unlimited length in recording?
		//or have final step be read the full data from the stored file? Break out into a separate object?
	class DerivedOrientation {
		public String dateString;
		public Location location;
		public String locationString;
		public float gpsAccuracy;
		public float distance; //distance from labeled target for debugging
		
		//Values we want:
			//http://developer.android.com/reference/android/hardware/SensorManager.html#getOrientation(float[], float[])
		public double azimuthInDegrees;
		public double pitchInDegrees;
		public double rollInDegrees;
			//http://developer.android.com/reference/android/hardware/SensorManager.html#getInclination(float[])
		public double inclinationInDegrees; 
		
		public double totalTurnDegrees; //naive implementation, will evolve into turn counts. 
		
		DerivedOrientation(String dateString, Location location) {
			this.dateString = dateString;
			this.location = location;
			this.locationString = location.getLatitude() + " " + location.getLongitude();
	    	
			this.gpsAccuracy = location.getAccuracy();
			this.distance = location.distanceTo(UserLocationManager.homeLocation);
			
			// Storage for Sensor readings. Need both present before orientation can be derived
			float[] mGravity = new float[3]; //accelerometer
			float[] mGeomagnetic = new float[3]; //magnet meter
			// additional storage for intermediary steps in calculation
			float rotationMatrix[] = new float[9];
			float inclinationMatrix[] = new float[9];
			float orientationMatrix[] = new float[3];
						
			System.arraycopy(accRecentEvent.values, 0, mGravity, 0, 3);
			System.arraycopy(magnRecentEvent.values, 0, mGeomagnetic, 0, 3);
			
			//merge and calculate the inclination and orientation (finally)
				//feed the sensor events, and function call will fill in the two Matrices
			boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mGravity, mGeomagnetic);
			if (success) {
				SensorManager.getOrientation(rotationMatrix, orientationMatrix); //
				// Convert from radians to degrees and store in our target values
				azimuthInDegrees = Math.toDegrees(orientationMatrix[0]);
				pitchInDegrees = Math.toDegrees(orientationMatrix[1]);
				rollInDegrees = Math.toDegrees(orientationMatrix[2]);
				inclinationInDegrees = Math.toDegrees(SensorManager.getInclination(inclinationMatrix));
				updateTurnHistory(azimuthInDegrees);
				updateCurrentFloor();
			}
		}
		
		public void updateTurnHistory(double azimuthInDegrees) {
			if(orientRecent == null || orientRecent.size() == 0) { //if we're the first record, initialize at zero
				totalTurnDegrees = 0;
			} else {
				totalTurnDegrees = orientRecent.get(orientRecent.size() - 1).totalTurnDegrees; //initialize with previous total
				double previousAzimuth = orientRecent.get(orientRecent.size() - 1).azimuthInDegrees;
				//compensate for possible crossing the South line (-180 to +180)
				if((azimuthInDegrees - previousAzimuth) < -180) {
					azimuthInDegrees += 360;
				} else if ((azimuthInDegrees - previousAzimuth) > 180) {
					azimuthInDegrees -= 360;
				}
				totalTurnDegrees += azimuthInDegrees - previousAzimuth;
								
				/*
				 * case 1. go from 0 to -10. now - old = -10 good
				 * case 2. go from 0 to 10. now - old = 10 good
				 * case 3. go from 175 to -170. now - old = -345 baaaad. should be +15
				 * 	if delta < -180 degrees,    add 360 to the new number. 360 - 170 = 190. 190-175 = 15
				 * case 4. go from -175 to 170. now - old = 345 baaad. should be -15
				 *  if delta > 180 degrees, subtract 360 from the new number. 170 - 360 = -190. -190 - -175 = -15
				 */
			}
		}
		
		public void updateCurrentFloor() {
			float quarterTurnCount = (float) (totalTurnDegrees / 90);
			//TODO create array of pairs with border of turns to reach that floor and that floor's name. In future make this adaptable
			//peel off final parking the car turn (could be left or right, but doesn't change your floor any)
				//hardcoded for now. My building only lets compact cars park on the right
			quarterTurnCount -= 1;
			if(quarterTurnCount < -1) {
				parkedFloor = "1?";
			} else if (quarterTurnCount < 1) {
				parkedFloor = "1";
			} else if (quarterTurnCount < 3) {
				parkedFloor = "2";
			} else if (quarterTurnCount < 5) {
				parkedFloor = "2B";
			} else if (quarterTurnCount < 7) {
				parkedFloor = "3";
			} else if (quarterTurnCount < 9) {
				parkedFloor = "3B";
			} else if (quarterTurnCount < 11) {
				parkedFloor = "4";
			}
			
			//set the parking time too
			parkedDateString = new SimpleDateFormat(DATE_FORMAT_STRING).format(new Date());
		}
		
		//Output converts degrees into quarter turns. Easier to eyeball. Also turn convention switched to my preference
		public String toFormattedString() {
			return dateString + ", " +
					locationString + ", " +
					Float.toString(gpsAccuracy) + "," +
					Float.toString(distance) + "," +
	                Double.toString(azimuthInDegrees) + "," + 
	                Double.toString(pitchInDegrees) + "," +
	                Double.toString(rollInDegrees) + "," +
	                Double.toString(inclinationInDegrees) + "," +
	                Double.toString(totalTurnDegrees * -1) + "," +
	                Double.toString(totalTurnDegrees / -90) +
	                "\n";
		}
		
	}
	
	class CompassReading {
	    
		public String dateString;
		public float x;
		public float y;
		public float z;
		public float angle;
		public float accuracy;
		
		CompassReading(String dateString, SensorEvent event) {
			this.dateString = dateString;
			this.x = event.values[0];
			this.y = event.values[1];
			this.z = event.values[2];
			this.angle = event.values[3];
			this.accuracy = event.values[4];
		}
		
		public String toFormattedString() {
			return dateString + ", " +
	                Float.toString(x) + "," + 
	                Float.toString(y) + "," +
	                Float.toString(z) + "," +
	                "\n";
		}
		
	}
	
	class HumidityReading {
		public String dateString;
		public float humidPercent;
		
		HumidityReading(String dateString, SensorEvent event) {
			this.dateString = dateString;
			this.humidPercent = event.values[0];
		}
		
		public String toFormattedString() {
			return dateString + ", " +
	                Float.toString(humidPercent) + 
	                "\n";
		}
		
	}
	
	class PressureReading {
		
		public String dateString;
		public float pressure;
		
		PressureReading(String dateString, SensorEvent event)  {
			this.dateString = dateString;
			this.pressure = event.values[0];
		}
		
		public String toFormattedString() {
			return dateString + ", " +
	                Float.toString(pressure) + 
	                "\n";
		}
	
	
	}
	
	class AccelerometerReading {
		//public Date date; //might be nice to return this someday so I can do math, but probably not in the near future
		public String dateString;
		public Location location;
		public String locationString;
		public boolean createdOrientationReading = false;
		public float x;
		public float y;
		public float z;
		public float mag;
		public float xDel;
		public float yDel;
		public float zDel;
		public float magDel;	
			
		
		AccelerometerReading(String dateString, Location location, SensorEvent event) {
			this.dateString = dateString;
			this.location = location;
			this.locationString = location.getLatitude() + " " + location.getLongitude();
			x = event.values[0];
	        y = event.values[1];
	        z = event.values[2];
	        mag = (float) Math.sqrt(x*x + y*y + z*z);
	        
	        if(accRecent.size() == 0) {
	        	xDel = 0;
	        	yDel = 0;
	        	zDel = 0;
	        	magDel = 0;
	        } else {
	        	xDel = Math.abs(accRecent.get(accRecent.size() - 1).x - x);
	            yDel = Math.abs(accRecent.get(accRecent.size() - 1).y - y);
	            zDel = Math.abs(accRecent.get(accRecent.size() - 1).z - z);
	            magDel = Math.abs(accRecent.get(accRecent.size() - 1).mag - mag);
	
	            if (xDel < ACCELEROMETER_NOISE) 
	            	xDel = (float)0.0;
	            if (yDel < ACCELEROMETER_NOISE) 
	            	yDel = (float)0.0;
	            if (zDel < ACCELEROMETER_NOISE) 
	            	zDel = (float)0.0;
	            if (magDel < ACCELEROMETER_NOISE) 
	            	magDel = (float)0.0;
	        } 
		}
		
		public String toFormattedString() {
			return dateString + ", " +
					locationString + ", " +
	                Float.toString(x) + ", " +
	                Float.toString(y) + ", " +
	                Float.toString(z) + ", " +
	                Float.toString(mag) + ", " +
	                Float.toString(xDel) + ", " +
	                Float.toString(yDel) + ", " +
	                Float.toString(zDel) + ", " +
	                Float.toString(magDel) + 
	                "\n";
		}		
	}

}
