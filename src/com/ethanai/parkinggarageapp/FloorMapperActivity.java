package com.ethanai.parkinggarageapp;

import java.util.ArrayList;

import com.ethanai.parkinggarageapp.UserSettings.Floor;
import com.ethanai.parkinggarageapp.UserSettings.GarageLocation;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest.Builder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Need to make sure user
 * 1. runs this on the way to the garage, not at the garage
 * 2. finalizes on top floor but not parked
 */
public class FloorMapperActivity extends Activity {
	public static UserSettings mySettings; // = MainActivity.mySettings; 
    public static RecentSensorData recentData; // = MainActivity.recentData;
	
    public int floorCount = 1;
    
    public String newGarageName;
    public PhoneLocation newLocation;
    public ArrayList<Floor> floors = new ArrayList<Floor>();
    
    public TextView tvGarageName;
    public TextView tvFloor;
    public TextView tvActionHistory;
    
    public String actionHistory = "";
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_mapper);
        
        if(null == MainActivity.mySettings)
			mySettings = DaemonReceiver.mySettings;
		else
			mySettings = MainActivity.mySettings;
		
		if(null == MainActivity.recentData)
			recentData = DaemonReceiver.recentData;
		else
			recentData = MainActivity.recentData;
		
		getGarageName();
		
        //garageName = getIntent().getStringExtra("garageName");
        
        tvGarageName = (TextView) findViewById(R.id.garage_name);
        tvFloor = (TextView) findViewById(R.id.next_floor_number);
        tvActionHistory = (TextView) findViewById(R.id.action_history);

        tvGarageName.setText("Garage: ");
        tvFloor.setText("Preparing Floor: " + Integer.toString(floorCount));
        
        addAds();
        
    }
    
	public void getGarageName() {	
		
		//get new name
		//dialog to select text //currently allow redoing any of them
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    // Get the layout inflater
	    LayoutInflater inflater = this.getLayoutInflater();
	    
	    final View layout = inflater.inflate(R.layout.dialog_get_garage_name, null);

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(layout)
	    		.setTitle("Enter New Garage Name")
	    		// Add action buttons
	           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) { 
	                   EditText garageNameField = (EditText) layout.findViewById(R.id.new_garage_name);
	                   newGarageName = garageNameField.getText().toString();
	                   //Toast.makeText(getApplicationContext(), newGarageName, Toast.LENGTH_SHORT).show();
	                   
	                   tvGarageName.setText("Garage: " + newGarageName);
	                   dialogInstructUser();
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	  FloorMapperActivity.this.finish(); //go back to calling activity
	               }
	           })
	           .setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
		            	  FloorMapperActivity.this.finish(); //go back to calling activity
					}
	           }) 
	           ;      
	    builder.create();
	    builder.show();
	    
	}
	
	public void dialogInstructUser() {
		//instruct user
		AlertDialog.Builder startBuilder = new AlertDialog.Builder(this);
		startBuilder.setMessage("You must start profiling approx. 1/2 mile away from the garage. "
				+ "If you are parked at the garage you would like to map, drive a few block away before starting.")
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
 
                   }
               });
		startBuilder.create();
		startBuilder.show();
	}
    
	public void startMapping(View view)  {
		stopSensors();
		actionHistory += "Mapping Active: \n";
		tvActionHistory.setText(actionHistory);

		forceStartSensors();
	}
	
	public void stopSensors(View view) {
		stopSensors();
	}
	
	public void stopSensors() {
		Intent intent = new Intent(getApplicationContext(), SensorService.class);
		stopService(intent); 
	}
	
    public void forceStartSensors() {
    	Toast.makeText(getBaseContext(), "ForceStart", Toast.LENGTH_SHORT).show();
    	Intent intent = new Intent(getBaseContext(), SensorService.class);
    	intent.putExtra("debugState", "true");
		startService(intent);  
    }
	
    //Name already assigned. Get 2. garage GPS location and 3. turncounts/sensor pattern for each floor
    public void addFloor(View view) { 	
    	DataAnalyzer dataAnalyzer = new DataAnalyzer(recentData);
		
    	//make sure to only do meaty actions if we have running sensors etc. 
    	if(null == recentData.newestPhoneLocation) {
    		Toast.makeText(getBaseContext(), "No location data yet\nTurn on sensors", Toast.LENGTH_SHORT).show();
    	} else if (dataAnalyzer.turnDegreesArray.size() == 0) {
    		Toast.makeText(getBaseContext(), "Sensors not running\nRestart 1 km away", Toast.LENGTH_SHORT).show();
    	} else {
    		if(null == newLocation) {
    			newLocation = recentData.newestPhoneLocation; //maybe do this at final floor?
    			actionHistory += "Garage Location Recorded: " + newLocation.getLocationCoordinates() + "\n";
    		}
    		
	    	float turnCount = dataAnalyzer.getConsecutiveTurns();
	    	Floor newFloor = mySettings.new Floor(turnCount, floorCount, Integer.toString(floorCount));
	    	floors.add(newFloor);
	    	
	    	actionHistory += "Floor Mapped: " + Integer.toString(floorCount) + "\n";
    		Toast.makeText(getBaseContext(), "Added pattern: " + newGarageName + "\n" 
    				+ floorCount + " " + turnCount, Toast.LENGTH_SHORT).show();
    		floorCount++;

    		//update display
            tvFloor.setText("Preparing Floor: " + Integer.toString(floorCount));
    		tvActionHistory.setText(actionHistory);
    	} 
    }
    
    public void addAds() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        Builder adBuilder = new AdRequest.Builder();
        adBuilder.addTestDevice("2EF171E12F703640E851B84E5314ED51");
        AdRequest adRequest = adBuilder.build();
        adView.loadAd(adRequest);
	}
    
    public void complete(View view) {

    	stopSensors();
    	Toast.makeText(getApplicationContext(), "New Garage Profile Complete", Toast.LENGTH_SHORT).show();
    	//build garage location and add it
    	//PhoneLocation phoneLocation = recentData.newestPhoneLocation;	
    	GarageLocation newCustomGarage = mySettings.new GarageLocation(newGarageName, newLocation, floors);
    	mySettings.userAddedGarageLocations.add(newCustomGarage);
    	mySettings.allGarageLocations.add(newCustomGarage);
    	mySettings.enabledGarageLocations.add(newCustomGarage);
    	mySettings.saveSettings();
    	//toast
    	
    	finish();
    }

}
