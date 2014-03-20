package com.ethanai.parkinggarageapp;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//Activity dashboard for the user. Replaces the developer's GraphActivity in final product
public class MainActivity extends Activity {
	
	public static UserSettings mySettings; 
    public static RecentSensorData recentData;	
    
    public TextView tvGarage;
    public TextView tvFloor;
    public TextView tvGarageStatus;
    public TextView tvBTStatus;
    
    public String garageName;
    public String garageFloor;    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Look up the AdView as a resource and load a request.
        addAds();
        
        tvGarage = (TextView) findViewById(R.id.garageField);
        tvFloor = (TextView) findViewById(R.id.floorField);
        tvGarageStatus = (TextView) findViewById(R.id.garage_setup_status);
        tvBTStatus = (TextView) findViewById(R.id.bt_setup_status);
        
        //set up structure to hold recent data (not all data so we can run for unlimited time)
        mySettings = new UserSettings(); 
        recentData =  new RecentSensorData(getBaseContext());
        
        if(mySettings.isFirstRun)
        	onboarding();
        
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

	public void addAds() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        Builder adBuilder = new AdRequest.Builder();
        adBuilder.addTestDevice("2EF171E12F703640E851B84E5314ED51");
        AdRequest adRequest = adBuilder.build();
        adView.loadAd(adRequest);
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
        if(null == mySettings.enabledGarageLocations || mySettings.enabledGarageLocations.size() == 0) {
        	tvGarageStatus.setText("No garages selected");
        } else {
        	tvGarageStatus.setText("");
        }
        if(null == mySettings.carBTName || mySettings.carBTName.length() == 0) {
        	tvBTStatus.setText("Bluetooth disabled");
        } else {
        	tvBTStatus.setText("");
        }
        
        tvGarage.setText("    " + garageName);
        tvFloor.setText("    Floor: " + garageFloor);
	}
	
	public void onboarding() {
		String text1 = "Welcome to Parking Garage App. This app will help you find your car inside parking garages, where "
	    		+ "GPS cannot reach. The app runs automatically when you drive, and uses the sensors do detect "
	    		+ "what floor you parked your car on. After the initial set up, the app will do everything automatically."
	    		+ "\n\nThis app in in beta. Currently it works successfuly on garages with one "
	    		+ "entrance, and no loops within the garage. This version can run automatically if "
	    		+ "you have a bluetooth stereo in your car. "
	    		+ "If you do not have a bluetooth stereo, you give it a try by running it manually. Tell us what "
	    		+ "you think of our app, and look forward to future improvments!";
		final String text2 = "Instructions:\n"
				+ "1. Use 'BT Settings' to register your Bluetooth device (if applicable).\n\n"
				+ "2. Use 'Garage Settings' to select one of the preset garages (Currently only CCV6) "
				+ "or build a custom profile for any garage. \n\n"
				+ "That's it, we'll handle the rest. \n\nAnytime your car approaches one of the garages on your list, "
				+ "the sensors will automatically turn on and calculate which floor you stop on. "
				+ "More presets garages, support for more garage types and automatic running without Bluetooth is comming "
				+ "soon. "
				+ "\n\n"
				+ "Thank you for participating in the alpha and beta.";
		final String text3 = "Coming Improvements:\n"
	    		+ "\tAuto start/stop without bluetooth stereo\n"
	    		+ "\tSupport for multi-entrance garages\n"
	    		+ "\tSupport for garages with internal intersections\n"
	    		+ "\tDatabase of pre-mapped garages\n"
	    		+ "\tImproved battery usage\n";
		AlertDialog.Builder startBuilder = new AlertDialog.Builder(this);
		startBuilder.setMessage(text1)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   messageDialog2(text2);
                   }
               });
		startBuilder.create();
		startBuilder.show();
	}
	
	public void messageDialog2(final String textMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(textMessage)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	  mySettings.isFirstRun = false;
                	  mySettings.saveSettings();
                   }
               });
        builder.create();
        builder.show();
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
