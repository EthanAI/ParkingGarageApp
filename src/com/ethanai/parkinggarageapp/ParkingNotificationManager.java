package com.ethanai.parkinggarageapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class ParkingNotificationManager {
	// Sets an ID for the notification
	final int RUN_STATE_NOTIFICATION_ID = 001;
	final int FLOOR_NOTIFICATION_ID 	= 002;
	
	Context context;
	RecentSensorData recentData;
	
	ParkingNotificationManager(Context newContext, RecentSensorData newData) {
		context = newContext;
		recentData = newData;
	}
	
	public void sensorRunningNotification() {
		//modify notification http://developer.android.com/training/notify-user/managing.html
		NotificationManager mNotifyMgr = 
		        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		NotificationCompat.Builder mBuilder =
			    new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.icon_notification_sensors_on_hdpi)
			    .setContentTitle("Sensors Running")
			    .setContentText("Recording possible parking actions");
		
		Intent resultIntent = new Intent(context, GraphActivity.class);
		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		//set click behavior
		mBuilder.setContentIntent(resultPendingIntent);
		
		//mBuilder.setContentText("Modified")
        //.setNumber(7);
	    // Because the ID remains unchanged, the existing notification is
	    // updated.
		mNotifyMgr.notify(RUN_STATE_NOTIFICATION_ID, mBuilder.build());
	}
	
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
	
	public void daemonNotification() {
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
	
	public void cancelNotification(int notificationLabel) {
		//delete notification http://developer.android.com/reference/android/app/NotificationManager.html#cancel(int)
		NotificationManager mNotifyMgr = 
		        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.cancel(notificationLabel);;
	}
	
	public void cancelRunStateNotification() {
		cancelNotification(RUN_STATE_NOTIFICATION_ID);
	}
	
	public void cancelFloorNotification() {
		cancelNotification(FLOOR_NOTIFICATION_ID);
	}

}

