package com.ethanai.parkinggarageapp;

import java.util.ArrayList;

import com.ethanai.parkinggarageapp.UserSettings.FloorBorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FloorBorderActivity extends Activity {

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_border);   //get the overall layout

        Intent intent = getIntent();
        //GarageLocation garageLocation = (GarageLocation) intent.getSerializableExtra("floorBorders");
        //ArrayList<FloorBorder> listBorders = garageLocation.floorBorders;
        @SuppressWarnings("unchecked")
		ArrayList<FloorBorder> listBorders = (ArrayList<FloorBorder>) intent.getSerializableExtra("floorBorders");

        ArrayAdapter<FloorBorder> adapter 
        	= new ArrayAdapter<FloorBorder>(this, R.layout.floor_border_list_item, listBorders);

        ListView listView = (ListView) findViewById(R.id.listview); // get the field for the listview within the overall layout
        listView.setAdapter(adapter);      
        
    }	
}

