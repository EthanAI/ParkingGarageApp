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


public class BluetoothSettingsActivity extends Activity {
	
	public TextView tvEnabledStatus;
	public TextView tvBTName;
	public TextView tvBTMac;
	
	public UserSettings mySettings;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoothsettings);   //get the overall layout
        
        mySettings = MainActivity.mySettings;
        
        tvEnabledStatus = (TextView) findViewById(R.id.btenabledstatus);
        tvBTName = (TextView) findViewById(R.id.btname);
        tvBTMac = (TextView) findViewById(R.id.btmac);
        
        updateTextViews();   
    }
    
    public void updateTextViews() {
    	if(mySettings.isBluetoothUser)
        	tvEnabledStatus.setText("Enabled");
        else
        	tvEnabledStatus.setText("Disabled");
        
        if(null != mySettings.carBTName)
        	tvBTName.setText(mySettings.carBTName);
        else
        	tvBTName.setText("None");
        
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
	
	public void finish(View view) {
		finish();
	}

}
