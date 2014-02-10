package com.ethanai.parkinggarageapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.widget.Toast;

public class DaemonReceiver extends BroadcastReceiver{

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
		
	}

	public void startSensors(Context context) {
		//Toast.makeText(context, "bCast in", Toast.LENGTH_SHORT).show();
		Intent service = new Intent(context, SensorService.class);
	    context.startService(service);
	}
	
	public void stopSensors(Context context) {
		//Toast.makeText(context, "bCast out", Toast.LENGTH_SHORT).show();
		Intent service = new Intent(context, SensorService.class);
		context.stopService(service);
	}
	
}
