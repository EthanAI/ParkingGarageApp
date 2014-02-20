package com.ethanai.parkinggarageapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class UserLocationManager {
	public static Context context;
	public static int matchDistance = 150;
	public static boolean isInitialized = false;
	
	//Make list of locations with all the relevant data
	//temp hardcoded home location
	public static Location homeLocation = new Location("hardcoded");
	
	public static void initialize(Context newContext) {
		setContext(newContext);
		setHomeLocation();
	}
	
	public static void setContext(Context newContext) {
		context = newContext;
	}
	
	public static void setHomeLocation() {
		setHomeLocation(null);
	}
	
	public static void setHomeLocation(Location newHomeLocation) {
		if(newHomeLocation == null) {
			//temporarily hardcoded the home/target garage location
	        homeLocation.setLatitude(21.3474357); //21.3474357
	    	homeLocation.setLongitude(-157.9035183); //-157.9035183	
		} else {
			homeLocation = newHomeLocation;
		}
	}
	
	public static boolean isInitialized() {
		return isInitialized;
	}
	
	public static String getLocationName() {
		Location newLocation = getLocation();
		String locationName = "";
		//need to store home/target location, then test for distance from that point. assign a label string like HOME if close
		//How to do preferences properly. Ill just hardcode something for now to test. 	
		
		if(getDistanceFromHome() < matchDistance) { //if within 100 meters of home
			locationName += "Home";
		} else {
			locationName = "Unknown";
		}
		
		Log.i("GarageAppGPS", Float.toString(newLocation.distanceTo(homeLocation)));
		Log.i("GarageAppGPS", locationName);
		
		return locationName;
	}
	
	public static Location getLocation() {
		Location bestLocation;
		//seems to work without location listener. needs field testing

		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		//LocationListener locationListener = new LocationListener();
		//do i need to define any functions for a locationlistener?
		
		// Register the listener with the Location Manager to receive location updates
		//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

		String locationProvider = LocationManager.NETWORK_PROVIDER;
		// Or, use GPS location data:
		// String locationProvider = LocationManager.GPS_PROVIDER;
		Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
		Log.i("GarageAppGPS", lastKnownLocation.toString());
		
		//insert kung fu here to check different providers and times to make sure we have an accurate location
		bestLocation = lastKnownLocation;

		return bestLocation;
	}
	
	public static float getDistanceFromHome() {
		//if(getLocation())
		//Log.i("GarageAppGPS", locationString);
		return getLocation().distanceTo(homeLocation);
	}
	
	public static String getLocationCoordinates() {
		Location myLocation = getLocation();
		String locationString = myLocation.getLatitude() + " " + myLocation.getLongitude();
		Log.i("GarageAppGPS", locationString);
		return locationString;
	}
	
	public static boolean isAtHome() {
		return (getLocation().distanceTo(homeLocation) < matchDistance);
	}

}

/* legacy code
 * 		//location code trial
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		float minAccuracy = 500.0f;
		long minTime = 1000 * 60 * 5;
		Location mBestReading;
		
		Location bestResult = null;
		float bestAccuracy = Float.MAX_VALUE;
		long bestTime = Long.MIN_VALUE;
		List<String> matchingProviders = mLocationManager.getAllProviders();

		for (String provider : matchingProviders) {

			Location location = mLocationManager.getLastKnownLocation(provider);

			if (location != null) {

				float accuracy = location.getAccuracy();
				long time = location.getTime();

				if (accuracy < bestAccuracy) {

					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;

				}
			}
		}
		if (bestAccuracy > minAccuracy || bestTime < minTime) {
			bestResult = null;
		}
		//location.getAccuracy()
		//location.getLongitude()
		//location.getLatitude()
		//end of location code
		 */
