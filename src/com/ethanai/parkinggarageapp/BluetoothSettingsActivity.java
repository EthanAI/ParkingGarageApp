package com.ethanai.parkinggarageapp;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.TextView;

import com.ethanai.parkinggarageapp.UserSettings;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest.Builder;


public class BluetoothSettingsActivity extends Activity {
	
	public TextView tvBTOn;
	public TextView tvEnabledStatus;
	public TextView tvBTName;
	public TextView tvBTMac;
	
	public boolean isBluetoothOn = false;
	
	public UserSettings mySettings;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothsettings);   //get the overall layout
        
        if(null == MainActivity.mySettings)
			mySettings = DaemonReceiver.mySettings;
		else
			mySettings = MainActivity.mySettings;
        
        tvBTOn = (TextView) findViewById(R.id.isbton);
        tvEnabledStatus = (TextView) findViewById(R.id.btenabledstatus);
        tvBTName = (TextView) findViewById(R.id.btname);
        tvBTMac = (TextView) findViewById(R.id.btmac);
        
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(btAdapter != null && btAdapter.isEnabled())
			isBluetoothOn = true;
        
		addAds();
        updateTextViews();   
    }
    
    public void updateTextViews() {
    	if(isBluetoothOn) {
    		//tvBTOn.setText("Device Bluetooth is on");
    	}
    	else
    		tvBTOn.setText("Please turn on device's Bluetooth before changing settings.");
    	
    	if(mySettings.isBluetoothUser)
        	tvEnabledStatus.setText("Automatic Start?: Yes");
        else
        	tvEnabledStatus.setText("Automatic Start?: No");
        
        if(null != mySettings.carBTName)
        	tvBTName.setText("Device Name:\n" + mySettings.carBTName);
        else
        	tvBTName.setText("Device Name:\n" + "None");
        
        if(null != mySettings.carBTMac)
        	tvBTMac.setText(mySettings.carBTMac);
        else
        	tvBTMac.setText("None");
        
        tvEnabledStatus.invalidate();
        tvBTName.invalidate();
        tvBTMac.invalidate();
    }
		
	public void setBTSettings(View view) {
		setBTEnableStatus();			
	}
	
	public void	setBTEnableStatus() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Does your car have a Bluetooth Stereo?")
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   mySettings.isBluetoothUser = true;
                       pickCarBT();
               		   updateTextViews();
	        		   mySettings.saveSettings();
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   mySettings.isBluetoothUser = false;
                	   mySettings.carBTName = null;
                	   mySettings.carBTMac = null;
                	   updateTextViews();
                	   mySettings.saveSettings();
                   }
               });
        builder.create();
        builder.show();
	}
	
	public void pickCarBT() { 
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if(null == btAdapter) {
			Toast.makeText(getApplicationContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
		} else if(!btAdapter.isEnabled()) {
			Toast.makeText(getApplicationContext(), "Turn on Bluetooth and Try Again.", Toast.LENGTH_SHORT).show();
		} else {
			Set<BluetoothDevice> btList = btAdapter.getBondedDevices();
			final ArrayList<String> btNames = new ArrayList<String>();
			final ArrayList<String> btMACs = new ArrayList<String>();
			
			for(BluetoothDevice device : btList) {
				btNames.add(device.getName());
				btMACs.add(device.getAddress());
			}
			
			String btNamesArray[] = {""};
			btNamesArray = btNames.toArray(btNamesArray);
			
			AlertDialog ad = 
					new AlertDialog.Builder(this)
					.setTitle("Select Car Bluetooth Device")
					.setItems(btNamesArray, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {   
			            	   mySettings.carBTName = btNames.get(which);
			            	   mySettings.carBTMac = btMACs.get(which);
				               updateTextViews();
				               mySettings.saveSettings();
				               Toast.makeText(getApplicationContext(), "Registered: " + mySettings.carBTName
				            		   + "\n" + mySettings.carBTMac, Toast.LENGTH_SHORT).show();
	
			           }
			       }).create();
			ad.show();	
		}
	}
	
	public void addAds() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        Builder adBuilder = new AdRequest.Builder();
        adBuilder.addTestDevice("2EF171E12F703640E851B84E5314ED51");
        AdRequest adRequest = adBuilder.build();
        adView.loadAd(adRequest);
	}
	
	public void finish(View view) {
		finish();
	}

}
