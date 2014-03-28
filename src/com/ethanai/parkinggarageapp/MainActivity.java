package com.ethanai.parkinggarageapp;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
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
    public TextView tvRunStatus;
    public TextView tvVersionNumber;
    
    public String runStatus;
    
    public String garageName;
    public String garageFloor;    
    
    private LocalBroadcastManager lbManager;
    public String NOTIFICATION_TAG = "notification";
    
    private boolean isSensorRunning = false;
    Button manualStartButton = null;
    Button manualStopButton = null;
    TableLayout tl;
	LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	TableRow row;
    
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
		    String updateStatus = intent.getStringExtra("updateStatus");
		    Log.i("MainActivity", "Recieved update " + updateStatus);
		    runStatus = updateStatus;
		    Toast.makeText(context, runStatus, Toast.LENGTH_SHORT).show();
		    updateViews();
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //set up structure to hold recent data (not all data so we can run for unlimited time)
        //If there are no user settings, get them from the DaemonReciever entry point
        //If there are no RecentSensorData, get it from the DaemonReciever entyr point
        //after this point, MainActivity fields should all point to the correct structures,
        //Even if it wasnt the one that created them
        if(null == DaemonReceiver.mySettings)
        	mySettings = new UserSettings(); 
        else
        	mySettings = DaemonReceiver.mySettings;
        if(null == DaemonReceiver.recentData)
        	recentData = new RecentSensorData(getBaseContext());
        else
        	recentData = DaemonReceiver.recentData;
        
        Log.i("MainActivity", "Created: "
				+ "States: Settings M/D: " + (null != MainActivity.mySettings) + " "
				+ (null != DaemonReceiver.mySettings) + " Data: "
				+ (null != MainActivity.recentData) + " "
				+ (null != DaemonReceiver.recentData) + " "
				);
        
        // Look up the AdView as a resource and load a request.
        addAds();
        
        tl = (TableLayout) findViewById(R.id.button_table_layout);
        
        tvGarage 		= (TextView) findViewById(R.id.garageField);
        tvFloor 		= (TextView) findViewById(R.id.floorField);
        tvGarageStatus 	= (TextView) findViewById(R.id.garage_setup_status);
        tvBTStatus 		= (TextView) findViewById(R.id.bt_setup_status);
        tvRunStatus 	= (TextView) findViewById(R.id.run_status);
        tvVersionNumber = (TextView) findViewById(R.id.version);
        
        manualStopButton = new Button(this);
		manualStopButton.setText("Manual Stop");
		manualStopButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {
	        	Intent intent = new Intent(getApplicationContext(), SensorService.class);
	    		stopService(intent); 
	    		
	    	    isSensorRunning = false;	
	        }
	    });

		manualStartButton = new Button(this);
		manualStartButton.setText("Manual Start");
		manualStartButton.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View view) {
	        	Intent serviceIntent = new Intent(MainActivity.this, SensorService.class);
	    	    startService(serviceIntent);
	    	    
	    	    isSensorRunning = true;
	        }
	    });
        
        if(mySettings.isFirstRun)
        	onboarding();
        	
        //updateViews();
        row = new TableRow(this);
        
        //subscribe to message to update our view (notifiers or sensors might change our state)
        lbManager = LocalBroadcastManager.getInstance(getApplicationContext());
	    lbManager.registerReceiver(mMessageReceiver, new IntentFilter(NOTIFICATION_TAG));
	}
	
	@Override
	protected void onDestroy() {
	  super.onDestroy();
	  lbManager.unregisterReceiver(mMessageReceiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		updateViews();
	}
	
	public void addAds() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        Builder adBuilder = new AdRequest.Builder();
        adBuilder.addTestDevice("2EF171E12F703640E851B84E5314ED51");
        AdRequest adRequest = adBuilder.build();
        adView.loadAd(adRequest);
	}
	
	
	public void updateViews() {
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
        
        
        runStatus = getRunStatus();
        
        tvRunStatus.setText("Automation Status: " + runStatus);
        
        try {
			String versionName = getPackageManager()
				    .getPackageInfo(getPackageName(), 0).versionName;
			tvVersionNumber.setText("Version: " + versionName);
		} catch (NameNotFoundException e) {
			Log.e("MainActivity", e.getMessage());
		}
        
        if(mySettings.isBluetoothUser) {    
        	tl.removeView(row);
        } else {
           	row.removeAllViews();

           	if(!isSensorRunning) {
           		row.addView(manualStartButton);
           	} else {
        		row.addView(manualStopButton);
           	}
    		
    		tl.removeView(row);
        	tl.addView(row);

    		/*
        	if(isSensorRunning) {
        		row.addView(manualStartButton);
        		
        		//tl.removeView(manualStopButton);
        		//tl.removeView(manualStartButton);
        		//tl.addView(manualStopButton, lp);
        	} else {
        		
        		row.addView(manualStopButton);

        		//tl.removeView(manualStopButton);
        		//tl.removeView(manualStartButton);
        		//tl.addView(manualStartButton, lp);
        	}
        	tl.addView(row);
        	*/
        } 
	}
	
	public String getRunStatus() {
		String newRunStatus = runStatus;
		if(null == newRunStatus) {
			newRunStatus = "Waiting for departure";
	        if(mySettings.enabledGarageLocations.size() == 0)
	        	newRunStatus += "\nWarning: No garages enabled";
        }
        
        if(!mySettings.isBluetoothUser) {
        	if(!isSensorRunning)
        		newRunStatus = "Waiting for manual start";
        	else
        		newRunStatus = "Waiting for manual stop";
	        if(mySettings.enabledGarageLocations.size() == 0)
	        	newRunStatus += "\nWarning: No garages enabled";
        } else { //assume if bluetooth user, status has been set by the sensors via parking  notification manager
        	//if(mySettings.enabledGarageLocations.size() == 0)
        	//	newRunStatus += "\nWarning: No garages enabled";
        	//else
        	//	newRunStatus = "Awaiting Departure";
        }
        
        //add additional warnings based on poor settings choices
	    if(mySettings.enabledGarageLocations.size() == 0)
	    	newRunStatus += "\nWarning: No garages enabled";
	    if(!mySettings.isBluetoothUser)
	    	newRunStatus += "\nNo Bluetooth device set. Manually start before driving.";
	    
	    return newRunStatus;
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
		/*
		final String text3 = "Coming Improvements:\n"
	    		+ "\tAuto start/stop without bluetooth stereo\n"
	    		+ "\tSupport for multi-entrance garages\n"
	    		+ "\tSupport for garages with internal intersections\n"
	    		+ "\tDatabase of pre-mapped garages\n"
	    		+ "\tImproved battery usage\n";
	    		*/
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
	
	public void disableAll(View view) {
		Toast.makeText(getApplicationContext(), "Not Implemented Yet", Toast.LENGTH_SHORT).show();
	}
	public void reEnable(View view) {
		Toast.makeText(getApplicationContext(), "Not Implemented Yet", Toast.LENGTH_SHORT).show();
	}
	
	
}
