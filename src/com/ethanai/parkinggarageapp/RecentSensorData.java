package com.ethanai.parkinggarageapp;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
//import android.util.Log;

import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Date;
import java.util.Date;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class RecentSensorData implements Serializable { //must specify serializable so it can be passed by our intents neatly
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 5721779411217090251L;
	public int historyLength = UserSettings.recentDataHistoryCount;
    private final float ACCELEROMETER_NOISE = (float) 0.5;
	public DateFormat format = new SimpleDateFormat("'Date 'yyyy-MM-dd HH:mm:ss.SSS");
    
    public Date initialDate; //date this structure initialized. 
	public ArrayList<AccelerometerReading> accRecent = new ArrayList<AccelerometerReading>();
	public ArrayList<MagnetReading> magnRecent = new ArrayList<MagnetReading>();
	public ArrayList<CompassReading> compassRecent = new ArrayList<CompassReading>();
	public ArrayList<HumidityReading> humidRecent = new ArrayList<HumidityReading>();
	public ArrayList<PressureReading> pressRecent = new ArrayList<PressureReading>();
	public ArrayList<DerivedOrientation> orientRecent = new ArrayList<DerivedOrientation>(); //will be dated with the time the two sensor readings got merged (here)
	public ArrayList<PhoneLocation> gpsRecent = new ArrayList<PhoneLocation>();
	public ArrayList<PhoneLocation> networkRecent = new ArrayList<PhoneLocation>();
	
	//headers for each data type:
    public final String orientHeader = "Time, lat, long, accuracy, distance, bearing, altitude, speed, azimuth, pitch, roll, inclination, turn degrees, quarter turns\n";
    public final String accHeader = "Time, lat, long, accuracy, distance, bearing, altitude, speed, Xacc, Yacc, Zacc, MagAcc, Xjerk, Yjerk, Zjerk, MagJerk\n";
    public final String magnHeader = "Date, lat, long, accuracy, distance, bearing, altitude, speed, x, y, z\n";
    public final String compassHeader = "Date, lat, long, accuracy, distance, bearing, altitude, speed, x, y, z, total, accuracy\n";
	public final String pressureHeader = "Date, lat, long, accuracy, distance, bearing, altitude, speed, Pressure(millibars)\n";
	public final String locationHeader = "Date, lat, long, accuracy, distance, bearing, altitude, speed \n";

	//parts needed to be collected before creating a new DerivedOrientation
	private SensorEvent accRecentEvent = null;
	private SensorEvent magnRecentEvent = null;
	private boolean isOrientationNew = false; //flag to identify if orientation record is fresh or not
		
	//absolute final result... what floor we parked on! Also used for running current location/status
	public float  turnConsecutiveCount;
	public String parkedFloor = "";
	public String parkedDateString ="";
	public PhoneLocation newestPhoneLocation;
	public final String HOME_TAG = "Home";

	RecentSensorData() {
		initialDate = new Date();  //date this structure initialized. Caller needs to be careful so this is close to the start of sensor polling  
		format.setTimeZone(TimeZone.getTimeZone("HST"));
	}
	
	/*
	RecentSensorData(Date manualInitialDate) {
		initialDate = manualInitialDate;
		format.setTimeZone(TimeZone.getTimeZone("HST"));
	}
	
	RecentSensorData(int newHistoryLength) {
		historyLength = newHistoryLength;
		format.setTimeZone(TimeZone.getTimeZone("HST"));
	}
	*/
	
	/*
	 * Call the constructor for whatever sensor type made this, build a sensor reading object
	 * Store object in the appropriate ArrayList
	 * Well accept manual locations but could also use the newestLocation stored in the global field
	 */
	public <E> void addUpToLimit(PhoneLocation phoneLocation, SensorEvent event) {
		Sensor sensor = event.sensor;     
		int sensorType = sensor.getType();
		
		switch (sensorType) {
		case Sensor.TYPE_ACCELEROMETER:
        	addUpToLimit(accRecent, new AccelerometerReading(phoneLocation, event));  
        	
        	//also try to create an orientation data record
        	accRecentEvent = event; //add this sensor event or overwrite stale data
        	if((accRecentEvent != null) && (magnRecentEvent != null)) { //if we have both parts, build an orientation record and add it
        		addUpToLimit(orientRecent, new DerivedOrientation(phoneLocation, accRecentEvent, magnRecentEvent));
        		updateParkingData();
        		isOrientationNew = true;  //flag that this most recent accelerometer reading was used for calculating a new orientation reading
        		accRecentEvent = null; // require new readings for both sensors before building another
        		magnRecentEvent = null;
        	}
        	break;
        		        	
		case Sensor.TYPE_MAGNETIC_FIELD:
        	addUpToLimit(magnRecent, new MagnetReading(phoneLocation, event));
        	
        	//also try to create an orientation data record
        	magnRecentEvent = event; //add this sensor event or overwrite stale data
        	if((accRecentEvent != null) && (magnRecentEvent != null)) { //if we have both parts, build an orientation record and add it
        		addUpToLimit(orientRecent, new DerivedOrientation(phoneLocation, accRecentEvent, magnRecentEvent));
        		updateParkingData();
        		isOrientationNew = true;   //flag that this most recent magnet reading was used for calculating a new orientation reading
        		accRecentEvent = null; // require new readings for both sensors before building another
        		magnRecentEvent = null;
        	}
        	break;
        	
		case Sensor.TYPE_ROTATION_VECTOR:
        	addUpToLimit(compassRecent, new CompassReading(event));
        	break;
        	
		case Sensor.TYPE_RELATIVE_HUMIDITY:
        	addUpToLimit(humidRecent, new HumidityReading(event));
        	break;
        	
		case Sensor.TYPE_PRESSURE:
        	addUpToLimit(pressRecent, new PressureReading(event));
        	break;
		}
    }	
	
	//version for non-sensor data (gps records)
	public void addUpToLimit(Location newLocation) {
		if(newLocation.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
			addUpToLimit(gpsRecent, new PhoneLocation(newLocation));
		} else {
			addUpToLimit(networkRecent, new PhoneLocation(newLocation));

		}
	}
	
	public <E> void addUpToLimit(ArrayList<E> arrayList, E newEntry) {
        if(arrayList != null && arrayList.size() == historyLength) {
        	arrayList.remove(0);
        }
        arrayList.add(newEntry);
    }	
	
	public boolean isOrientationNew() {
		return isOrientationNew;
	}
	
	public void setOrientationUsed() {
		isOrientationNew = false;
	}
	
	public void updateParkingData() {
		if(orientRecent.size() > 1) {
			parkedFloor = DataAnalyzer.getCurrentFloorEstimate(this);  
			
			//for viewing turncount during development.
			ArrayList<Float> turnValues = new ArrayList<Float>();
			for(DerivedOrientation orientationObject : orientRecent)
				turnValues.add((float) orientationObject.totalTurnDegrees);
			turnConsecutiveCount = DataAnalyzer.getConsecutiveRightTurns(turnValues);
		} else {
			parkedFloor = "0";
		}
		parkedDateString = format.format(new Date()); //set the parking time too
	}
	
	class MagnetReading {
		
		public String dateString;
		public Date date;
		public Location location;
		public String locationString;
		public float x;
		public float y;
		public float z;
		
		MagnetReading(Location location, SensorEvent event) {
			this.date = new Date(event.timestamp);			
			this.dateString = format.format(date);			this.location = location;
			this.locationString =  new PhoneLocation(location).locationString; // location.getLatitude() + " " + location.getLongitude();
			this.x = event.values[0];
			this.y = event.values[1];
			this.z = event.values[2];
			
		}
		
		public String toFormattedString() {
			return "Date: " + dateString + ", " +
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
		
		DerivedOrientation(Location location, SensorEvent accEvent, SensorEvent magnEvent) {
			this.dateString = format.format(new Date());
			this.location = location;
			this.locationString =  new PhoneLocation(location).locationString; // location.getLatitude() + " " + location.getLongitude();
	    	
			this.gpsAccuracy = location.getAccuracy();
			this.distance = location.distanceTo(UserSettings.getUserLocation(HOME_TAG).location); //get distance to home (for now its default for me)
			
			// Storage for Sensor readings. Need both present before orientation can be derived
			float[] mGravity = new float[3]; //accelerometer
			float[] mGeomagnetic = new float[3]; //magnet meter
			// additional storage for intermediary steps in calculation
			float rotationMatrix[] = new float[9];
			float inclinationMatrix[] = new float[9];
			float orientationMatrix[] = new float[3];
						
			System.arraycopy(accEvent.values, 0, mGravity, 0, 3);
			System.arraycopy(magnEvent.values, 0, mGeomagnetic, 0, 3);
			
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
				totalTurnDegrees = updateTurnDegrees(azimuthInDegrees);
				
				/* this should happen in the recenData object, not in a DerivedOrientation object
				if(orientRecent.size() > 1) {
					parkedFloor = DataAnalyzer.getCurrentFloorEstimate(newestPhoneLocation);  
					
					//for viewing turncount during development.
					ArrayList<Float> turnValues = new ArrayList<Float>();
					for(DerivedOrientation orientationObject : orientRecent)
						turnValues.add((float) orientationObject.totalTurnDegrees);
					turnConsecutiveCount = DataAnalyzer.getConsecutiveRightTurns(turnValues);
				} else {
					parkedFloor = "0";
				}
				parkedDateString = format.format(new Date()); //set the parking time too
				*/
			}
		}
		
		public double updateTurnDegrees(double azimuthInDegrees) {
			double newTotal;
			if(orientRecent == null || orientRecent.size() == 0) { //if we're the first record, initialize at zero
				newTotal = 0;
			} else {
				newTotal = orientRecent.get(orientRecent.size() - 1).totalTurnDegrees; //initialize with previous total
				double previousAzimuth = orientRecent.get(orientRecent.size() - 1).azimuthInDegrees;
				//compensate for possible crossing the South line (-180 to +180)
				if((azimuthInDegrees - previousAzimuth) < -180) {
					azimuthInDegrees += 360;
				} else if ((azimuthInDegrees - previousAzimuth) > 180) {
					azimuthInDegrees -= 360;
				}
				newTotal += azimuthInDegrees - previousAzimuth;
								
				/*
				 * case 1. go from 0 to -10. now - old = -10 good
				 * case 2. go from 0 to 10. now - old = 10 good
				 * case 3. go from 175 to -170. now - old = -345 baaaad. should be +15
				 * 	if delta < -180 degrees,    add 360 to the new number. 360 - 170 = 190. 190-175 = 15
				 * case 4. go from -175 to 170. now - old = 345 baaad. should be -15
				 *  if delta > 180 degrees, subtract 360 from the new number. 170 - 360 = -190. -190 - -175 = -15
				 */
			}
			return newTotal;
		}
		
		//Output converts degrees into quarter turns. Easier to eyeball. Also turn convention switched to my preference
		public String toFormattedString() {
			return "Date: " + dateString + ", " +
					newestPhoneLocation.locationString + "," +
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
		public Date date;
		public float x;
		public float y;
		public float z;
		public float angle;
		public float accuracy;
		
		CompassReading(SensorEvent event) {
			this.date = new Date(event.timestamp);			
			this.dateString = format.format(date);
			this.x = event.values[0];
			this.y = event.values[1];
			this.z = event.values[2];
			this.angle = event.values[3];
			this.accuracy = event.values[4];
		}
		
		public String toFormattedString() {
			return "Date: " + dateString + ", " +
	                Float.toString(x) + "," + 
	                Float.toString(y) + "," +
	                Float.toString(z) + "," +
	                "\n";
		}
		
	}
	
	class HumidityReading {
		public String dateString;
		public float humidPercent;
		
		HumidityReading(SensorEvent event) {
			this.dateString = format.format(event.timestamp);
			this.humidPercent = event.values[0];
		}
		
		public String toFormattedString() {
			return "Date: " + dateString + ", " +
	                Float.toString(humidPercent) + 
	                "\n";
		}
		
	}
	
	class PressureReading {
		
		public String dateString;
		public float pressure;
		
		PressureReading(SensorEvent event)  {
			this.dateString = format.format(event.timestamp);
			this.pressure = event.values[0];
		}
		
		public String toFormattedString() {
			return "Date: " + dateString + ", " +
	                Float.toString(pressure) + 
	                "\n";
		}
	}
	
	class AccelerometerReading {
		//public Date date; //might be nice to return this someday so I can do math, but probably not in the near future
		public String dateString;
		public Date date;
		public Location location;
		public String locationString;
		public float x;
		public float y;
		public float z;
		public float mag;
		public float xDel;
		public float yDel;
		public float zDel;
		public float magDel;	
			
		
		AccelerometerReading(Location location, SensorEvent event) {
			this.date = new Date(event.timestamp);			
			this.dateString = format.format(date);
			this.location = location;
			this.locationString =  new PhoneLocation(location).locationString; // location.getLatitude() + " " + location.getLongitude();
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
	        	xDel = accRecent.get(accRecent.size() - 1).x - x;
	            yDel = accRecent.get(accRecent.size() - 1).y - y;
	            zDel = accRecent.get(accRecent.size() - 1).z - z;
	            magDel = accRecent.get(accRecent.size() - 1).mag - mag;
	
	            if (Math.abs(xDel) < ACCELEROMETER_NOISE) 
	            	xDel = (float)0.0;
	            if (Math.abs(yDel) < ACCELEROMETER_NOISE) 
	            	yDel = (float)0.0;
	            if (Math.abs(zDel) < ACCELEROMETER_NOISE) 
	            	zDel = (float)0.0;
	            if (Math.abs(magDel) < ACCELEROMETER_NOISE) 
	            	magDel = (float)0.0;
	        } 
		}
		
		public String toFormattedString() {
			return "Date: " + dateString + ", " +
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
	
	class PhoneLocation extends Location {
		public String provider;
		public Location location;
		
		public final int MATCH_DISTANCE = 150;
		
		public Date date;

		public String dateString;
		public String locationString;
		
		PhoneLocation (Location location) {
			super(location);
			this.provider = location.getProvider();
			this.location = location;
			this.date = new Date(location.getTime());			
			this.dateString = format.format(date);
			this.locationString = getLocationString();
					
		}	

		public String getLocationCoordinates() {
			String locationString = location.getLatitude() + " " + location.getLongitude();
			//Log.i("GarageAppGPS", locationString);
			return locationString;
		}
		
		public String getLocationName() {
			if(isAtHome()) 
				return HOME_TAG;
			else
				return getLocationCoordinates();
		}
		
		public boolean isAtHome() {
			return (location.distanceTo(UserSettings.getUserLocation(HOME_TAG).location) < MATCH_DISTANCE);
		}
		
		public String getLocationString() {
			return location.getLatitude() + ", " + location.getLongitude() + ", " + location.getAccuracy() + ", " 
					+ location.distanceTo(UserSettings.getUserLocation(HOME_TAG).location) + ", " + location.getBearing() + ", " 
					+ location.getAltitude() + ", " + location.getSpeed();
		}
		
		public String toFormattedString() {
			return getLocationString() + "\n";
		}
	}
}
