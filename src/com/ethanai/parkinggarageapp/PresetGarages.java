package com.ethanai.parkinggarageapp;

import java.util.ArrayList;

import android.location.Location;

public class PresetGarages {
	private static ArrayList<GarageLocation> presetGarageLocations = new ArrayList<GarageLocation>();
	
	public static ArrayList<GarageLocation> getPresetGarages() {
		String garageTextData;
		
		garageTextData = "CCV6,21.3474357,-157.9035183,"
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
		presetGarageLocations.add(createGarageLocation(garageTextData.split(",")));	
		
		garageTextData = "UH West Entrance,21.29612203,-157.8197402,5, 3, 3L,3, 2, 2L,1, 1, 1L,-1, 1, 1R,-3, 2, 1R Loopingq";
		presetGarageLocations.add(createGarageLocation(garageTextData.split(",")));	
		
		garageTextData = "UH Central Entrance,21.29612203,-157.8197402,5, 3, 3L,3, 2, 2L,1, 1, 1L,-1, 1, 1R,-3, 2, 1R Loopingq";
		presetGarageLocations.add(createGarageLocation(garageTextData.split(",")));
		
		garageTextData = "UH East Entrance,21.29612203,-157.8197402,5, 3, 3L,3, 2, 2L,1, 1, 1L,"
				+ "-1, 1, 1R,-3, 2, 1R Loopingq";
		presetGarageLocations.add(createGarageLocation(garageTextData.split(",")));

		garageTextData = "TestGarage,21.29871750,-157.82012939";
		presetGarageLocations.add(createGarageLocation(garageTextData.split(",")));	
		
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
}
