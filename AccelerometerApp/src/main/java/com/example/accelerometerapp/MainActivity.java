/*
    Adapted from http://www.techrepublic.com/blog/software-engineer/a-quick-tutorial-on-coding-androids-accelerometer/#.
    Project to record accelerometer data.
    Goals:
        Read sensors
        Record sensors
        Graphical View
        Run only when in parking garage
            Subgoals to achieve this:
                Run in background
                GPS/BT to trigger it on and off
        Export data & pattern analyze
            Is acc or jerk the best descriptor? Record both for now
 */

package com.example.accelerometerapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.YuvImage;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.XMLFormatter;

public class MainActivity extends Activity implements SensorEventListener {
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private final float NOISE = (float) 0.5; //minimum acceleration to be recorded. Save battery?
    private float mLastX, mLastY, mLastZ, mLastMag;
    private final int LIST_LIMIT = 3;

    private ArrayList<Float> mArrayX = new ArrayList<Float>();
    private ArrayList<Float> mArrayY = new ArrayList<Float>();
    private ArrayList<Float> mArrayZ = new ArrayList<Float>();
    private ArrayList<Float> mArrayMag = new ArrayList<Float>();
    private ArrayList<Float> mArrayXJerk = new ArrayList<Float>();
    private ArrayList<Float> mArrayYJerk = new ArrayList<Float>();
    private ArrayList<Float> mArrayZJerk = new ArrayList<Float>();
    private ArrayList<Float> mArrayMagJerk = new ArrayList<Float>();

    String dateString = "today"; //TODO add actual date call
    String fileName = dateString + "sensorReadings.txt";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


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
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Calendar cal = Calendar.getInstance();
        Date date = new Date();

        TextView tvX= (TextView)findViewById(R.id.x_axis);
        TextView tvXJerk= (TextView)findViewById(R.id.x_axis_jerk);

        TextView tvY= (TextView)findViewById(R.id.y_axis);
        TextView tvYJerk= (TextView)findViewById(R.id.y_axis_jerk);

        TextView tvZ= (TextView)findViewById(R.id.z_axis);
        TextView tvZJerk= (TextView)findViewById(R.id.z_axis_jerk);

        TextView tvMag = (TextView)findViewById(R.id.magnitude);
        TextView tvMagJerk = (TextView)findViewById(R.id.magnitude_jerk);

        //ImageView iv = (ImageView)findViewById(R.id.image); //images not used

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        float mag = (float) Math.sqrt(x*x + y*y + z*z);

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mLastMag = (float)Math.sqrt(x*x + y*y + z*z);

            for(int i = 0; i < LIST_LIMIT; i++) {
                mArrayX.add((float) 0);
                mArrayY.add((float) 0);
                mArrayZ.add((float) 0);
                mArrayMag.add((float) 0);

                mArrayXJerk.add((float) 0);
                mArrayYJerk.add((float) 0);
                mArrayZJerk.add((float) 0);
                mArrayMagJerk.add((float) 0);
            }

            tvX.setText("0.0");
            tvY.setText("0.0");
            tvZ.setText("0.0");
            tvMag.setText("0.0");

            tvXJerk.setText("0.0");
            tvYJerk.setText("0.0");
            tvZJerk.setText("0.0");
            tvMagJerk.setText("0.0");

            writeToFile("Test data", fileName);
            String testRead = "";
            testRead = readFromFile(fileName);
            System.out.println(testRead);
            Log.i("Test", testRead);
            Log.i("Test", date.toString());

            mInitialized = true;
        } else {
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);
            float deltaMag = Math.abs(mLastMag - mag);

            if (deltaX < NOISE) deltaX = (float)0.0;
            if (deltaY < NOISE) deltaY = (float)0.0;
            if (deltaZ < NOISE) deltaZ = (float)0.0;
            if (deltaMag < NOISE) deltaMag = (float)0.0;

            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mLastMag = mag;

            addUpToLimit(mArrayX, x, LIST_LIMIT);
            addUpToLimit(mArrayY, y, LIST_LIMIT);
            addUpToLimit(mArrayZ, z, LIST_LIMIT);
            addUpToLimit(mArrayMag, mag, LIST_LIMIT);
            addUpToLimit(mArrayXJerk, deltaX, LIST_LIMIT);
            addUpToLimit(mArrayYJerk, deltaY, LIST_LIMIT);
            addUpToLimit(mArrayZJerk, deltaZ, LIST_LIMIT);
            addUpToLimit(mArrayMagJerk, deltaMag, LIST_LIMIT);

            tvX.setText(toReverseVerticalList(mArrayX));
            tvY.setText(toReverseVerticalList(mArrayY));
            tvZ.setText(toReverseVerticalList(mArrayZ));
            tvMag.setText(toReverseVerticalList(mArrayMag)); //Float.toString(mag));

            tvXJerk.setText(toReverseVerticalList(mArrayXJerk));
            tvYJerk.setText(toReverseVerticalList(mArrayYJerk));
            tvZJerk.setText(toReverseVerticalList(mArrayZJerk));
            tvMagJerk.setText(toReverseVerticalList(mArrayMagJerk));

            //record in file
            writeToFile(date.getTime() + "," + Float.toString(deltaMag), fileName);

            /* //I'm not messing around with pictures to indicate acceleration. Integers are sufficient.
            iv.setVisibility(View.VISIBLE);  // sets the iv - Image View
            if (deltaX > deltaY) {
                iv.setImageResource(R.drawable.horizontal);
            } else if (deltaY > deltaX) {
                iv.setImageResource(R.drawable.vertical);
            } else {
                iv.setVisibility(View.INVISIBLE);
            }
            */
        }
    }

    public void addUpToLimit(ArrayList<Float> list, Float newEntry, int maxEntries) {
        if(list.size() > maxEntries)
            list.remove(0);
        list.add(newEntry);
    }

    public String toReverseVerticalList(ArrayList list) {
        String listText = "";
        for(int i = list.size() - 1; i >= 0; i--) {
            listText += list.get(i).toString();
            if(i > 0)
                listText += "\n";
        }
        return listText;
    }

    // file IO adapted from http://stackoverflow.com/questions/14376807/how-to-read-write-string-from-a-file-in-android
    private void writeToFile(String data, String fileName) {
        try {
            OutputStreamWriter outputStreamWriter;
            outputStreamWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    private String readFromFile(String fileName) {

        String ret = "";

        try {
            InputStream inputStream = openFileInput(fileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }


}