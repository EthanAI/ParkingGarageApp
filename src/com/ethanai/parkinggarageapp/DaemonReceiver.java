package com.ethanai.parkinggarageapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

public class DaemonReceiver extends BroadcastReceiver {
	Location homeLocation = null;
	
	ParkingNotificationManager myNotifier;

	@Override
	public void onReceive(Context context, Intent intent) { //maybe not final. create new context Context getBaseContext() and pass it
		Log.i("BootReceiver", "Recieved something. " + intent.getAction());
		myNotifier = new ParkingNotificationManager(context, null);
		if(homeLocation == null) {
			homeLocation = getHomeLocation();
		}
		if(intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
			Toast.makeText(context, "Power On!", Toast.LENGTH_SHORT).show();
		} else if(intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
			Toast.makeText(context, "Power Off!", Toast.LENGTH_SHORT).show();
		} else if(intent.getAction() == BluetoothDevice.ACTION_ACL_CONNECTED) {
			Toast.makeText(context, "BT Connect!", Toast.LENGTH_SHORT).show();
			//TODO 
				//Make 2nd deamon? Activate sensors when near home
				//check GPS is not home
			startSensors(context); //possibly this getting triggered multiple times by multiple bluetooth devices (if rebooted in the car?)
		} else if(intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
			Toast.makeText(context, "BT Disconnect!", Toast.LENGTH_SHORT).show();
			stopSensors(context);
		//} else if(intent.getAction() == BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) { //this doesnt seem to work and is overly complicated to implement
		//	Toast.makeText(context, "BT Change! " + "" + " " + (BluetoothA2dp.STATE_CONNECTED), Toast.LENGTH_SHORT).show();
		} else if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED){
			myNotifier.daemonNotification();
		} else {
			//Toast.makeText(context, "Unused Broadcast: " + intent.getAction(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public Location getHomeLocation() {
		return null; //TODO
	}

	public void startSensors(Context context) {
		//Toast.makeText(context, "bCast in", Toast.LENGTH_SHORT).show();
		Intent serviceIntent = new Intent(context, SensorService.class);
	    context.startService(serviceIntent);
	    
	    //temp for debugging. Also turn on graph activity so I can watch/confirm working
	    Intent activityIntent = new Intent(context, GraphActivity.class);
	    context.startService(activityIntent);
	}
	
	public void stopSensors(Context context) {
		//Toast.makeText(context, "bCast out", Toast.LENGTH_SHORT).show();
		Intent serviceIntent = new Intent(context, SensorService.class);
		context.stopService(serviceIntent);
	}
	
}
