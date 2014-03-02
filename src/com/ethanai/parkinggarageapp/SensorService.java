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
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
	private RecentSensorData recentData; 

	private final String ACCELEROMETER_TAG 	= "accelerometer";
	private final String MAGNETIC_TAG 		= "magnetic";
	private final String ORIENTATION_TAG 	= "orientation";
	private final String COMPASS_TAG 		= "compass";
	private final String GPS_TAG			= "gps";
	private final String NETWORK_TAG		= "network";
	
	private final String HOME_TAG			= "Home";
	
	ParkingNotificationManager myNotifier;
	
	private LocationManager locationManager;
	
	//private Location gpsLocation;
	//private Location networkLocation;	
	
	/* VERY temporary implementation. We will want the on/off triggered in other ways. 
	 * 1. Want to have this guy hide in the background pretty much permenantly (upon app creation?)
	 * 2. Want it to hide/sleep until connecting to bluetooth. Then checks decide if it's truly time 
	 * turn on (drive home vs drive out)
	 * 3. Turn off when bluetooth disabled
	 */
	public int onStartCommand(Intent intent, int flags, int startID) {
		Toast.makeText(this, "Sensors Started", Toast.LENGTH_SHORT).show();

        //get info from the calling Activity
        /*
		Bundle extras = intent.getExtras();
        if(extras != null){
        	int newMax = extras.getInt("maxReadingHistoryCount");
        	if(newMax > 0) {
        		maxReadingHistoryCount = newMax;
        	}
        }
        */
	
		//set up sensor listeners
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagn = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); //TODO break out into its own listener 
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagn, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        
        //Listen for signal strength
        	//temp disabled for debugging. Not using this data anyhow. 
        //((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); //register the phone state listener

        //location listeners
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, networkListener);  
		
		//get initial location and date for file naming
	    String dateString = new SimpleDateFormat(FILE_DATE_FORMAT_STRING).format(new Date());
	    Location initialLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER); //assume this will get us something useful always
	    
	    //set up structure to hold recent data (not all data so we can run for unlimited time)
        recentData =  new RecentSensorData(initialLocation);
	    //recentData.newestPhoneLocation = recentData.new PhoneLocation(initialLocation); //good object for getting data about locations

	    String locationName = recentData.newestPhoneLocation.getLocationName();
	    String locationCoords = recentData.newestPhoneLocation.getLocationCoordinates();
	    Float distanceFromHome = recentData.newestPhoneLocation.location.distanceTo(UserSettings.getUserLocation(HOME_TAG).location);
	    
	    //set up files to hold the data
	    accelerometerFile = createExternalFile(UserSettings.STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " accelReadings.csv");
	    magnFile = createExternalFile(UserSettings.STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " magReadings.csv"); 
	    compassFile = createExternalFile(UserSettings.STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " compassReadings.csv"); 
	    //pressureFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + " " + locationString + " pressureReadings.csv"); 	
	    orientFile = createExternalFile(UserSettings.STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " orientationReadings.csv"); 
	    //signalFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + " " + locationName + " signalStrength.csv");
	    parkingLogFile = createExternalFile(UserSettings.STORAGE_DIRECTORY_NAME, "parkingLog.csv");
	    
		appendToFile(orientFile, "Departed from: " + locationName + ", " + locationCoords + ", Distance: " + distanceFromHome + "\n");
		appendToFile(orientFile, recentData.orientHeader);
		appendToFile(accelerometerFile, "Departed from: " + locationName + ", " + locationCoords + ", Distance: " + distanceFromHome + "\n");
		appendToFile(accelerometerFile, recentData.accHeader);
		appendToFile(magnFile, "Departed from: " + locationName + ", " + locationCoords + ", Distance: " + distanceFromHome + "\n");
		appendToFile(magnFile, recentData.magnHeader);
		if(parkingLogFile.length() == 0)
			appendToFile(parkingLogFile, "Date, location, locationName, floor, sourceFile \n");
		
		//create notifier and notify sensors running
		myNotifier = new ParkingNotificationManager(this, recentData);
		myNotifier.sensorRunningNotification();
                        				
		return START_STICKY; //keep running until specifically stopped
	}


	public void onDestroy() {
		Toast.makeText(this, "Sensors Stopped", Toast.LENGTH_SHORT).show();
		super.onDestroy();		
		
		//display result to user
		myNotifier.cancelRunStateNotification(); //turn off sensor notification
		//myNotifier.daemonNotification(); //turn on deamon notification //turn into a modify, not a replace?
		myNotifier.floorNotification();
		Toast.makeText(this, recentData.parkedFloor, Toast.LENGTH_SHORT).show();

		//keep result somewhere
		//storeFinalLocation();
		appendToFile(parkingLogFile, recentData.parkedDateString + ", " + recentData.newestPhoneLocation.getLocationCoordinates() 
				+ ", " + recentData.newestPhoneLocation.getLocationName() + ", " + recentData.parkedFloor + "," 
				+ orientFile.getName().toString() + "\n");
		
		mSensorManager.unregisterListener(this); //undo sensor listeners
		locationManager.removeUpdates(gpsListener); //undo location listeners
		locationManager.removeUpdates(networkListener);
		
		//((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(psListener, PhoneStateListener.LISTEN_NONE); //unregister the phone state listener
		//unregisterReceiver(receiver);
	}
	
	/*
	public void storeFinalLocation() {
		insertAtFileTop(orientFile, "Parked at: " + recentData.newestPhoneLocation.getLocationName() + ", " + recentData.newestPhoneLocation.getLocationCoordinates() 
				+ ", Distance: " + Float.toString(recentData.newestPhoneLocation.location.distanceTo(UserSettings.getUserLocation(HOME_TAG).location)) 
				+ ", Parked Floor: " + recentData.parkedFloor + ", ");
	}
	*/

	
	public LocationListener gpsListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	//gpsLocation = location;
        	recentData.addUpToLimit(location);
        	recentData.setGPSLocation(location);
            //appendToFile(gpsFile, recentData.newestPhoneLocation.locationString);
        	notifyUpdate(GPS_TAG);
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
        	recentData.addUpToLimit(location);
        	recentData.setNetworkLocation(location);
        	notifyUpdate(NETWORK_TAG);
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

        switch (sensorType) {
	        case Sensor.TYPE_ACCELEROMETER: 
	            appendToFile(accelerometerFile, recentData.accRecent.get(recentData.accRecent.size() - 1).toFormattedString());
	            notifyUpdate(ACCELEROMETER_TAG); 
	            
	            //check if this reading generated a new orientation reading
	            if(recentData.isOrientationNew()) {
	            	recentData.setOrientationUsed();
	                appendToFile(orientFile, recentData.orientRecent.get(recentData.orientRecent.size() - 1).toFormattedString());   
	                notifyUpdate(ORIENTATION_TAG);   //important this happens last i think
	            }
	            break;
	        case Sensor.TYPE_MAGNETIC_FIELD:
	            appendToFile(magnFile, recentData.magnRecent.get(recentData.magnRecent.size() - 1).toFormattedString());
	            notifyUpdate(MAGNETIC_TAG);
	            
	            //check if this reading generated a new orientation reading
	            if(recentData.isOrientationNew()) {
	            	recentData.setOrientationUsed();
	                appendToFile(orientFile, recentData.orientRecent.get(recentData.orientRecent.size() - 1).toFormattedString());   
	                notifyUpdate(ORIENTATION_TAG);   //important this happens last i think
	            }
	            break;
	        case Sensor.TYPE_ROTATION_VECTOR:
	        	appendToFile(compassFile, recentData.compassRecent.get(recentData.compassRecent.size() - 1).toFormattedString());
	        	notifyUpdate(COMPASS_TAG);
	        	break;
        }
    }
    
	 // broadcast notice that this sensor has updated. Also give the updated recent data 
	 private void notifyUpdate(String sensorName) {
		 //Log.i("sender", "Broadcasting message " + sensorName);
		 Intent brIntent = new Intent(sensorName);
		 // Include data & label with the intent we send
		 brIntent.putExtra("sensorType", sensorName);
		 brIntent.putExtra("recentData", (Serializable)recentData);
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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