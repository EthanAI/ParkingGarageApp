package com.ethanai.parkinggarageapp;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.ethanai.parkinggarageapp.R;
import com.ethanai.parkinggarageapp.UserSettings;
import com.ethanai.parkinggarageapp.UserSettings.GarageLocation;


public class SettingsActivity extends Activity implements OnItemClickListener {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);   //get the overall layout
        
        ArrayList<GarageLocation> listGarages = UserSettings.allGarageLocations;        
        ArrayAdapter<GarageLocation> adapter 
        	= new ArrayAdapter<GarageLocation>(this, R.layout.settings_list_item, listGarages);

        ListView listView = (ListView) findViewById(R.id.listview); // get the field for the listview within the overall layout
        listView.setAdapter(adapter);
 
        listView.setOnItemClickListener(this);
    }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		GarageLocation garageLocation = (GarageLocation) adapterView.getItemAtPosition(position);
		
		Intent intent = new Intent(SettingsActivity.this, FloorBorderActivity.class);
		intent.putExtra("floorBorders", (Serializable) garageLocation.floorBorders);
		startActivity(intent);
		
		
	}
}
