package com.ethanai.parkinggarageapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class DaemonReceiver extends BroadcastReceiver {	
	//Temp hardcoding of my car's BT description to test with. Will need to be settable/changable in the future
	String carBTName = "XPLOD";
	String carBTMac = "54:42:49:B0:7A:C6";
	
	ParkingNotificationManager myNotifier;

	@Override
	public void onReceive(Context context, Intent intent) { //maybe not final. create new context Context getBaseContext() and pass it
		Log.i("BootReceiver", "Recieved something. " + intent.getAction());
		myNotifier = new ParkingNotificationManager(context, null);
		if(!UserLocationManager.isInitialized) {
			UserLocationManager.initialize(context);
		}
		
		if(intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
			Toast.makeText(context, "Power On!", Toast.LENGTH_SHORT).show();
		} else if(intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
			Toast.makeText(context, "Power Off!", Toast.LENGTH_SHORT).show();
		} else if(intent.getAction() == BluetoothDevice.ACTION_ACL_CONNECTED) {
			//http://stackoverflow.com/questions/9459680/how-identify-which-bluetooth-device-causes-an-action-acl-connected-broadcast
			//TODO 
				//Make 2nd deamon? Activate sensors when near home
				//check GPS is not home
			if(isCarDevice(intent)) {
				//Toast.makeText(context, "Car Connect!", Toast.LENGTH_SHORT).show();
				if(UserLocationManager.isAtHome()) {
					Toast.makeText(context, "At home, no need", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, "Outside, Start Sensors", Toast.LENGTH_LONG).show();
					startSensors(context); //possibly this getting triggered multiple times by multiple bluetooth devices (if rebooted in the car?)
				}
			} else {
				Toast.makeText(context, "Other BT Connection", Toast.LENGTH_SHORT).show();
			}
		} else if(intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
			Toast.makeText(context, "BT Disconnect!", Toast.LENGTH_SHORT).show();
			stopSensors(context);
		} else if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED){
			myNotifier.daemonNotification();
		} else {
			//Toast.makeText(context, "Unused Broadcast: " + intent.getAction(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private boolean isCarDevice(Intent intent) {
		BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		String deviceName = device.getName(); 
		String macAddress = device.getAddress();
		return (deviceName.equalsIgnoreCase(carBTName) && macAddress.equalsIgnoreCase(carBTMac));
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
