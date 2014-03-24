/*
 * Service to read the accelerometers and store data in a text file. 
 * Allows human analysis of the data afterwards, allows multiple activites
 * to access the data by reading the text file.
 * 
 * Advice from https://www.youtube.com/watch?v=GAOH7XTW7BU
 */

package com.ethanai.parkinggarageapp;

//import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
//import android.telephony.PhoneStateListener;
//import android.telephony.SignalStrength;
//import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("SimpleDateFormat")
public class SensorService extends Service implements SensorEventListener {
	
	public RecentSensorData recentData; // =  MainActivity.recentData; 
	private UserSettings mySettings; // = MainActivity.mySettings;
	
	//for debugging
	public boolean forceSensorStart = true;
	
    private SensorManager mSensorManager;
    
    private Sensor mAccelerometer;
    private Sensor mMagn;
    private Sensor mCompass;
    private Sensor mPressure;
    	
    private File accelerometerFile = null;
    private File magnFile = null;
    private File compassFile = null;
    //private File pressureFile = null;
    private File orientFile = null; //hold orientation derived from accelerometer and magnetic fields
    //private File signalFile = null;
    private File parkingLogFile = null;
   
	private String FILE_DATE_FORMAT_STRING = "yyyy-MM-dd HH.mm";

	//private int maxReadingHistoryCount = 100;

	//private final String ACCELEROMETER_TAG 	= "accelerometer";
	//private final String MAGNETIC_TAG 		= "magnetic";
	private final String ORIENTATION_TAG 	= "orientation";
	//private final String COMPASS_TAG 		= "compass";

	private final String GPS_UPDATE_TAG			= "gpsUpdate";
	private final String NETWORK_UPDATE_TAG	= "networkUpdate";
	
	//private final String HOME_TAG			= "Home";
	
	ParkingNotificationManager myNotifier;
	
	private LocationManager locationManager;
	public long locationUpdateMinTime = 10000; 
	//private boolean isSensorsRuning
	
	private boolean debugState = false;
	
	public int onStartCommand(Intent intent, int flags, int startID) {
		if(null == MainActivity.recentData)
			recentData = DaemonReceiver.recentData;
		else
			recentData =  MainActivity.recentData; 
		if(null == MainActivity.mySettings)
			mySettings = DaemonReceiver.mySettings;
		else
			mySettings = MainActivity.mySettings;
		
		Toast.makeText(this, "Sensors Started Debug: " + debugState, Toast.LENGTH_SHORT).show();
		Log.i("SensorService", "ServiceService Started. Debug: " + debugState);

        //get info from the calling Activity
		Bundle extras = intent.getExtras();
        if(extras != null){
        	String debugStateString = (String)extras.get("debugState");
        	if(debugStateString.equalsIgnoreCase("true"))
        		debugState = true;
        }
	        
        //location listeners
		//locationUpdateMinTime = 0; //temporary hard coding
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, networkListener);  
		
		//create notifier 
		myNotifier = new ParkingNotificationManager(this, recentData);
		//myNotifier.cancelFloorNotification();
		myNotifier.gpsRunningNotification();
		
		//set up sensor manager (sensor listeners only activate if we're close to a garage)
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if(debugState) { //for the forcestart command
        	registerSensors();
        } //otherwise wait until gps signal triggers the sensors
        
        // disabled to see if the GPS signal will trigger the sensors
        //reviseUpdateFrequency(); //decide if we should start the sensors or not
        //registerSensors(); //temporary hard coded
                        				
		return START_STICKY; //keep running until specifically stopped
	}


	public void onDestroy() {
		super.onDestroy();		
		
		//stop everything
		//stop GPS/Location listeners
		locationManager.removeUpdates(gpsListener); //undo location listeners
		locationManager.removeUpdates(networkListener);
		//stop sensors
		unregisterSensors();
		
		//((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(psListener, PhoneStateListener.LISTEN_NONE); //unregister the phone state listener
		//unregisterReceiver(receiver);
				
		//timing on this needs firming up. We shouldn't be updating widgets etc if this hasnt completed
		//possibly update using the file so we have a longer data timeframe.
		if(orientFile != null && orientFile.exists())
			new AnalyzeAllDataTask().execute(orientFile);
		
		Toast.makeText(this, "Sensors Stopped\n" + recentData.parkedFloor, Toast.LENGTH_SHORT).show();

		//display result to user
		myNotifier.cancelSensorNotification(); //turn off sensor notification
		myNotifier.cancelGPSNotification();
		//myNotifier.daemonNotification(); //turn on deamon notification //turn into a modify, not a replace?
		//myNotifier.floorNotification();
		
		// Tell widget to update 
		Intent brIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        sendBroadcast(brIntent);
         
 		//keep result somewhere
 		//storeFinalLocation();
        mySettings.parkingRecordRecent = mySettings.new ParkingRecord(recentData.parkedDateString, 
        		recentData.newestPhoneLocation, recentData.parkedFloor, orientFile);
        mySettings.saveSettings();
 		if(parkingLogFile != null && parkingLogFile.exists())
 			appendToFile(parkingLogFile, mySettings.parkingRecordRecent.toString());
 		
 		renameFiles();
	}
	
	private class AnalyzeAllDataTask extends AsyncTask<File, Void, String> {
		  
		public AnalyzeAllDataTask() {
			super();
		}
  
	    @Override
	    protected String doInBackground(File... files) {
	    	for (File file : files) {
	    		DataAnalyzer dataAnalyzer = new DataAnalyzer(file);
	    		recentData.parkedFloor = dataAnalyzer.getFloor();
	    	}
	    	return recentData.parkedFloor;
	    }
	
	    @Override
	    protected void onPostExecute(String result) {
	    	Toast.makeText(getApplicationContext(), "Parked on:\n" + recentData.parkedFloor, Toast.LENGTH_SHORT).show();
	    }
	}

	
	public void registerSensors() {
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagn = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);  
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagn, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        
        //Listen for signal strength
        	//temp disabled for debugging. Not using this data anyhow. 
        //((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); //register the phone state listener
	
        //notification 
        myNotifier.cancelGPSNotification();
        myNotifier.sensorRunningNotification();
	}
	
	public void unregisterSensors() {
		mSensorManager.unregisterListener(this); //undo sensor listeners
		
        myNotifier.cancelSensorNotification();
        myNotifier.gpsRunningNotification();
	}

    public void reviseUpdateFrequency() {
    	long newLocationUpdateMinTime;
    	//decide whats best based on our distance from nearest garage. Involve speed in the calculation?
    	if(recentData.distanceNearestGarage < 1000) {
    		newLocationUpdateMinTime = 0;
    	} else if(recentData.distanceNearestGarage < 5000) {
    		newLocationUpdateMinTime = 15 * 1000;
    	} else if(recentData.distanceNearestGarage < 10000) {
    		newLocationUpdateMinTime = 1 * 60 * 1000;
    	} else {
    		newLocationUpdateMinTime = 3 * 60 * 1000;
    	}
    	
    	if(recentData.initialLocationName != null)
    		Log.i("sensorService", "initial location: " + recentData.initialLocationName 
    				+ " current: " + recentData.newestPhoneLocation.getLocationName());
    	else
    		Log.i("sensorService", "initial location null");
    	
    	//check if we should turn on the sensors
    		//FUTURE NOTE: users might forget something and park in the same garage the started. Re-enable this someday
    	if(locationUpdateMinTime != 0 && newLocationUpdateMinTime == 0 && isNonOriginatingGarage()) {
    		registerSensors();
    		Log.i("SensorService", "sensors on");
    	}
    	
    	//check if we should turn off the sensors
    	if(locationUpdateMinTime == 0 && newLocationUpdateMinTime != 0) {
    		unregisterSensors();
    		Log.i("SensorService", "sensors off");
    	}
    	
    	//if new distance means we should slow down or speed up updates, change it
    	if(newLocationUpdateMinTime != locationUpdateMinTime) {
    		Log.i("SensorService", "Update frequency: GPS frequency: " + locationUpdateMinTime + " " + newLocationUpdateMinTime 
    				+ " " + (locationUpdateMinTime != 0 && newLocationUpdateMinTime == 0) + " "
    				+ (locationUpdateMinTime == 0 && newLocationUpdateMinTime != 0)
    				+ " " + recentData.distanceNearestGarage);
    		
    		locationUpdateMinTime = newLocationUpdateMinTime;
        	locationManager.removeUpdates(gpsListener);
        	locationManager.removeUpdates(networkListener);

        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdateMinTime, 0f, gpsListener);
        	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationUpdateMinTime, 0f, networkListener);
    	}
        	
    }
    
    public boolean isNonOriginatingGarage() {
    	boolean isNotOriginating = false;
    	String originatingGarage = recentData.initialLocationName;
    	
    	//Note doesn not have to be position location ( within < 150 m) within our sensor start radius (1000m)
    	//The 1000m limit will be handled by related logic to check the speed of the GPS polling
    	String closestGarage = recentData.newestPhoneLocation.getNearestGarage().name; 
    	isNotOriginating = originatingGarage != null 
    			&& !closestGarage.equalsIgnoreCase(originatingGarage); 
    	if(forceSensorStart == true)
    		isNotOriginating = true;
    	return isNotOriginating;
    }
	
	public LocationListener gpsListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	//gpsLocation = location;
        	recentData.addUpToLimit(location);
        	if(recentData.initialLocationName == null)
        		recentData.initialLocationName = recentData.newestPhoneLocation.getLocationName();
            //appendToFile(gpsFile, recentData.newestPhoneLocation.locationString);
        	Log.i("SensorService", "GPS update:    Distance: " + recentData.distanceNearestGarage);
        	
        	//Make updates more frequent if close, less frequent if far
        	reviseUpdateFrequency();
        	
        	notifyUpdate(GPS_UPDATE_TAG);
        }
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }
        public void onProviderEnabled(String s) {
          // try switching to a different provider
        }
        public void onProviderDisabled(String s) {
          // try switching to a different provider
        }
	};
	
	public LocationListener networkListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	//networkLocation = location;
        	if(null == recentData)
        		recentData = new RecentSensorData(getBaseContext());
        	recentData.addUpToLimit(location);
        	Log.i("SensorService", "NetworkUpdate:  Distance: " + recentData.distanceNearestGarage);
        	reviseUpdateFrequency();

        	
        	notifyUpdate(NETWORK_UPDATE_TAG);
        }
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }
        public void onProviderEnabled(String s) {
          // try switching to a different provider
        }
        public void onProviderDisabled(String s) {
          // try switching to a different provider
        }
	};
	
	/*
	//Override phone state listener to add code to react to signal strength changing
	//http://mfarhan133.wordpress.com/2010/10/15/manipulating-incoming-ougoing-calls-tutorial-for-android/
	private final PhoneStateListener psListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged (SignalStrength signalStrength) {
        	Log.i("CellSignal", signalStrength.toString());
        	String signalString = "";
        	signalString = 	signalStrength.getCdmaDbm() + ", " +
        					signalStrength.getCdmaEcio() + ", " +
        					signalStrength.getEvdoDbm() + ", " +
           					signalStrength.getEvdoEcio() + ", " +
           					signalStrength.getEvdoSnr() + ", " +
           					signalStrength.getEvdoSnr() + ", " +
           					signalStrength.getGsmBitErrorRate() + ", " +
           					signalStrength.getGsmSignalStrength() + "\n";
        	appendToFile(signalFile, new SimpleDateFormat(DATE_FORMAT_STRING).format(new Date()) +"," 
           					+ recentData.newestPhoneLocation.getLocationCoordinates() + "," + signalString);
        }
	};	
	*/
	
	public void createDataFiles() {
		//get initial location and date for file naming
	    String dateString = new SimpleDateFormat(FILE_DATE_FORMAT_STRING).format(new Date());
	    String locationName = recentData.newestPhoneLocation.getLocationName();
	    //String locationCoords = recentData.newestPhoneLocation.getLocationCoordinates();
	    //Float distanceFromNearestGarage = recentData.newestPhoneLocation.getDistanceNearestGarage();
		
	    //set up files to hold the data
	    accelerometerFile = createExternalFile(mySettings.STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " accelReadings.csv");
	    magnFile = createExternalFile(mySettings.STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " magReadings.csv"); 
	    compassFile = createExternalFile(mySettings.STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " compassReadings.csv"); 
	    //pressureFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + " " + locationString + " pressureReadings.csv"); 	
	    orientFile = createExternalFile(mySettings.STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " orientationReadings.csv"); 
	    //signalFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " signalStrength.csv");
	    parkingLogFile = createExternalFile(mySettings.STORAGE_DIRECTORY_NAME, "parkingLog.csv");
	    
		//appendToFile(orientFile, "Departed from: " + locationName + ", " + locationCoords + ", Distance: " + distanceFromNearestGarage + "\n");
		appendToFile(orientFile, recentData.orientHeader);
		//appendToFile(accelerometerFile, "Departed from: " + locationName + ", " + locationCoords + ", Distance: " + distanceFromNearestGarage + "\n");
		appendToFile(accelerometerFile, recentData.accHeader);
		//appendToFile(magnFile, "Departed from: " + locationName + ", " + locationCoords + ", Distance: " + distanceFromNearestGarage + "\n");
		appendToFile(magnFile, recentData.magnHeader);
		if(parkingLogFile.length() == 0)
			appendToFile(parkingLogFile, "Date, location, locationName, floor, sourceFile \n");
		
	}
	
	public void renameFiles() {
	    String dateString = new SimpleDateFormat(FILE_DATE_FORMAT_STRING).format(recentData.initialDate);

		String name = mySettings.STORAGE_DIRECTORY_NAME + "/" 
				+ dateString 
				+ " End " + recentData.newestPhoneLocation.getLocationName() 
				+ " Fl " + recentData.parkedFloor
				+ " Orig " + recentData.initialLocationName + " "; 
		File newFileName = new File(Environment.getExternalStorageDirectory(), name + "orientationReadings.csv");
		if(null != orientFile && orientFile.exists() && !newFileName.exists())
			orientFile.renameTo(newFileName);
		
		newFileName = new File(Environment.getExternalStorageDirectory(), name + "accelReadings.csv");
		if(null != accelerometerFile && accelerometerFile.exists() && !newFileName.exists())
			accelerometerFile.renameTo(newFileName);
		
		newFileName = new File(Environment.getExternalStorageDirectory(), name + "magReadings.csv");
		if(null != magnFile && magnFile.exists() && !newFileName.exists())
			magnFile.renameTo(newFileName);
		
		newFileName = new File(Environment.getExternalStorageDirectory(), name + "compassReadings.csv");
		if(null != compassFile && compassFile.exists() && !newFileName.exists())
			compassFile.renameTo(newFileName);
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		//possibly I don't need to bind to anything. 
		//I write to file. Everyone else reads from that file
		return null;
	}

	//Update our data object, write new reading to disk
    //@Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;  
        int sensorType = sensor.getType();
        
        recentData.addUpToLimit(event);
        
        if(orientFile == null || !orientFile.exists())
        	createDataFiles();

        switch (sensorType) {
	        case Sensor.TYPE_ACCELEROMETER: 
	            appendToFile(accelerometerFile, recentData.accRecent.get(recentData.accRecent.size() - 1).toFormattedString());
	            //notifyUpdate(ACCELEROMETER_TAG); 
	            
	            //check if this reading generated a new orientation reading
	            if(recentData.isOrientationNew()) {
	            	recentData.setOrientationUsed();
	                appendToFile(orientFile, recentData.orientRecent.get(recentData.orientRecent.size() - 1).toFormattedString());   
	                notifyUpdate(ORIENTATION_TAG);   //important this happens last i think
	            }
	            break;
	        case Sensor.TYPE_MAGNETIC_FIELD:
	            appendToFile(magnFile, recentData.magnRecent.get(recentData.magnRecent.size() - 1).toFormattedString());
	            //notifyUpdate(MAGNETIC_TAG);
	            
	            //check if this reading generated a new orientation reading
	            if(recentData.isOrientationNew()) {
	            	recentData.setOrientationUsed();
	                appendToFile(orientFile, recentData.orientRecent.get(recentData.orientRecent.size() - 1).toFormattedString());   
	                notifyUpdate(ORIENTATION_TAG);   //important this happens last i think
	            }
	            break;
	        case Sensor.TYPE_ROTATION_VECTOR:
	        	appendToFile(compassFile, recentData.compassRecent.get(recentData.compassRecent.size() - 1).toFormattedString());
	        	//notifyUpdate(COMPASS_TAG);
	        	break;
        }
    }
    
	 // broadcast notice that this sensor has updated. Also give the updated recent data 
	 private void notifyUpdate(String updateTag) {
		 Log.i("SensorService", "NotifyUpdate sending message: " + updateTag + " " + recentData.orientRecent.size() + " " 
				 + recentData.orientRecent.size());
		 Intent brIntent = new Intent(updateTag);
		 // Include data & label with the intent we send
		 brIntent.putExtra("updateType", updateTag);
		 brIntent.putExtra("recentData", (Serializable) recentData);
		 LocalBroadcastManager.getInstance(this).sendBroadcast(brIntent);
	 }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read
    * http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
    * */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void writeToFile(File file, String text, Boolean isAppend) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, isAppend);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(text);
            bw.close();
        } catch (IOException e) {
        	Log.e("Exception", e.toString());
        }
    }

    public void writeNewFile(File file, String text) {
        writeToFile(file, text, false);
    }

    public void appendToFile(File file, String text) {
        if(file.exists())
            writeToFile(file, text, true);
        else
            writeNewFile(file, text);
    }
    
    /* possible bug source. Maybe needs a separate thread because of time required
    public void insertAtFileTop(File file, String text) {
    	FileReader fr;
		try {
			fr = new FileReader(file);
	    	BufferedReader br = new BufferedReader(fr);
	    	String newText = text;
	    	String line = "";
	    	while((line = br.readLine()) != null){
	    		newText = newText + line + "\n"; 
	    	}
	    	br.close();
	    	fr.close();

	    	file.delete();
	    	appendToFile(file, newText);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}    	
    }
    */

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// wont be used
		
	}
	
    public File createExternalFile(String directory, String fileName) {
        File myFile = null;
        try {
            String sdCard = Environment.getExternalStorageDirectory().toString(); //get root of external storage
            File dir = new File(sdCard, directory);
            if (!dir.exists()) { //make directory if it doesnt exist
                dir.mkdirs();  //make all parent directories even.
            }

            myFile = new File(dir.getAbsolutePath(), fileName); //add on the filename to the total path

            // if file doesn't exists, then create it
            if (!myFile.exists()) {
                myFile.createNewFile();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return myFile;
    }
    
}