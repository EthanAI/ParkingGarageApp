package com.ethanai.parkinggarageapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import android.app.Activity;
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

//simple tutorial http://www.vogella.com/tutorials/AndroidListView/article.html
//possible future enhancements: https://github.com/thecodepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView

public class HistoryActivity extends Activity implements OnItemClickListener {

	//TODO list view. Adapters. Click to ...map it?
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);   //get the overall layout

        ArrayList<String> listStrings = readColumns(UserSettings.FLOOR_COLUMN_INDEX, 1);
        ArrayList<String> adapterStrings = getSortedRecent(listStrings, 10);
        
        //pass the layout for the individual items (kinda mundane but expandable in the future)
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.history_list_item, adapterStrings);

        ListView listView = (ListView) findViewById(R.id.listview); // get the field for the listview within the overall layout
        listView.setAdapter(adapter);
        
        //listView.setOnClickListener(this);
        
        /*
        //for debugging
        PackageManager packageManager = getPackageManager();
        
        List<String> startupApps = new ArrayList<String>();
        List<ResolveInfo> detail = new ArrayList<ResolveInfo>();
        Intent intent = new Intent("android.intent.action.BOOT_COMPLETED");
        List<ResolveInfo> activities = packageManager.queryBroadcastReceivers(intent, 0);
        for (ResolveInfo resolveInfo : activities) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                startupApps.add(activityInfo.name);
                detail.add(resolveInfo);
            }
        }
        System.out.println();
        */
        
        
    }
	
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
		File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + UserSettings.STORAGE_DIRECTORY_NAME 
				+ "/parkingLog.csv");
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			for(int i = 0; i < headerRowCount; i++) //skip headers
				br.readLine();
			
			String line = "";
			while((line = br.readLine()) != null) {
				String entries[] = line.split(",");
				arrayList.add(entries[UserSettings.FLOOR_COLUMN_INDEX] + "\n" + entries[0] + "\n" + entries[2]);
			}
			br.close();			
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return arrayList;
	}
	

	
    /*
     * Function for the button
     */
    public void changeToGraphActivity(View view) {
    	//Intent intent = new Intent(TextActivity.this, GraphActivity.class);
        //startActivity(intent);
        this.finish();
    }



	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
		// TODO Auto-generated method stub
		//Object o = lv.getItemAtPosition(position);
		
	}
	
	
}
