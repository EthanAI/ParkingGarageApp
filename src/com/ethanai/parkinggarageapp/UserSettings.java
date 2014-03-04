package com.ethanai.parkinggarageapp;

import java.util.ArrayList;
import java.util.Arrays;

import android.location.Location;

public class UserSettings {
	public static ArrayList<UserLocation> allUserLocations = new ArrayList<UserLocation>();
	public static int recentDataHistoryCount = 2000;
	public static final int FLOOR_COLUMN_INDEX = 3;
	public final static String STORAGE_DIRECTORY_NAME = "Documents";
	
	UserSettings() {
		for(int i = 0; i < 1; i++) { //TODO iterate through all of stored persistent data
			//read some data
			//decode it
				//temporary hardcode
			String name = "Home";
			Location location = new Location(name);
			location.setLatitude(21.3474357); 
			location.setLongitude(-157.9035183); 
			ArrayList<FloorBorder> borders = new ArrayList<FloorBorder>(
					Arrays.asList(
							new FloorBorder(-1, -1, "Low?"),
							new FloorBorder(1, 1, "1 Default"),
							new FloorBorder(3, 2, "2"),
							new FloorBorder(5, 2.5f, "2B"),
							new FloorBorder(7, 3, "3"),
							new FloorBorder(9, 3.5f, "3B"),
							new FloorBorder(11, 4, "4"),
							new FloorBorder(13, 99, "High?")
							)); 
			//form new Location and add it
			allUserLocations.add(new UserLocation(name, location, borders));
			
			name = "UH Lot 20";
			location = new Location(name);
			location.setLatitude(21.295819); 
			location.setLongitude(-157.818232); 
			//21.2930909	-157.8171503

			borders = new ArrayList<FloorBorder>(
					Arrays.asList(
							new FloorBorder(-5, 3, "3L"),
							new FloorBorder(-3, 2, "2L"),
							new FloorBorder(-1, 1, "1L"),
							new FloorBorder(1, 1, "1R"),
							new FloorBorder(3, 2, "1R Looping?")
							)); 
			//form new Location and add it
			allUserLocations.add(new UserLocation(name, location, borders));
		}
	}	
	
	public void addUserLocation(String name, Location location, ArrayList<FloorBorder> borders) {
		allUserLocations.add(new UserLocation(name, location, borders));
	}
	
	public static UserLocation getUserLocation(String searchName) {
		UserLocation returnValue = null;
		for(UserLocation location : allUserLocations) {
			if(location.name.equalsIgnoreCase(searchName)) {
				returnValue = location;
			}
		}
		
		//catch case just for testing
		if(returnValue == null) 
			returnValue = allUserLocations.get(0);
		
		return returnValue;
	}
	
	public static ArrayList<String> toArrayList() {
		ArrayList<String> settingData = new ArrayList<String>();
		for(UserLocation userLocation : allUserLocations) {
			String text = "";
			text += userLocation.name + " " + userLocation.location.getLatitude() + " " + userLocation.location.getLatitude();
			settingData.add(text);
		}
		return settingData;
	}
	
	class UserLocation {
		public String name = "";
		public Location location;
		public ArrayList<FloorBorder> floorBorders; //structure to hold all the borders between floors for this particular garages
		//add location address etc?
		
		UserLocation(String newName, Location newLocation, ArrayList<FloorBorder> newBorders) {
			name = newName;
			location = newLocation;
			floorBorders = newBorders;
		}		
	}
	
	class FloorBorder {
		public int maxTurns; //max number of quarter turns before crossing to the next floor positive is right, negative is left
		public float floorNum; //numerical representation of a floor
		public String floorString; //text representation of a floor
		
		FloorBorder (int maxTurns, float floorNum, String floorString) {
			this.maxTurns = maxTurns;
			this.floorNum = floorNum;
			this.floorString = floorString;
		}
		
	}
	
}
