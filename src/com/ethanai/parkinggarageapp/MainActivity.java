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
    
    public TextView tvGarage;
    public TextView tvFloor;
    
    public String garageName;
    public String garageFloor;    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvGarage = (TextView) findViewById(R.id.garageField);
        tvFloor = (TextView) findViewById(R.id.floorField);
        
        //set up structure to hold recent data (not all data so we can run for unlimited time)
        mySettings = new UserSettings(); 
        recentData =  new RecentSensorData(getBaseContext());
        
        updateTextViews();
	}
	
	@Override
	protected void onDestroy() {
	  super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateTextViews();
	}
	
	public void updateTextViews() {
        //ArrayList<String> listStrings = readLog(1);        
        if(null != mySettings.parkingRecordRecent) {
	        garageName = mySettings.parkingRecordRecent.locationName;//listStrings.get(listStrings.size() - 1);
	        garageFloor = mySettings.parkingRecordRecent.floorName;
        } else {
        	garageName = "No Record";
        	garageFloor = "No Record";
        }
        
        tvGarage.setText("    " + garageName);
        tvFloor.setText("    Floor: " + garageFloor);
	}
	
	/*
	public ArrayList<String> readLog(int headerRowCount) {
		ArrayList<String> arrayList = new ArrayList<String>();
		File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + mySettings.STORAGE_DIRECTORY_NAME 
				+ "/parkingLog.csv");
		if(null != file && file.exists()) {
			try {
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				for(int i = 0; i < headerRowCount; i++) //skip headers
					br.readLine();
				
				String line = "";
				while((line = br.readLine()) != null) {
					String entries[] = line.split(",");
					arrayList.add(entries[mySettings.FLOOR_COLUMN_INDEX] + "\n" + entries[0] + "\n" + entries[1]
							+ "\n" + entries[2]);
				}
				br.close();			
			}
			catch (Exception e) {
				System.err.println(e.getMessage());
			}
		} else {
			arrayList.add("None");
		}
		return arrayList;
	}
	*/
	
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
