package com.ethanai.parkinggarageapp;

import java.util.ArrayList;

import com.ethanai.parkinggarageapp.dataStructures.Floor;

import android.location.Location;

public class PresetGarages {
	static UserSettings mySettings;
	
	private static ArrayList<GarageLocation> presetGarageLocations = new ArrayList<GarageLocation>();
	
	private static String homeTextData = "Country Club Village 6,21.3474357,-157.9035183,"
			+ "2, -1, Below 1,"
			+ "0, 1, 1,"
			+ "-2, 2, 2,"
			+ "-4, 2.5f, 2B,"
			+ "-6, 3, 3,"
			+ "-8, 3.5f, 3B,"
			+ "-10, 4, 4,"
			+ "-12, 4.5f, 4B,"
			+ "-14, 5, 5,"
			+ "-16, 5.5f, 5B,"
			+ "-18, 6, 6,"
			+ "-20, 6.5f, 6B,"
			+ "-22, 7, 7,"
			+ "-24, 7.5f, 7B"; 
	private static String uhWestTextData 	= "UHM Lot 20 West Entrance,21.29612203,-157.8197402,5, 3, 3L,3, 2, 2L,1, 1, 1L,-1, 1, 1R,-3, 2, 1R Loopingq";
	private static String uhCentralTextData = "UHM Lot 20 Central Entrance,21.29612203,-157.8197402,5, 3, 3L,3, 2, 2L,1, 1, 1L,-1, 1, 1R,-3, 2, 1R Loopingq";
	private static String uhEastTextData	= "UHM Lot 20 East Entrance,21.29612203,-157.8197402,5, 3, 3L,3, 2, 2L,1, 1, 1L,-1, 1, 1R,-3, 2, 1R Loopingq";
	private static String testTextData		= "TestGarage,21.29871750,-157.82012939";

	
	public static ArrayList<GarageLocation> getPresetGarages() {	
		if(null == MainActivity.mySettings)
			mySettings = DaemonReceiver.mySettings;
		else
			mySettings = MainActivity.mySettings;
		
		presetGarageLocations.add(createGarageLocation(homeTextData.split(",")));	
		
		if(null != mySettings && mySettings.isDebug) {
			presetGarageLocations.add(createGarageLocation(uhWestTextData.split(",")));	
			presetGarageLocations.add(createGarageLocation(uhCentralTextData.split(",")));
			presetGarageLocations.add(createGarageLocation(uhEastTextData.split(",")));
			presetGarageLocations.add(createGarageLocation(testTextData.split(",")));	
		}
		
		return presetGarageLocations;
	}
	
	public static GarageLocation createGarageLocation(String... textData) {
		String name = textData[0];
		float latitude = Float.parseFloat(textData[1]);
		float longitude = Float.parseFloat(textData[2]);
		
		
		Location location = new Location(name);
		location.setLatitude(latitude); 
		location.setLongitude(longitude); 
		PhoneLocation phoneLocation = new PhoneLocation(location, null);
		
		
		ArrayList<Floor> borders = new ArrayList<Floor>();
		for(int i = 3; textData.length > i; i = i + 3) {
			Floor newFloor = new Floor(Float.parseFloat(textData[i].trim()), 
					Float.parseFloat(textData[i+1].trim()), 
					textData[i+2]);
			borders.add(newFloor);
		}
				
		//form new Location and add it
		return new GarageLocation(name, phoneLocation, borders);
	}
	
	//for tester
	public static ArrayList<Floor> getHomeFloors() {
		String textData[] = homeTextData.split(",");
		ArrayList<Floor> borders = new ArrayList<Floor>();
		for(int i = 3; textData.length > i; i = i + 3) {
			Floor newFloor = new Floor(Float.parseFloat(textData[i].trim()), 
					Float.parseFloat(textData[i+1].trim()), 
					textData[i+2]);
			borders.add(newFloor);
		}
		return borders;
	}
}
