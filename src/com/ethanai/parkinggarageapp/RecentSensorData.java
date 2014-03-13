package com.ethanai.parkinggarageapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
//import android.util.Log;

import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
//import android.util.Log;





import android.util.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Date;
import java.util.Date;
import java.util.TimeZone;

import com.ethanai.parkinggarageapp.UserSettings.GarageLocation;

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
    public String initialLocationName; //just for testing, some use cases better off without this
	public ArrayList<AccelerometerReading> accRecent = new ArrayList<AccelerometerReading>();
	public ArrayList<MagnetReading> magnRecent = new ArrayList<MagnetReading>();
	public ArrayList<CompassReading> compassRecent = new ArrayList<CompassReading>();
	public ArrayList<HumidityReading> humidRecent = new ArrayList<HumidityReading>();
	public ArrayList<PressureReading> pressRecent = new ArrayList<PressureReading>();
	public ArrayList<DerivedOrientation> orientRecent = new ArrayList<DerivedOrientation>(); //will be dated with the time the two sensor readings got merged (here)
	public ArrayList<PhoneLocation> gpsRecent = new ArrayList<PhoneLocation>();
	public ArrayList<PhoneLocation> networkRecent = new ArrayList<PhoneLocation>();
	
	//headers for each data type:
    public final String orientHeader = "Time, Glat, long, accuracy, distance garage, garage name, bearing, age, speed, Nlat, long, accuracy, distance garage, garage name, bearing, age, speed, raw azimuth, smoothed azimuth, pitch, roll, inclination, turn degrees, quarter turns\n";
    public final String accHeader = "Time, Glat, long, accuracy, distance garage, garage name, bearing, age, speed, Nlat, long, accuracy, distance garage, garage name, bearing, age, speed, Xacc, Yacc, Zacc, MagAcc, Xjerk, Yjerk, Zjerk, MagJerk\n";
    public final String magnHeader = "Date, Glat, long, accuracy, distance garage, garage name, bearing, age, speed, Nlat, long, accuracy, distance garage, garage name, bearing, age, speed, x, y, z\n";
    public final String compassHeader = "Date, Glat, long, accuracy, distance garage, garage name, bearing, age, speed, Nlat, long, accuracy, distance garage, garage name, bearing, age, speed, x, y, z, total, accuracy\n";
	public final String pressureHeader = "Date, Glat, long, accuracy, distance garage, garage name, bearing, age, speed, Nlat, long, accuracy, distance garage, garage name, bearing, age, speed, Pressure(millibars)\n";
	public final String locationHeader = "Date, lat, long, accuracy, distance garage, garage name, bearing, age, speed \n";

	private final String BLANK_GPS_RESULT = "0,0,0,0,0,0,0,0,";
	//parts needed to be collected before creating a new DerivedOrientation
	private SensorEvent accRecentEvent = null;
	private SensorEvent magnRecentEvent = null;
	private boolean isOrientationNew = false; //flag to identify if orientation record is fresh or not
		
	//absolute final result... what floor we parked on! Also used for running current location/status
	public float  turnConsecutiveCount;
	public String parkedFloor = "";
	public String parkedDateString ="";
	public PhoneLocation newestPhoneLocation; //hold our final best location decision
	public PhoneLocation currentGPSLocation;
	public PhoneLocation currentNetworkLocation;
	public float distanceNearestGarage = 0; //assume we're in a garage until we get a proper piece of data to confirm
	//public final String HOME_TAG = "Home";
	
	public String recentLocationString = "";

	RecentSensorData() {
		initialDate = new Date();  //date this structure initialized. Caller needs to be careful so this is close to the start of sensor polling  
		format.setTimeZone(TimeZone.getTimeZone("HST"));
		
		//initialize string with all gps location
		if(null == currentGPSLocation)
			recentLocationString = BLANK_GPS_RESULT;
		else
			recentLocationString = currentGPSLocation.getLocationString() + ", ";
		if(null == currentNetworkLocation)
			recentLocationString += BLANK_GPS_RESULT;
		else
			recentLocationString += currentNetworkLocation.getLocationString();
	}
	
	RecentSensorData(Context context) {
		this();
		
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		
		newestPhoneLocation = new PhoneLocation(location);
		distanceNearestGarage = newestPhoneLocation.getDistanceNearestGarage();
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
	public <E> void addUpToLimit(SensorEvent event) {
		Sensor sensor = event.sensor;     
		int sensorType = sensor.getType();
		
		switch (sensorType) {
		case Sensor.TYPE_ACCELEROMETER:
        	addUpToLimit(accRecent, new AccelerometerReading(event));  
        	
        	//also try to create an orientation data record
        	accRecentEvent = event; //add this sensor event or overwrite stale data
        	if((accRecentEvent != null) && (magnRecentEvent != null)) { //if we have both parts, build an orientation record and add it
        		addUpToLimit(orientRecent, new DerivedOrientation(accRecentEvent, magnRecentEvent));
        		updateParkingData();
        		isOrientationNew = true;  //flag that this most recent accelerometer reading was used for calculating a new orientation reading
        		accRecentEvent = null; // require new readings for both sensors before building another
        		magnRecentEvent = null;
        	}
        	break;
        		        	
		case Sensor.TYPE_MAGNETIC_FIELD:
        	addUpToLimit(magnRecent, new MagnetReading(event));
        	
        	//also try to create an orientation data record
        	magnRecentEvent = event; //add this sensor event or overwrite stale data
        	if((accRecentEvent != null) && (magnRecentEvent != null)) { //if we have both parts, build an orientation record and add it
        		addUpToLimit(orientRecent, new DerivedOrientation(accRecentEvent, magnRecentEvent));
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
			setGPSLocation(newLocation);
		} else {
			addUpToLimit(networkRecent, new PhoneLocation(newLocation));
			setNetworkLocation(newLocation);
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
			DataAnalyzer dataAnalyzer = new DataAnalyzer(this, newestPhoneLocation);
			parkedFloor = dataAnalyzer.getCurrentFloorEstimate();  
			
			//for viewing turncount during development.
			//ArrayList<Float> turnValues = new ArrayList<Float>();
			//for(DerivedOrientation orientationObject : orientRecent)
			//	turnValues.add((float) orientationObject.totalTurnDegrees);
			turnConsecutiveCount = dataAnalyzer.getConsecutiveTurns();
		} else {
			parkedFloor = "0";
		}
		parkedDateString = format.format(new Date()); //set the parking time too
	}
	
	public void setGPSLocation(Location location) {
		currentGPSLocation = new PhoneLocation(location);
		newestPhoneLocation = getBestLocation();
		distanceNearestGarage = newestPhoneLocation.getDistanceNearestGarage();
		
		//initialize string with all gps location
		recentLocationString = "";
		if(null == currentGPSLocation)
			recentLocationString += BLANK_GPS_RESULT;
		else
			recentLocationString += currentGPSLocation.getLocationString();
		if(null == currentNetworkLocation)
			recentLocationString += "BLANK_GPS_RESULT";
		else
			recentLocationString += currentNetworkLocation.getLocationString();
	}
	
	public void setNetworkLocation(Location location) {
		currentNetworkLocation = new PhoneLocation(location);
		newestPhoneLocation = getBestLocation();
		distanceNearestGarage = newestPhoneLocation.getDistanceNearestGarage();
		
		//initialize string with all gps location
		recentLocationString = "";
		if(null == currentGPSLocation)
			recentLocationString += BLANK_GPS_RESULT;
		else
			recentLocationString += currentGPSLocation.getLocationString();
		if(null == currentNetworkLocation)
			recentLocationString += BLANK_GPS_RESULT;
		else
			recentLocationString += currentNetworkLocation.getLocationString();
	}
	
	public PhoneLocation getBestLocation() {
		return currentGPSLocation == null ? currentNetworkLocation : currentGPSLocation; //assuming true for now
	}
	
	class MagnetReading {
		
		public String dateString;
		public Date date;
		public PhoneLocation phoneLocation;
		public String locationString;
		public float x;
		public float y;
		public float z;
		
		MagnetReading(SensorEvent event) {
			this.date = new Date(event.timestamp);			
			this.dateString = format.format(new Date()); //format.format(date);
			this.phoneLocation = newestPhoneLocation;
			this.locationString =  recentLocationString; //new PhoneLocation(location).locationString; // location.getLatitude() + " " + location.getLongitude();
			this.x = event.values[0];
			this.y = event.values[1];
			this.z = event.values[2];
			
		}
		
		public String toFormattedString() {
			return dateString + ", " +
					locationString +
	                Float.toString(x) + "," + 
	                Float.toString(y) + "," +
	                Float.toString(z) + "," +
	                "\n";
		}
		
	}
	
	// Derived from coursera Class example CompassActivity
		//Also contains turn history. this will evolve to be more than just an angle. Might want to force unlimited length in recording?
		//or have final step be read the full data from the stored file? Break out into a separate object?
	/*
	 * Updated with my convention from the beginning. It's just too unintuitive to view on graph and have a raw and opposite 
	 * convention somwehwer else. Now right = negative, left = positive
	 */
	class DerivedOrientation {
		//private final double MAX_ADJACENT_CHANGE = 25;
		
		public String dateString;
		public PhoneLocation phoneLocation;
		public String locationString;
		//public float gpsAccuracy;
		//public float distance; //distance from labeled target for debugging
		
		//Values we want:
			//http://developer.android.com/reference/android/hardware/SensorManager.html#getOrientation(float[], float[])
		public double azimuthInDegrees;
		//public double correctedAzimuthInDegrees; //corrected by limiting changes by MAX_ADJACENT_CHANGE
		public double pitchInDegrees;
		public double rollInDegrees;
			//http://developer.android.com/reference/android/hardware/SensorManager.html#getInclination(float[])
		public double inclinationInDegrees; 
		
		public double totalTurnDegrees; //naive implementation, will evolve into turn counts. 

		
		DerivedOrientation(SensorEvent accEvent, SensorEvent magnEvent) {
			
			
			this.dateString = format.format(new Date());
			this.phoneLocation = newestPhoneLocation;
			
			//for the recentData structure
			distanceNearestGarage = phoneLocation.getDistanceNearestGarage();
			if(initialLocationName == null)
				initialLocationName = newestPhoneLocation.getNearestGarage().name;
			
			this.locationString =  recentLocationString; //new PhoneLocation(location).locationString; // location.getLatitude() + " " + location.getLongitude();
	    	
			//this.gpsAccuracy = newestPhoneLocation.getAccuracy();
			//this.distance = newestPhoneLocation.distanceTo(UserSettings.getUserLocation(HOME_TAG).location); //get distance to home (for now its default for me)
			
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
				azimuthInDegrees *= -1; //convert to my convention
				
				//get previous azimuth if available
				//Double previousAzimuth = orientRecent.size() > 0 
				//		? orientRecent.get(orientRecent.size() - 1).correctedAzimuthInDegrees 
				//		: azimuthInDegrees;
						
				//correctedAzimuthInDegrees = previousAzimuth + getMaxAcceptableChange(azimuthInDegrees, previousAzimuth, MAX_ADJACENT_CHANGE);
				//correction if we cross the south line
				//if(correctedAzimuthInDegrees > 180)
				//	correctedAzimuthInDegrees -= 360;
				//if(correctedAzimuthInDegrees < -180)
				//	correctedAzimuthInDegrees += 360;
					
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
		
		/* Function to limit the amount of change possible between sucessive readings
		 * getting problems with angle 180 degree changes in 200 ms. That just isn't possible
		 * Its sensor noise. Possibly due to slow reading rate, but cleaning noisy sensor data is just part of 
		 * using sensors. 
		 */
		/* hmm, this is running into trouble because the sudden 350 degree shifts are how we detect crossing the S line. 
		public double getMaxAcceptableChange(double newValue, double oldValue, double smoothingLimit){
			//get the last one. If one one exists, take that
			//if(null == oldValue) 
			//	oldValue = orientRecent.get(orientRecent.size() - 1).totalTurnDegrees;
			//else
			//	oldValue = newValue;
			double totalChange = newValue - oldValue;
			if(Math.abs(totalChange) > smoothingLimit) { //limit intensity to smoothing limit
				totalChange = totalChange / Math.abs(totalChange) * smoothingLimit;
				Log.w("DataSmoothing", totalChange + ", " + oldValue + "," + newValue);
			}
			
			return totalChange;
		}
		*/
		
		public double updateTurnDegrees(double newAzimuthInDegrees) {
			double newTotal;
			if(orientRecent == null || orientRecent.size() == 0) { //if we're the first record, initialize at zero
				newTotal = 0;
			} else {
				newTotal = orientRecent.get(orientRecent.size() - 1).totalTurnDegrees; //initialize with previous total
				double previousAzimuth = orientRecent.get(orientRecent.size() - 1).azimuthInDegrees;
				//compensate for possible crossing the South line (-180 to +180)
				if((newAzimuthInDegrees - previousAzimuth) < -180) {
					newAzimuthInDegrees += 360;
				} else if ((newAzimuthInDegrees - previousAzimuth) > 180) {
					newAzimuthInDegrees -= 360;
				}
				newTotal += newAzimuthInDegrees - previousAzimuth; //could put limiters here to save time
								
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
			return  dateString + ", " +
					recentLocationString +
	                Double.toString(azimuthInDegrees) + "," +
	                Double.toString(0.0) + "," +
	                Double.toString(pitchInDegrees) + "," +
	                Double.toString(rollInDegrees) + "," +
	                Double.toString(inclinationInDegrees) + "," +
	                Double.toString(totalTurnDegrees) + "," +
	                Double.toString(totalTurnDegrees / 90) +
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
			return dateString + ", " +
	                Float.toString(pressure) + 
	                "\n";
		}
	}
	
	class AccelerometerReading {
		//public Date date; //might be nice to return this someday so I can do math, but probably not in the near future
		public String dateString;
		public Date date;
		public PhoneLocation phoneLocation;
		public String locationString;
		public float x;
		public float y;
		public float z;
		public float mag;
		public float xDel;
		public float yDel;
		public float zDel;
		public float magDel;	
			
		
		AccelerometerReading(SensorEvent event) {
			this.date = new Date(event.timestamp);			
			this.dateString = format.format(new Date()); //format.format(date);
			this.phoneLocation = newestPhoneLocation;
			this.locationString =  recentLocationString; //new PhoneLocation(location).locationString; // location.getLatitude() + " " + location.getLongitude();
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
			return  dateString + ", " +
					locationString +
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
	
	/*
	 * Cant save any location fields/constructor anything or else cannot serialize. This is so awkward
	 */
	class PhoneLocation implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4019620007238814716L;
		public String provider;
		//public Location location; //cant have
		public double latitude;
		public double longitude;
		public double altitude;
		public float bearing;
		public float speed;
		public float accuracy;
		public long elapsedRealtimeNanos;
		
		public final int MATCH_DISTANCE = 150;
		
		public Date date;

		public String dateString;
		//public String locationString;
		public float age = 0;
		
		PhoneLocation (Location location) {
			//super(location);
			this.provider = location.getProvider();
			this.latitude = location.getLatitude();
			this.longitude = location.getLongitude();
			this.altitude = location.getAltitude();
			this.bearing = location.getBearing();
			this.speed = location.getSpeed();
			this.accuracy = location.getAccuracy();
			this.elapsedRealtimeNanos = location.getElapsedRealtimeNanos();
			
			this.date = new Date(location.getTime());			
			this.dateString = format.format(date);

			if(orientRecent != null && orientRecent.size() > 0) {
				//recent phone location of this time = orientRecent.get(orientRecent.size()-1).location
				PhoneLocation lastLocationSameProvider  = getLastLocationSameProvider();
				this.age = (location.getElapsedRealtimeNanos() - lastLocationSameProvider.getElapsedRealtimeNanos());
				this.age /= 1000000000f;
			}
			
			//Log.i("RecentData", "Age: " + age + " " + location.getProvider());
								
		}	
		
		public PhoneLocation getLastLocationSameProvider() {
			PhoneLocation lastLocation = null;
			for(int i = orientRecent.size()-1; orientRecent != null && lastLocation == null && i >= 0; i--) {
				if(provider.equalsIgnoreCase(orientRecent.get(i).phoneLocation.getProvider())) {
					lastLocation = orientRecent.get(i).phoneLocation;
				}
			}
			return lastLocation;
		}

		public String getLocationCoordinates() {
			String locationString = getLatitude() + " " + getLongitude();
			//Log.i("GarageAppGPS", locationString);
			return locationString;
		}
		
		public String getLocationName() {
			if(isAtGarage()) 
				return getNearestGarage().name;
			else
				return "Other"; //getLocationCoordinates();
		}
		
		public boolean isAtGarage() {
			return (getDistanceNearestGarage() < MATCH_DISTANCE);
		}
		
		public float getDistanceNearestGarage() {
			if(null == getNearestGarage())
				return 999999999;
			else
				return this.distanceTo(getNearestGarage().phoneLocation);
		}
		
		public GarageLocation getNearestGarage() {
			if(null != UserSettings.allGarageLocations && UserSettings.allGarageLocations.size() > 0) {
				GarageLocation closestGarage = UserSettings.allGarageLocations.get(0); //= UserSettings.allUserLocations.get(0).location;
				float closestDistance = distanceTo(closestGarage.phoneLocation); //closestLocation.location.distanceTo(location);
				for(GarageLocation garageLocation : UserSettings.allGarageLocations) {
					float checkDistance = distanceTo(garageLocation.phoneLocation);
					if(checkDistance < closestDistance) {
						closestDistance = checkDistance;
						closestGarage = garageLocation;
					}
				}
				return closestGarage;
			} else {
				return null;
			}
		}
		
		//implement replacements for Location class methods .. -_-
		public String getProvider() {
			return provider;
		}
		public double getLatitude() {
			return latitude;
		}
		public double getLongitude() {
			return longitude;
		}
		public float distanceTo(PhoneLocation phoneLocation) {
			Location here = new Location("here"); //temp for math
			Location there = new Location("there"); //also needed
			here.setLatitude(latitude);
			here.setLongitude(longitude);
			there.setLatitude(phoneLocation.latitude);
			there.setLongitude(phoneLocation.longitude);
			return here.distanceTo(there);
		}
		public double getAltitude() {
			return altitude;
		}
		public float getBearing() {
			return bearing;
		}
		public float getSpeed() {
			return speed;
		}
		public float getAccuracy() {
			return accuracy;
		}
		public long getElapsedRealtimeNanos() {
			return elapsedRealtimeNanos;
		}
		
		public String getLocationString() {
			String nearestGarageName = "";
			if(null == getNearestGarage())
				nearestGarageName = "None";
			else
				nearestGarageName = getNearestGarage().name;
			String locationString 
					= getLatitude() + ", " 
					+ getLongitude() + ", " 
					+ getAccuracy() + ", " 
					+ getDistanceNearestGarage() + ", " 
					+ nearestGarageName + ", "
					+ getBearing() + ", " 
					+ age + ", " //location.getAltitude() + ", " 
					+ getSpeed() + ", ";
			
			Log.i("RecentData", "Age: " + age + " " + getProvider());
			Log.i("RecentData", locationString);
			//if(null == )
			//	return BLANK_GPS_RESULT;
			//else
				return locationString;
		}
		
		public String toFormattedString() {
			return getLocationString() + "\n";
		}
	}
}
