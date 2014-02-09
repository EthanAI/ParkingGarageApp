package com.ethanai.parkinggarageapp;

import java.util.Set;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
//BluetoothDevice.ACTION_ACL_CONNECTED


public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) { //maybe not final. create new context Context getBaseContext() and pass it
		Log.i("BootReceiver", "Recieved something. " + intent.getAction());
		if(intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
			Toast.makeText(context, "Power On!", Toast.LENGTH_SHORT).show();
		} else if(intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
			Toast.makeText(context, "Power Off!", Toast.LENGTH_SHORT).show();
		} else if(intent.getAction() == BluetoothDevice.ACTION_ACL_CONNECTED) {
			Toast.makeText(context, "BT Connect!", Toast.LENGTH_SHORT).show();
			startSensors(context);
		} else if(intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
			Toast.makeText(context, "BT Disconnect!", Toast.LENGTH_SHORT).show();
			stopSensors(context);
		//} else if(intent.getAction() == BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) { //this doesnt seem to work and is overly complicated to implement
		//	Toast.makeText(context, "BT Change! " + "" + " " + (BluetoothA2dp.STATE_CONNECTED), Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "I dont know! " + intent.getAction(), Toast.LENGTH_SHORT).show();
		}
		
		
		
		
		/*
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
		}
		
		if (!mBluetoothAdapter.isEnabled()) {
		    // more checks
		}
		
		//TODO http://docs.oracle.com/javase/7/docs/api/java/util/Set.html
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		//TODO http://stackoverflow.com/questions/19079400/arrayadapter-in-android-to-create-simple-listview
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a ListView
		        //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		    }
		}
		
		BluetoothA2dp mBluetoothA2dp;
		 
		BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
		    public void onServiceConnected(int profile, BluetoothProfile proxy) {
		        if (profile == BluetoothProfile.A2DP) {
		        	//mBluetoothA2dp = (BluetoothA2dp) proxy;
		        	//startSensors(context);
		        }
		    }
		    public void onServiceDisconnected(int profile) {
		        if (profile == BluetoothProfile.A2DP) {
		        	//mBluetoothA2dp = null;
		        	//stopSensors(context);
		        }
		    }
		};
		
		 
		// Establish connection to the proxy.
		mBluetoothAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.A2DP);
		 
		// ... call functions on mBluetoothHeadset
		 
		// Close proxy connection after use.
		//mBluetoothAdapter.closeProfileProxy(mBluetoothA2dp);
		
		
		
		
		
		
		//Bundle extras = intent.getExtras();
	    //String state = extras.getString(BluetoothDevice);
*/
		
		/*
		IntentFilter testFilter = new IntentFilter();
        IntentFilter btFilter = new IntentFilter();
        
        testFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        testFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        btFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        btFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        
        registerReceiver(context, testFilter);
        registerReceiver(this, btFilter);
        */
	}

	public void startSensors(Context context) {
		//Toast.makeText(context, "bCast in", Toast.LENGTH_SHORT).show();
		Intent service = new Intent(context, AccelerometerService.class);
	    context.startService(service);
	}
	
	public void stopSensors(Context context) {
		//Toast.makeText(context, "bCast out", Toast.LENGTH_SHORT).show();
		Intent service = new Intent(context, AccelerometerService.class);
		context.stopService(service);
	}
	
}
