package com.ethanai.parkinggarageapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	public static final String MY_WIDGET_FILTER_ACTION_ID = "mywidget";
	
	public UserSettings mySettings; // = MainActivity.mySettings;

	@Override
		public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		}
	
	//http://stackoverflow.com/questions/20273543/appwidgetmanager-getappwidgetids-in-activity-returns-an-empty-list/22165711#22165711
	//didnt work for me, made my own implementation
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		if(null != MainActivity.mySettings) {
			mySettings = MainActivity.mySettings;
		}
		else {
			mySettings = DaemonReceiver.mySettings;
		}

		//update the text
		//ArrayList<String> floorHistory = getSortedRecent(readColumns(mySettings.FLOOR_COLUMN_INDEX, 1), 5);
		
		// Get all ids
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	    ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
	    for (int widgetId : allWidgetIds) {
	    	// Create an Intent to launch ExampleActivity
	        intent = new Intent(context, MainActivity.class);
	        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
	    	
	    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
	    	remoteViews.setOnClickPendingIntent(R.id.widgetField, pendingIntent);   	
	    	
	    	// Set the text
	    	if(null != mySettings && null != mySettings.parkingRecordRecent.floorName)//floorHistory.size() > 0)
	    		remoteViews.setTextViewText(R.id.widgetField,  mySettings.parkingRecordRecent.floorName);
	    	else
	    		remoteViews.setTextViewText(R.id.widgetField,  "None");
	    	
	        appWidgetManager.updateAppWidget(widgetId, remoteViews);
	    }
	    
	    /*
	  	// Register an onClickListener
	  	Intent newIntent = new Intent(context, MainActivity.class);

	  	newIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	  	newIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

	  	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	  	remoteViews.setOnClickPendingIntent(R.id.widgetField, pendingIntent);
	  	//appWidgetManager.updateAppWidget(widgetId, remoteViews);
	  	
	  	 
	  	
	  	
	  	Intent intent = new Intent(context, GraphActivity.class);
	  	intent.setAction(MY_WIDGET_FILTER_ACTION_ID);
	  	
	  	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
	  	// Get the layout for the App Widget and attach an on-click listener to the button
	  	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
	  	views.setOnClickPendingIntent(R.id.widgetField, pendingIntent);
	  	
	  	appWidgetManager.updateAppWidget(widgetId, remoteViews);
	  	*/

	}
	
	
	

}


/*
@Override
public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	
	
	ArrayList<String> floorHistory = getSortedRecent(readColumns(UserSettings.FLOOR_COLUMN_INDEX, 1), 5);

	// Get all ids
  ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
  int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
  for (int widgetId : allWidgetIds) {
  	// Create an Intent to launch ExampleActivity
      Intent intent = new Intent(context, HistoryActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
  	
  	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
  	remoteViews.setOnClickPendingIntent(R.id.widgetField, pendingIntent);   	
  	
  	// Set the text
  	if(floorHistory.size() > 0)
  		remoteViews.setTextViewText(R.id.widgetField,  floorHistory.get(0));
  	else
  		remoteViews.setTextViewText(R.id.widgetField,  "None");
  	
      appWidgetManager.updateAppWidget(widgetId, remoteViews);

  	// Register an onClickListener
  	Intent intent = new Intent(context, SettingsActivity.class);

  	intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
  	intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

  	PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  	remoteViews.setOnClickPendingIntent(R.id.widgetField, pendingIntent);
  	//appWidgetManager.updateAppWidget(widgetId, remoteViews);
  	
  	 
  	
  	
  	Intent intent = new Intent(context, GraphActivity.class);
  	intent.setAction(MY_WIDGET_FILTER_ACTION_ID);
  	
  	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
  	// Get the layout for the App Widget and attach an on-click listener to the button
  	RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
  	views.setOnClickPendingIntent(R.id.widgetField, pendingIntent);
  	
  	appWidgetManager.updateAppWidget(widgetId, remoteViews);
  	
  	//}
}
*/

/*
public ArrayList<String> getSortedRecent(ArrayList<String> rawList, int maxItems) {
	ArrayList<String> sortedArrayList = new ArrayList<String>();
	for(int i = 0; i <= maxItems && i < rawList.size(); i++) {
		int index = rawList.size() - 1 - i;
		sortedArrayList.add(rawList.get(index));
	}
	return sortedArrayList;
}

public ArrayList<String> readColumns(int targetColumnIndex, int headerRowCount) {
	ArrayList<String> arrayList = new ArrayList<String>();
	File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + mySettings.STORAGE_DIRECTORY_NAME 
			+ "/parkingLog.csv");
	if(null != file && file.exists()) {
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			for(int i = 0; i < headerRowCount; i++) //skip headers
				br.readLine();
			
			String line = "";
			while((line = br.readLine()) != null) {
				String entries[] = line.split(",");
				arrayList.add(entries[mySettings.FLOOR_COLUMN_INDEX]);
			}
			br.close();			
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	} else {
		arrayList.add("None");
	}
	return arrayList;
}
*/
