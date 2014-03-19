package com.ethanai.parkinggarageapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

//simple tutorial http://www.vogella.com/tutorials/AndroidListView/article.html
//possible future enhancements: https://github.com/thecodepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView

public class HistoryActivity extends Activity implements OnItemClickListener {

	ArrayList<String> adapterStrings;
	public UserSettings mySettings;

	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);   //get the overall layout
        
        mySettings = MainActivity.mySettings;

        ArrayList<String> listStrings = readLog(1);
        adapterStrings = getSortedRecent(listStrings, 10);
        
        //pass the layout for the individual items (kinda mundane but expandable in the future)
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.history_list_item, adapterStrings);

        ListView listView = (ListView) findViewById(R.id.listview); // get the field for the listview within the overall layout
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(this);        
    }
	
	public ArrayList<String> getSortedRecent(ArrayList<String> rawList, int maxItems) {
		ArrayList<String> sortedArrayList = new ArrayList<String>();
		for(int i = 0; i <= maxItems && i < rawList.size(); i++) {
			int index = rawList.size() - 1 - i;
			sortedArrayList.add(rawList.get(index));
		}
		return sortedArrayList;
	}
	
	public ArrayList<String> readLog(int headerRowCount) {
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
					arrayList.add(entries[mySettings.FLOOR_COLUMN_INDEX] + "\n" + entries[0] + "\n" + entries[1]
							+ "\n" + entries[2]);
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
	

	
    /*
     * Function for the button
     */
	/*
    public void changeToGraphActivity(View view) {
    	Intent intent = new Intent(HistoryActivity.this, GraphActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }
    */


	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		//Object o = lv.getItemAtPosition(position);
		String viewText = adapterStrings.get((int) id);
		String coordinates = viewText.split("\n")[2];
		Log.i("HistoryActivity", coordinates);
		try {
			//String address = addrText.getText().toString();
			//address = address.replace(' ', '+');
			
			Intent geoIntent = new Intent(
					android.content.Intent.ACTION_VIEW, Uri.parse(
							"geo:0,0?q=" + coordinates));
			startActivity(geoIntent);
		} catch (Exception e) {
			Log.e("HistoryActivity", e.toString());
		}
		
	}

	public void finish(View view) {
		finish();
	}

	
}
