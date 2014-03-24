package com.ethanai.parkinggarageapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

public class ParkingNotificationManager {
	// Sets an ID for the notification
	final int SENSOR_NOTIFICATION_ID 	= 001;
	final int FLOOR_NOTIFICATION_ID 	= 002;
	final int GPS_NOTIFICATION_ID		= 003;
	final int DAEMON_NOTIFICATION_ID	= 004;
	
    public String NOTIFICATION_TAG = "notification"; //let mainactivity know to update its view
    public String SENSOR_MESSAGE = "Active Sensors";
    public String GPS_MESSAGE = "Passive Sensors";
    public String IDLE_MESSAGE = "Waiting for departure";
    public String MANUAL_IDLE_MESSAGE = "Waiting for manual start";
	
	public Context context;
	public RecentSensorData recentData;
	public UserSettings mySettings;
	
	ParkingNotificationManager(Context newContext, RecentSensorData newData) {
		context = newContext;
		recentData = newData;
		
		if(null == MainActivity.mySettings)
			mySettings = DaemonReceiver.mySettings;
		else
			mySettings = MainActivity.mySettings;
	}
	
	public void sensorRunningNotification() {
		//update flag for display of system status
		notifyMainActivity(NOTIFICATION_TAG, SENSOR_MESSAGE);
		
		//modify notification http://developer.android.com/training/notify-user/managing.html
		NotificationManager mNotifyMgr = 
		        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.icon_notification_sensors_on_hdpi)
			    .setContentTitle("Sensors Running")
			    .setContentText("Preparing for Parking");
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		//set click behavior
		mBuilder.setContentIntent(resultPendingIntent);
		
		//mBuilder.setContentText("Modified")
        //.setNumber(7);
	    // Because the ID remains unchanged, the existing notification is
	    // updated.
		notifyMainActivity(NOTIFICATION_TAG, SENSOR_MESSAGE);
		
		mNotifyMgr.notify(SENSOR_NOTIFICATION_ID, mBuilder.build());
	}
	
	public void gpsRunningNotification() {
		//update flag for display of system status
		notifyMainActivity(NOTIFICATION_TAG, GPS_MESSAGE);
				
		//modify notification http://developer.android.com/training/notify-user/managing.html
		NotificationManager mNotifyMgr = 
		        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
			    .setContentTitle("Parking Garage App Active")
			    .setContentText("Monitoring for Proximity to Active Garages");
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		//set click behavior
		mBuilder.setContentIntent(resultPendingIntent);
		
		//mBuilder.setContentText("Modified")
        //.setNumber(7);
	    // Because the ID remains unchanged, the existing notification is
	    // updated.
		mNotifyMgr.notify(GPS_NOTIFICATION_ID, mBuilder.build());
	}
	
	
	/*
	public void floorNotification() {
		//modify notification http://developer.android.com/training/notify-user/managing.html
		NotificationManager mNotifyMgr = 
		        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.icon_notification_floor_posted_hdpi)
			    .setContentTitle("You are parked on floor " + recentData.parkedFloor)
			    .setContentText("You parked there on: " + recentData.parkedDateString);
			    //.setNumber(9.5);
		
		Intent resultIntent = new Intent(context, HistoryActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		
		//mBuilder.setContentText("Modified")
        //.setNumber(2);
	    // Because the ID remains unchanged, the existing notification is
	    // updated.
		mNotifyMgr.notify(
				FLOOR_NOTIFICATION_ID,
				mBuilder.build());
	}
	*/
	
	public void daemonNotification() {
		//update flag for display of system status
		notifyMainActivity(NOTIFICATION_TAG, IDLE_MESSAGE);
		
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
		mNotifyMgr.notify(DAEMON_NOTIFICATION_ID, mBuilder.build());
	}
	
	public void cancelNotification(int notificationLabel) {
		
		//delete notification http://developer.android.com/reference/android/app/NotificationManager.html#cancel(int)
		NotificationManager mNotifyMgr = 
		        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.cancel(notificationLabel);
	}
	
	public void cancelSensorNotification() {
		notifyMainActivity(NOTIFICATION_TAG, IDLE_MESSAGE);
		cancelNotification(SENSOR_NOTIFICATION_ID);
	}
	
	public void cancelGPSNotification() {
		notifyMainActivity(NOTIFICATION_TAG, IDLE_MESSAGE);
		cancelNotification(GPS_NOTIFICATION_ID);
	}
	
	public void cancelFloorNotification() {
		cancelNotification(FLOOR_NOTIFICATION_ID);
	}
	
	public void notifyMainActivity(String updateTag, String updateStatus) {
		//If user doesn't have a bluetooth device, change the idle message to let them know 
		//they need to manually activate it.
		if(updateStatus.equals(IDLE_MESSAGE) && !mySettings.isBluetoothUser)
			updateStatus = MANUAL_IDLE_MESSAGE;
		
		Intent intent = new Intent(updateTag);
		// Include data & label with the intent we send
		intent.putExtra("updateStatus", updateStatus);
		 
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

}

