package com.ethanai.parkinggarageapp;

import java.util.ArrayList;
import java.util.Arrays;

import android.location.Location;

public class PresetGarages {
	private static ArrayList<GarageLocation> presetGarageLocations = new ArrayList<GarageLocation>();
	
	public static ArrayList<GarageLocation> getPresetGarages() {
		String name = "CCV6";
		Location location = new Location(name);
		location.setLatitude(21.3474357); 
		location.setLongitude(-157.9035183); 
		PhoneLocation phoneLocation = new PhoneLocation(location, null);
		ArrayList<Floor> borders = new ArrayList<Floor>(
				Arrays.asList(
						new Floor(2, -1, "Low?"),
						new Floor(0, 1, "1"),
						new Floor(-2, 2, "2"),
						new Floor(-4, 2.5f, "2B"),
						new Floor(-6, 3, "3"),
						new Floor(-8, 3.5f, "3B"),
						new Floor(-10, 4, "4"),
						new Floor(-12, 99, "High?")
						)); 
		//form new Location and add it
		presetGarageLocations.add(new GarageLocation(name, phoneLocation, borders));		
		
		name = "UH Lot 20";
		location = new Location(name);
		location.setLatitude(21.295819); 
		location.setLongitude(-157.818232); 
		//21.2930909	-157.8171503
		phoneLocation = new PhoneLocation(location, null);
		borders = new ArrayList<Floor>(
				Arrays.asList(
						new Floor(5, 3, "3L"),
						new Floor(3, 2, "2L"),
						new Floor(1, 1, "1L"),
						new Floor(-1, 1, "1R"),
						new Floor(-3, 2, "1R Looping?")
						)); 
		
		//form new Location and add it		
		name = "TestGarage";
		location = new Location(name);
		location.setLatitude(21.29871750); 
		location.setLongitude(-157.82012939); 
		phoneLocation = new PhoneLocation(location, null);
		
		borders = new ArrayList<Floor>(); 
		
		return presetGarageLocations;
	}
}
