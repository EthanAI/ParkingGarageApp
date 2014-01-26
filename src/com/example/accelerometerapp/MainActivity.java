/*
    Adapted from http://www.techrepublic.com/blog/software-engineer/a-quick-tutorial-on-coding-androids-accelerometer/#.
    Project to record accelerometer data.
    Goals:
        xRead sensors
        xRecord sensors
        Graphical View
        Run only when in parking garage
            Subgoals to achieve this:
                Run in background
                GPS/BT to trigger it on and off
        xExport data
        Pattern analyze
            Is acc or jerk the best descriptor? Record both for now
 */

package com.example.accelerometerapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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


    private final String DIRECTORY_NAME = "Documents";
    File externalFile = null;
    private final String HEADERS = "Time, Xacc, Yacc, Zacc, MagAcc, Xjerk, Yjerk, Zjerk, MagJerk";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        
        Date date = new Date();
        String dateString = date.toString(); //TODO neaten up the date
        String fileName = dateString + "sensorReadings.csv";

        externalFile = createExternalFile(DIRECTORY_NAME, fileName);
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
        Date changedDate = new Date();

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
            mLastMag = (float) Math.sqrt(x*x + y*y + z*z);

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

            writeNewFile(externalFile, HEADERS + "\n");

            /*
            writeToFile("Test data", fileName);
            String testRead = "";
            testRead = readFromFile(fileName);
            System.out.println(testRead);
            Log.i("Test", testRead);
            */

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
            Log.i("test", changedDate.toString());
            appendToFile(externalFile,
                    changedDate.toString() + ", " +
                    Float.toString(x) + ", " +
                    Float.toString(y) + ", " +
                    Float.toString(z) + ", " +
                    Float.toString(mag) + ", " +
                    Float.toString(deltaX) + ", " +
                    Float.toString(deltaY) + ", " +
                    Float.toString(deltaZ) + ", " +
                    Float.toString(deltaMag) + "\n");

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

    public <E> String toReverseVerticalList(ArrayList<E> list) {
        String listText = "";
        for(int i = list.size() - 1; i >= 0; i--) {
            listText += list.get(i).toString();
            if(i > 0)
                listText += "\n";
        }
        return listText;
    }

    /*
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
    */

/*
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
    */

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

    public void writeToFile(File file, String text, Boolean isAppend) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, isAppend);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(text);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
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


    /*
     * Function for the button
     */
    public void changeToGraphActivity(View view) {
        Intent intent = new Intent(MainActivity.this, GraphActivity.class);
        startActivity(intent);
    }

    
    
    
    //Code Fragments for reference
    
    /*
    Log.i("Test", date.toString());
    Log.i("Test", getFilesDir().getPath());
    File f = getStorageFile(fileName);
    Log.i("Test", f.getAbsolutePath());
    Log.i("Test", String.valueOf(isExternalStorageWritable()));
    try {


        // This is the file that should be written to
        String sdCard = Environment.getExternalStorageDirectory().toString();
        File dir = new File(sdCard + "/Download");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File myFile = new File(dir.getAbsolutePath(), "savings.csv");

        // if file doesn't exists, then create it
        if (!myFile.exists()) {
            myFile.createNewFile();
        }
        FileWriter fw2 = new FileWriter(myFile);
        BufferedWriter bw2 = new BufferedWriter(fw2);
        bw2.write("Hello world");
        bw2.close();


    } catch (FileNotFoundException e) {
        Log.e("Test", e.toString());
    } catch (IOException e) {
        Log.e("Test", e.toString());
    }
    */
    
    /*//Reference Code. Button is handled by the xml. Only need to create a function for it to call here.
    final Button button = (Button) findViewById(R.id.button_id);
    button.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            // Perform action on click
        }
    });
    */

}