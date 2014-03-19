package com.ethanai.parkinggarageapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

//Activity dashboard for the user. Replaces the developer's GraphActivity in final product
public class MainActivity extends Activity {
	
	public static UserSettings mySettings; 
    public static RecentSensorData recentData;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //set up structure to hold recent data (not all data so we can run for unlimited time)
        mySettings = new UserSettings(); 
        recentData =  new RecentSensorData(getBaseContext());        
        
        TextView tvGarage = (TextView) findViewById(R.id.garageField);
        tvGarage.setText("Garage: TODO");
        
        TextView tvFloor = (TextView) findViewById(R.id.floorField);
        tvFloor.setText("Floor: TODO");
	}
	
	@Override
	protected void onDestroy() {
	  super.onDestroy();
	}
	
	public void toGraphActivity(View view) {
		Intent intent = new Intent(MainActivity.this, GraphActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	public void toHistoryActivity(View view) {
		Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	public void toSettingsActivity(View view) {
		Intent intent = new Intent(MainActivity.this, GarageSettingsActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	public void toBluetoothSettingsActivity(View view) {
		Intent intent = new Intent(MainActivity.this, BluetoothSettingsActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	
	public void sendDebugLog(View view) {
		Toast.makeText(getApplicationContext(), "Not Implemented Yet", Toast.LENGTH_SHORT).show();
	}
	
	
}
