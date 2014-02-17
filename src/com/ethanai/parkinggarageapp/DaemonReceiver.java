package com.ethanai.parkinggarageapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class DaemonReceiver extends BroadcastReceiver {
	Location homeLocation = null;
	
	// Sets an ID for the notification
	final int RUN_STATE_NOTIFICATION_ID = 001;

	@Override
	public void onReceive(Context context, Intent intent) { //maybe not final. create new context Context getBaseContext() and pass it
		Log.i("BootReceiver", "Recieved something. " + intent.getAction());
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
			startSensors(context);
		} else if(intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
			Toast.makeText(context, "BT Disconnect!", Toast.LENGTH_SHORT).show();
			stopSensors(context);
		//} else if(intent.getAction() == BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) { //this doesnt seem to work and is overly complicated to implement
		//	Toast.makeText(context, "BT Change! " + "" + " " + (BluetoothA2dp.STATE_CONNECTED), Toast.LENGTH_SHORT).show();
		} else if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED){
			daemonNotification(context);
		} else {
			Toast.makeText(context, "I dont know! " + intent.getAction(), Toast.LENGTH_SHORT).show();
		}
		
	}
			
	public void daemonNotification(Context context) {
		//http://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#setContentText(java.lang.CharSequence)
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(context)
			    .setSmallIcon(R.drawable.icon_notification_receiver_listening_hdpi)
			    .setContentTitle("Parking Garage App is Active")
			    .setContentText("Not using sensors. Minimal battery usage.");
		
		Intent resultIntent = new Intent(context, GraphActivity.class);
		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		//set click behavior
		mBuilder.setContentIntent(resultPendingIntent);
		
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = 
		        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(RUN_STATE_NOTIFICATION_ID, mBuilder.build());
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
	    context.startActivity(activityIntent);
	}
	
	public void stopSensors(Context context) {
		//Toast.makeText(context, "bCast out", Toast.LENGTH_SHORT).show();
		Intent serviceIntent = new Intent(context, SensorService.class);
		context.stopService(serviceIntent);
	}
	
}
