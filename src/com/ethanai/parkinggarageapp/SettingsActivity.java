package com.ethanai.parkinggarageapp;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.ethanai.parkinggarageapp.R;
import com.ethanai.parkinggarageapp.UserSettings;


public class SettingsActivity extends Activity implements OnItemClickListener {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);   //get the overall layout

        ArrayList<String> listStrings = UserSettings.toArrayList();        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.settings_list_item, listStrings);

        ListView listView = (ListView) findViewById(R.id.listview); // get the field for the listview within the overall layout
        listView.setAdapter(adapter);
 
        
    
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

}
