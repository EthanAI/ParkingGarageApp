/*
 * Service to read the accelerometers and store data in a text file. 
 * Allows human analysis of the data afterwards, allows multiple activites
 * to access the data by reading the text file.
 * 
 * Advice from https://www.youtube.com/watch?v=GAOH7XTW7BU
 */

package com.ethanai.parkinggarageapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

//TODO task bar icon http://developer.android.com/guide/topics/ui/notifiers/notifications.html

@SuppressLint("SimpleDateFormat")
public class SensorService extends Service implements SensorEventListener {
	
    private SensorManager mSensorManager;
    
    private Sensor mAccelerometer;
    private Sensor mMagn;
    private Sensor mCompass;
    private Sensor mPressure;
    
    	
    private final String STORAGE_DIRECTORY_NAME = "Documents";
    private File accelerometerFile = null;
    private File magnFile = null;
    private File compassFile = null;
    private File pressureFile = null;
    private File orientFile = null; //hold orientation derived from accelerometer and magnetic fields
   
	private String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm";

	private int maxReadingHistoryCount = 1000;
	private RecentSensorData recentData =  new RecentSensorData(maxReadingHistoryCount);
	
    private String accHeader = "Time, Xacc, Yacc, Zacc, MagAcc, Xjerk, Yjerk, Zjerk, MagJerk\n";
    private String magnHeader = "Date, x, y, z\n";
    private String orientHeader = "Time, azimuth, pitch, roll, inclination\n";
    private String compassHeader = "Date, x, y, z, total, accuracy\n";
	private String pressureHeader = "Date, Pressure(millibars)\n";

	private final String ACCELEROMETER_TAG 	= "accelerometer";
	private final String MAGNETIC_TAG 		= "magnetic";
	private final String ORIENTATION_TAG 	= "orientation";
	private final String COMPASS_TAG 		= "compass";
	private final String PRESSURE_TAG 		= "pressure";

	
	/* VERY temporary implementation. We will want the on/off triggered in other ways. 
	 * 1. Want to have this guy hide in the background pretty much permenantly (upon app creation?)
	 * 2. Want it to hide/sleep until connecting to bluetooth. Then checks decide if it's truly time 
	 * turn on (drive home vs drive out)
	 * 3. Turn off when bluetooth disabled
	 */
	public int onStartCommand(Intent intent, int flags, int startID) {
		Toast.makeText(this, "Sensors Started", Toast.LENGTH_SHORT).show();
		
        //get info from the calling Activity
        Bundle extras = intent.getExtras();
        if(extras != null){
            //maxReadingHistoryCount = extras.getInt("maxReadingHistoryCount");
        }
		Date date = new Date();        
	    String dateString = new SimpleDateFormat(DATE_FORMAT_STRING).format(date);   
	    String locationString = "";
	    accelerometerFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + locationString + " accelReadings.csv");
	    magnFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + locationString + " magReadings.csv"); 
	    compassFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + locationString + " compassReadings.csv"); 
	    pressureFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + locationString + " pressureReadings.csv"); 	
	    orientFile = createExternalFile(STORAGE_DIRECTORY_NAME, dateString + locationString + " orientationReadings.csv"); 	
		
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagn = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); //TODO break out into its own listener 
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagn, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
                        				
		return START_STICKY; //keep running until specifically stopped
	}
	
	public void onDestroy() {
		Toast.makeText(this, "Sensors Stopped", Toast.LENGTH_SHORT).show();
		super.onDestroy();		
		reportParkedFloor();
		
		mSensorManager.unregisterListener(this);
		//unregisterReceiver(receiver);
	}
	
	/*
	 * Should update some local stored data so later we can retrieve it. 
	 * Also update widget or notification icon
	 */
	public void reportParkedFloor() {
		//TODO
		Toast.makeText(this, recentData.parkedFloor, Toast.LENGTH_SHORT).show();

	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		//possibly I don't need to bind to anything. 
		//I write to file. Everyone else reads from that file
		return null;
	}

	//OK that takes care of initializing & closing this service. Now we have the meat for what it does while alive
    //@Override
    public void onSensorChanged(SensorEvent event) {
    	//This could use some refactoring
    	String dateString = new SimpleDateFormat(DATE_FORMAT_STRING).format(new Date());
        Sensor sensor = event.sensor;        
        //handle accelerometer update
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        	//Log.i(ACCELEROMETER_TAG, dateString);
            int initialOrientationReadingCount = 0;
            String oldFloor = "";
            String newFloor = "";
            if(recentData != null && recentData.orientRecent != null) {
            	initialOrientationReadingCount = recentData.orientRecent.size();
            	oldFloor = recentData.parkedFloor;
            }

            recentData.addUpToLimit(dateString, event);
            if(!accelerometerFile.exists()) {
                writeNewFile(accelerometerFile, accHeader + "\n");
            } else {
                appendToFile(accelerometerFile, recentData.accRecent.get(recentData.accRecent.size() - 1).toFormattedString());
                //notify activities they should update based on the new data 
                notifyUpdate(ACCELEROMETER_TAG);      
            }
            
            //also update the orientation records if new one was generated
            if(recentData != null && recentData.orientRecent != null && initialOrientationReadingCount < recentData.orientRecent.size()) {
                if(!orientFile.exists()) {
                    writeNewFile(orientFile, orientHeader + "\n");
                } else {
                    appendToFile(orientFile, recentData.orientRecent.get(recentData.orientRecent.size() - 1).toFormattedString());   
                    //temporary graphic to report floor changes when they happen. good for testing
                    newFloor = recentData.parkedFloor;
                    if(!newFloor.equalsIgnoreCase(oldFloor)) {
                    	//Toast.makeText(this, newFloor, Toast.LENGTH_SHORT).show();
                    }
                    //notify activities they should update based on the new data 
                    notifyUpdate(ORIENTATION_TAG);   //important this happens last i think
                }
            }
            
        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
        	//Log.i(MAGNETIC_TAG, dateString);
            int initialOrientationReadingCount = 0;
            String oldFloor = "";
            String newFloor = "";
            if(recentData != null && recentData.orientRecent != null) {
            	initialOrientationReadingCount = recentData.orientRecent.size();
            	oldFloor = recentData.parkedFloor;
            }
            
        	recentData.addUpToLimit(dateString, event);
            
        	if(!magnFile.exists()) {
        		writeNewFile(magnFile, magnHeader + "\n");
	        } else {
	            appendToFile(magnFile, recentData.magnRecent.get(recentData.magnRecent.size() - 1).toFormattedString());
	        }     	
        	
            notifyUpdate(MAGNETIC_TAG);    //seem only able to send one update 
            
          //also update the orientation records if new one was generated
            if(recentData != null && recentData.orientRecent != null && initialOrientationReadingCount < recentData.orientRecent.size()) {
                if(!orientFile.exists()) {
                    writeNewFile(orientFile, orientHeader + "\n");
                } else {
                    appendToFile(orientFile, recentData.orientRecent.get(recentData.orientRecent.size() - 1).toFormattedString());   
                    //temporary graphic to report floor changes when they happen. good for testing
                    newFloor = recentData.parkedFloor;
                    if(!newFloor.equalsIgnoreCase(oldFloor)) {
                    	//Toast.makeText(this, newFloor, Toast.LENGTH_SHORT).show();
                    }
                    //notify activities they should update based on the new data 
                    notifyUpdate(ORIENTATION_TAG);   //important this happens last i think
                }
            }
            
        } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
        	//Log.i(COMPASS_TAG, dateString);

        	recentData.addUpToLimit(dateString, event);
            
        	if(!compassFile.exists()) {
        		writeNewFile(compassFile, compassHeader + "\n");
	        } else {
	            appendToFile(compassFile, recentData.compassRecent.get(recentData.compassRecent.size() - 1).toFormattedString());
	        }     	
        	
            notifyUpdate(COMPASS_TAG);      
            
        } else if (sensor.getType() == Sensor.TYPE_PRESSURE) {
        	//Log.i(PRESSURE_TAG, dateString);
            float millibars = event.values[0];
            
            if(!pressureFile.exists()) {
        		writeNewFile(pressureFile, pressureHeader + "\n");
	        } else {
	            appendToFile(pressureFile, dateString + "," + millibars + "\n");
	        }     
            notifyUpdate(PRESSURE_TAG);    
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





	
	/* Possibly irrelevent now that this is a service vs activity
	 *     
	//stops the sensor (by stopping the sensor listener) if this activity goes in the background
    //Resumes when activity is restored
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
	 */
}
