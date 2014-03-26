package com.ethanai.parkinggarageapp;

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

import com.ethanai.parkinggarageapp.UserSettings;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest.Builder;


public class GarageSettingsActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
	public static UserSettings mySettings = MainActivity.mySettings; 
    public static RecentSensorData recentData = MainActivity.recentData;
	
    public ListView listView;
    public ArrayAdapter<GarageLocation> adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garagesettings);   
                     
        adapter = new ArrayAdapter<GarageLocation>(this, R.layout.settings_list_item, mySettings.enabledGarageLocations);

        listView = (ListView) findViewById(R.id.listview); // get the field for the listview within the overall layout
        listView.setAdapter(adapter);
 
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        
        addAds();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	updateListView();
    }

    
	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		if(mySettings.isDebug) {
			GarageLocation garageLocation = (GarageLocation) adapterView.getItemAtPosition(position);
			String garageName = garageLocation.name;
			
			Intent intent = new Intent(GarageSettingsActivity.this, FloorBorderActivity.class);
			//intent.putExtra("floorBorders", (Serializable) garageLocation.floors);
			intent.putExtra("garageName", garageName);
			startActivity(intent);
		}
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

	        	   mySettings.enabledGarageLocations.remove(garageLocation); //remove from enabled list only
	        	   mySettings.saveSettings();
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
	
	public void updateListView() {
		adapter.notifyDataSetChanged();
	}
	
	public void addGarageFromPresets(View view) {
		//make checkbox dialog (can I handle a lot? Scroll?)
		final ArrayList<Integer> checkedNames = new ArrayList<Integer>();
		final ArrayList<GarageLocation> unusedGarages = new ArrayList<GarageLocation>();
    	
    	ArrayList<String> garageNames = new ArrayList<String>(); //make list of all unused garages
    	for(GarageLocation garageLocation : mySettings.allGarageLocations) {
    		if(!mySettings.enabledGarageLocations.contains(garageLocation)) {
    			garageNames.add(garageLocation.name);
    			unusedGarages.add(garageLocation);
    		}
    	}
    	
    	if(garageNames.size() > 0) {
	    	CharSequence[] namesArray = garageNames.toArray(new CharSequence[garageNames.size()]);
	    	
			AlertDialog.Builder builder = 
					new AlertDialog.Builder(this)
					.setTitle("Select any preset garages to enable")
					.setMultiChoiceItems(namesArray, null, new DialogInterface.OnMultiChoiceClickListener() {
			               @Override
			               public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			                   if (isChecked) {
			                       // If the user checked the item, add it to the selected items
			                	   checkedNames.add(which);
			                   } else if (checkedNames.contains(which)) {
			                       // Else, if the item is already in the array, remove it 
			                	   checkedNames.remove(Integer.valueOf(which));
			                   }
			               }
			           })
			        // Set the action buttons
			        .setPositiveButton("Add These", new DialogInterface.OnClickListener() {
			        	@Override
			            public void onClick(DialogInterface dialog, int id) {
			        		for(int i : checkedNames) {
			        			mySettings.enabledGarageLocations.add(unusedGarages.get(i));
			                }
		        			mySettings.saveSettings();
			        		updateListView();
			            }
			        });
			builder.create();
			builder.show();	
			//update view
    	} else {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You already are using all garage presets")
                   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {

                       }
                   });
            builder.create();
            builder.show();
    	}		
	}
	
	public void removeGarageFromPresets(View view) {
		final ArrayList<Integer> checkedNames = new ArrayList<Integer>();
    	
    	ArrayList<String> garageNames = new ArrayList<String>(); //make list of all unused garages
    	for(GarageLocation garageLocation : mySettings.enabledGarageLocations) {
			garageNames.add(garageLocation.name);
    	}
    	
    	if(garageNames.size() > 0) {
	    	CharSequence[] namesArray = garageNames.toArray(new CharSequence[garageNames.size()]);
	    	
			AlertDialog.Builder builder = 
					new AlertDialog.Builder(this)
					.setTitle("Select garages to remove")
					.setMultiChoiceItems(namesArray, null, new DialogInterface.OnMultiChoiceClickListener() {
			               @Override
			               public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			                   if (isChecked) {
			                       // If the user checked the item, add it to the selected items
			                	   checkedNames.add(which);
			                   } else if (checkedNames.contains(which)) {
			                       // Else, if the item is already in the array, remove it 
			                	   checkedNames.remove(Integer.valueOf(which));
			                   }
			               }
			           })
			           // Set the action buttons
			           .setPositiveButton("Remove These", new DialogInterface.OnClickListener() {
			               @Override
			               public void onClick(DialogInterface dialog, int id) {
			            	   ArrayList<GarageLocation> toRemoveGarages = new ArrayList<GarageLocation>();
			                   for(int i : checkedNames) {
			                	   toRemoveGarages.add(mySettings.enabledGarageLocations.get(i));
			                   }
			                   
			                   for(GarageLocation garageLocation : toRemoveGarages) {
			                	   mySettings.enabledGarageLocations.remove(garageLocation);
			                   }
			                   mySettings.saveSettings();
			                   updateListView();
			               }
			           });
			builder.create();
			builder.show();	
			//update view
    	} else {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You don't have any garages set")
                   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {

                       }
                   });
            builder.create();
            builder.show();
    	}
		
	}
	
	public void toFloorMapper(View view) { //String garageName) {
		//start new activity. Pass garageName
		//let it build new garageLocation and add it
		Intent intent = new Intent(GarageSettingsActivity.this, FloorMapperActivity.class);
		//intent.putExtra("garageName", garageName);
		startActivity(intent);
		//update view
	}
    
    public void addAds() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        Builder adBuilder = new AdRequest.Builder();
        adBuilder.addTestDevice("2EF171E12F703640E851B84E5314ED51");
        AdRequest adRequest = adBuilder.build();
        adView.loadAd(adRequest);
	}
	
	public void finish(View view) {
		finish();
	}
}

/*
public void editGarageProfile(View view) {
	Toast.makeText(getApplicationContext(), "Not Implemented in Alpha", Toast.LENGTH_SHORT).show();

	
	dialogInstructUser();
	//dialog to select garage name
	ArrayList<String> garageNames = new ArrayList<String>();
	for(GarageLocation garageLocation : mySettings.enabledGarageLocations) {
		garageNames.add(garageLocation.name);
	}
	
	CharSequence[] namesArray = garageNames.toArray(new CharSequence[garageNames.size()]);
	
	AlertDialog ad = 
			new AlertDialog.Builder(this)
			.setTitle("Profile to Remake")
			.setItems(namesArray, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {   
		               makeNewGarageLocation(mySettings.enabledGarageLocations.get(which).name);
	           }
	       }).create();
	ad.show();	
	
}
*/

/*s
public void dialogGetFloor() {
	AlertDialog.Builder startBuilder = new AlertDialog.Builder(this);
	startBuilder.setMessage("Hit Next when you are on floor: " + floorNumber)
           .setPositiveButton("Save", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
            	   //get turncount and save it
            	   DataAnalyzer dataAnalyzer = new DataAnalyzer(recentData);
            	   float turnCount = dataAnalyzer.getConsecutiveTurns();
            	   if(mySettings.addFloorRecord(newGarageName, Integer.toString(floorNumber), turnCount)) {
       	    			Toast.makeText(getBaseContext(), "addfloor Located:" + newGarageName + "\n" 
       	    				+ floorNumber + " " + turnCount, Toast.LENGTH_SHORT).show();
       	    			floorNumber++;
            	   } else {	    	
            		   Toast.makeText(getBaseContext(), "Enter a number and set up garage first", Toast.LENGTH_SHORT).show();
            	   }
            	   dialogGetFloor();                	  
               }
           })
           .setNegativeButton("Finish", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {

               }
           });
	startBuilder.create();
	startBuilder.show();
	
}

public void dialogEnterGarageName() {	
	//dialog to select text //currently allow redoing any of them
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    // Get the layout inflater
    LayoutInflater inflater = this.getLayoutInflater();

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    builder.setView(inflater.inflate(R.layout.dialog_get_garage_name, null))
    		.setTitle("Enter Garage Name")
    		// Add action buttons
           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {
                   EditText garageNameField = (EditText) findViewById(R.id.input_garage_name);
                   SettingsActivity.this.newGarageName = garageNameField.toString();
               }
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
            	   stopSensors();
               }
           });      
    builder.create();
    builder.show();
	//driveawaystart dialog
	//recursive 'floor record' dialog
	//update view
}

public void dialogSelectGarageName() {
	//dialog to select garage name
	ArrayList<String> garageNames = new ArrayList<String>();
	for(GarageLocation garageLocation : UserSettings.enabledGarageLocations) {
		garageNames.add(garageLocation.name);
	}
	
	CharSequence[] namesArray = garageNames.toArray(new CharSequence[garageNames.size()]);
	
	AlertDialog ad = 
			new AlertDialog.Builder(this)
			.setTitle("Profile to Remake")
			.setItems(namesArray, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {   
		               newGarageName = UserSettings.enabledGarageLocations.get(which).name;
	           }
	       }).create();
	ad.show();		
}

public void dialogInstructUser() {
	//instruct user
	AlertDialog.Builder startBuilder = new AlertDialog.Builder(this);
	startBuilder.setMessage("You must start profiling approx. 1/2 mile away from the garage. Press start when ready.")
           .setPositiveButton("Start", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
            	   SettingsActivity.this.forceStartSensors();
            	   if(null == newGarageName)
            		   dialogEnterGarageName();
            	   dialogGetFloor();
            	   if(null != newGarageName && null != newLocation && null != newFloors) {
                	   UserSettings.allGarageLocations.add(mySettings.new GarageLocation(newGarageName, newLocation, newFloors));
                	   UserSettings.enabledGarageLocations.add(mySettings.new GarageLocation(newGarageName, newLocation, newFloors));
            	   } else {
            		   Toast.makeText(getApplicationContext(), "Missing Data. No entry created.", 
            				   Toast.LENGTH_SHORT).show();
            	   }
               }
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {

               }
           });
	startBuilder.create();
	startBuilder.show();
}
*/