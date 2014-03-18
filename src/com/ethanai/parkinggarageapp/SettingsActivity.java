package com.ethanai.parkinggarageapp;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import com.ethanai.parkinggarageapp.R;
import com.ethanai.parkinggarageapp.UserSettings;
import com.ethanai.parkinggarageapp.UserSettings.GarageLocation;


public class SettingsActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);   
             
        ArrayList<GarageLocation> listGarages = UserSettings.enabledGarageLocations;        
        ArrayAdapter<GarageLocation> adapter 
        	= new ArrayAdapter<GarageLocation>(this, R.layout.settings_list_item, listGarages);

        ListView listView = (ListView) findViewById(R.id.listview); // get the field for the listview within the overall layout
        listView.setAdapter(adapter);
 
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
    }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		GarageLocation garageLocation = (GarageLocation) adapterView.getItemAtPosition(position);
		
		Intent intent = new Intent(SettingsActivity.this, FloorBorderActivity.class);
		intent.putExtra("floorBorders", (Serializable) garageLocation.floors);
		startActivity(intent);
	}
	
	@Override
	public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long id) {
		//Toast.makeText(getApplicationContext(), "long click", Toast.LENGTH_SHORT).show(); 	    
 	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage("Confirm Delete")
	       .setTitle("Delete Record");
		
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	           @SuppressWarnings("unchecked")
			public void onClick(DialogInterface dialog, int id) {
	        	   GarageLocation garageLocation = (GarageLocation) adapterView.getItemAtPosition(position);
	        	   ArrayAdapter <GarageLocation> adapter = (ArrayAdapter<GarageLocation>) adapterView.getAdapter();

	        	   garageLocation.delete();
	        	   adapter.notifyDataSetChanged();
	        	   
	           }
	       });
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               //do nothing (returns)
	           }
	       });
		
		AlertDialog dialog = builder.create();
		dialog.show();
		return true; //consume the click
	}
	
	public void addGarageFromPresets(View view) {
		//make checkbox dialog (can I handle a lot? Scroll?)
		//update view
	}
	
	public void removeGarageFromPresets(View view) {
		//make checkbox dialog
		//update view
		
	}
	
	public void createNewGarage(View view) {
		//dialog to get text
		//buildGarageProfile (or similar)
		//update view
		
	}
	
	public void buildGarageProfile(View view) {
		//dialog to select text //currently allow redoing any of them
		//driveawaystart dialog
		//recursive 'floor record' dialog
		//update view
	}
	

}
