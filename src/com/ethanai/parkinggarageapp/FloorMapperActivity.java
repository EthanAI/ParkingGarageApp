package com.ethanai.parkinggarageapp;

import java.util.ArrayList;

import com.ethanai.parkinggarageapp.UserSettings.Floor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class FloorMapperActivity extends Activity {
	public static UserSettings mySettings = MainActivity.mySettings; 
    public static RecentSensorData recentData = MainActivity.recentData;
	
    public int nextFloor = 1;
    
    public String garageName;
    public ArrayList<Floor> floors = new ArrayList<Floor>();
    
    TextView tvGarageName = (TextView) findViewById(R.id.garage_name);
    TextView tvFloor = (TextView) findViewById(R.id.next_floor_number);
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_mapper);
        
        garageName = getIntent().getStringExtra("garageName");
        
        tvGarageName.setText("Garage: " + garageName);
        tvFloor.setText("Preparing Floor: " + Integer.toString(nextFloor));
        
    }
    
    public void addFloor(View view) {
    	DataAnalyzer dataAnalyzer = new DataAnalyzer(recentData);
		
    	if(null == recentData.newestPhoneLocation) {
    		Toast.makeText(getBaseContext(), "No location data yet\nTurn on sensors", Toast.LENGTH_SHORT).show();
    	} else if (dataAnalyzer.turnDegreesArray.size() == 0) {
    		Toast.makeText(getBaseContext(), "Sensors not running\nRestart 1 km away", Toast.LENGTH_SHORT).show();
    	} else {
	    	float turnCount = dataAnalyzer.getConsecutiveTurns();
	    	Floor newFloor = mySettings.new Floor(turnCount, nextFloor, Integer.toString(nextFloor));
	    	floors.add(newFloor);
    		Toast.makeText(getBaseContext(), "Added pattern: " + garageName + "\n" 
    				+ nextFloor + " " + turnCount, Toast.LENGTH_SHORT).show();
    		nextFloor++;
    		//updateTextViews();

    	} 
    }
    
    public void finish(View view) {
    	//addFloor
    	addFloor(view);
    	//build garage location and add it
    	PhoneLocation phoneLocation = recentData.newestPhoneLocation;	
    	mySettings.addGarageLocation(garageName, phoneLocation, floors);
    	//toast
    	Toast.makeText(getApplicationContext(), "New Garage Profile Complete", Toast.LENGTH_SHORT).show();
    	finish();
    }

}
