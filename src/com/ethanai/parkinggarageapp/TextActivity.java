/*
    Adapted from http://www.techrepublic.com/blog/software-engineer/a-quick-tutorial-on-coding-androids-accelerometer/#.
    Project to record accelerometer data.

 */

package com.ethanai.parkinggarageapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class TextActivity extends Activity {
	
    RecentSensorData recentData = new RecentSensorData();
	private final int LIST_LIMIT = 3;
    
	//http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "custom-event-name" is broadcasted.
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
			String sensorType = intent.getStringExtra("sensorType");
		    Log.d("GraphActivityReceiver", "Got message: " + sensorType);
		    recentData = (RecentSensorData) intent.getSerializableExtra("recentData");
		    if(sensorType.equals("accelerometer")) {
		    	displayData(recentData);
		    } else if (sensorType.equals("compass")) {
		    	
		    } else if (sensorType.equals("humidity")) {
		    	
		    } else if (sensorType.equals("pressure")) {
		    	
		    }

		}
	};

    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);   
        
        //attach to the messages about the sensor data
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("accelerometer"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("compass"));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("humidity"));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("pressure"));
    }
	
	private void displayData(RecentSensorData recentData) {
		TextView tvX= (TextView)findViewById(R.id.x_axis);
        TextView tvXJerk= (TextView)findViewById(R.id.x_axis_jerk);

        TextView tvY= (TextView)findViewById(R.id.y_axis);
        TextView tvYJerk= (TextView)findViewById(R.id.y_axis_jerk);

        TextView tvZ= (TextView)findViewById(R.id.z_axis);
        TextView tvZJerk= (TextView)findViewById(R.id.z_axis_jerk);

        TextView tvMag = (TextView)findViewById(R.id.magnitude);
        TextView tvMagJerk = (TextView)findViewById(R.id.magnitude_jerk);
        
        //make arrays to break apart each piece in the sensor reading
        //so we can put them in reverse order and in each separate field
        ArrayList<Float> xArray = new ArrayList<Float>(); 
        ArrayList<Float> yArray = new ArrayList<Float>(); 
        ArrayList<Float> zArray = new ArrayList<Float>(); 
        ArrayList<Float> magArray = new ArrayList<Float>(); 
        
        ArrayList<Float> xJerkArray = new ArrayList<Float>(); 
        ArrayList<Float> yJerkArray = new ArrayList<Float>(); 
        ArrayList<Float> zJerkArray = new ArrayList<Float>(); 
        ArrayList<Float> magJerkArray = new ArrayList<Float>(); 
               
        int i = recentData.accRecent.size() - LIST_LIMIT; //TODO inelegant solution. Streamline for practice
        if(i < 0)
        	i = 0;
        for(; i < recentData.accRecent.size(); i++) {
        	xArray.add(recentData.accRecent.get(i).x);
        	yArray.add(recentData.accRecent.get(i).y);
        	zArray.add(recentData.accRecent.get(i).z);
        	magArray.add(recentData.accRecent.get(i).mag);
        	
        	xJerkArray.add(recentData.accRecent.get(i).xDel);
        	yJerkArray.add(recentData.accRecent.get(i).yDel);
        	zJerkArray.add(recentData.accRecent.get(i).zDel);
        	magJerkArray.add(recentData.accRecent.get(i).magDel);
        }

        tvX.setText(toReverseVerticalList(xArray));
        tvY.setText(toReverseVerticalList(yArray));
        tvZ.setText(toReverseVerticalList(zArray));
        tvMag.setText(toReverseVerticalList(magArray)); 

        tvXJerk.setText(toReverseVerticalList(xJerkArray));
        tvYJerk.setText(toReverseVerticalList(yJerkArray));
        tvZJerk.setText(toReverseVerticalList(zJerkArray));
        tvMagJerk.setText(toReverseVerticalList(magJerkArray));
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
     * Function for the button
     */
    public void changeToGraphActivity(View view) {
    	//Intent intent = new Intent(TextActivity.this, GraphActivity.class);
        //startActivity(intent);
        this.finish();
    }
	
}

    