package com.ethanai.parkinggarageapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;

import com.ethanai.parkinggarageapp.RecentSensorData.PhoneLocation;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

public class UserSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1790572649218348232L;
	public static ArrayList<GarageLocation> allGarageLocations = new ArrayList<GarageLocation>();
	public static int recentDataHistoryCount;
	public static int graphHistoryCount;
	public static int FLOOR_COLUMN_INDEX;
	
	public static String STORAGE_DIRECTORY_NAME;
	public static String SETTINGS_FILE_NAME;
	public static File settingsFile;
	//public static final String GARAGE_LOG_NAME = "garageRecords.ser";
	//public static File garageLocationFile = new File(Environment.getExternalStorageDirectory().toString() 
	//		+ "/" + STORAGE_DIRECTORY_NAME + "/" + GARAGE_LOG_NAME);
	
	public static String carBTName;
	public static String carBTMac;
	public static ArrayList<String> nonCarBTMacArray = new ArrayList<String>();
	
	public static boolean isFirstRun;
	public static boolean isBluetoothUser;
	public static boolean isGarageSelectionComplete;

	
	UserSettings() {	
		//load settings from storage
		//loadSettings();
		
		//for testing
		resetSettings();
	}
	
	//temporary hard code, need to be able to add
	public void setBluetoothRecord() {
		carBTName = "XPLOD";
		carBTMac = "54:42:49:B0:7A:C6";
	}
	
	//for testing
	public void resetSettings() {
		RecentSensorData recentData = new RecentSensorData();
		allGarageLocations = new ArrayList<GarageLocation>();
		
		recentDataHistoryCount = 2000;
		graphHistoryCount = 2000;
		FLOOR_COLUMN_INDEX = 3;
		
		STORAGE_DIRECTORY_NAME = "Documents";
		SETTINGS_FILE_NAME = "_parkingGarageSettings.ser";
		settingsFile = new File(Environment.getExternalStorageDirectory().toString() 
						+ "/" + STORAGE_DIRECTORY_NAME + "/" + SETTINGS_FILE_NAME);
		
		carBTName = "";
		carBTMac = "";
		
	    isFirstRun = true;
		isBluetoothUser = false;
		isGarageSelectionComplete = false;
		
		//temp hardcode to add extra data
		String name = "Home";
		Location location = new Location(name);
		location.setLatitude(21.3474357); 
		location.setLongitude(-157.9035183); 
		PhoneLocation phoneLocation = recentData.new PhoneLocation(location);
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
		addGarageLocation(name, phoneLocation, borders);
		
		name = "UH Lot 20";
		location = new Location(name);
		location.setLatitude(21.295819); 
		location.setLongitude(-157.818232); 
		//21.2930909	-157.8171503
		phoneLocation = recentData.new PhoneLocation(location);
		borders = new ArrayList<Floor>(
				Arrays.asList(
						new Floor(5, 3, "3L"),
						new Floor(3, 2, "2L"),
						new Floor(1, 1, "1L"),
						new Floor(-1, 1, "1R"),
						new Floor(-3, 2, "1R Looping?")
						)); 
		//form new Location and add it
		addGarageLocation(name, phoneLocation, borders);
		
		name = "TestGarage";
		location = new Location(name);
		location.setLatitude(21.29871750); 
		location.setLongitude(-157.82012939); 
		phoneLocation = recentData.new PhoneLocation(location);
		
		borders = new ArrayList<Floor>(); 
		//form new Location and add it
		addGarageLocation(name, phoneLocation, borders);
	}

	/*
	@SuppressWarnings("unchecked")
	private ArrayList<GarageLocation> loadGarageLocations() {
		ArrayList<GarageLocation> garageLocations = new ArrayList<GarageLocation>();
		if(garageLocationFile != null && garageLocationFile.exists())
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(garageLocationFile));
			garageLocations = (ArrayList<GarageLocation>) is.readObject();
			is.close();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return garageLocations;
	}
	*/
	
	public void saveSettings() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(settingsFile)); //overwrite old file
			os.writeObject(this);
			os.close();
		} catch (Exception e) {
			Log.e("UserSettings", e.toString());
		}
	}
	
	/*
	public void loadSettings() {
		UserSettings loadedSettings = null;
		if(settingsFile != null && settingsFile.exists())
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(settingsFile));
			loadedSettings = (UserSettings) is.readObject();
			is.close();
		} catch (Exception e) {
			Log.e("UserSettings", e.getMessage());
		}
		
		//copy values into this()
		//TODO look up best way to restore this from file once we land
		allGarageLocations = loadedSettings.allGarageLocations;
		recentDataHistoryCount = loadedSettings.recentDataHistoryCount;
		graphHistoryCount = loadedSettings.graphHistoryCount;
		FLOOR_COLUMN_INDEX = loadedSettings.FLOOR_COLUMN_INDEX;
		
		STORAGE_DIRECTORY_NAME = loadedSettings.STORAGE_DIRECTORY_NAME;
		SETTINGS_FILE_NAME = loadedSettings.SETTINGS_FILE_NAME;
		settingsFile = loadedSettings.settingsFile;
		
		carBTName = loadedSettings.carBTName;
		carBTMac = loadedSettings.carBTMac;
		
		isFirstRun = loadedSettings.isFirstRun;
		isBluetoothUser = loadedSettings.isBluetoothUser;
		isGarageSelectionComplete = loadedSettings.isGarageSelectionComplete;
	}
	*/
	
	/*
	 * Assert: all garage locations are stored to disk and loaded to allGarageLocations identically
	 * We just add the new one to our array and overwrite the local file
	 */
	// good info on serilizable limitations http://www.javacodegeeks.com/2013/03/serialization-in-java.html
	public void addGarageLocation(String name, PhoneLocation phoneLocation, ArrayList<Floor> borders) {
		allGarageLocations.add(new GarageLocation(name, phoneLocation, borders));
		saveSettings();
	}
	
	public void removeGarageLocation(GarageLocation garageLocation) {
		allGarageLocations.remove(garageLocation);
		saveSettings();
	}

	//return sucess or fail
	public boolean addFloorRecord(String garageName, String floorName, float turnCount) {
		GarageLocation editingGarage = getGarageLocation(garageName);
		String tokens[] = floorName.split("[a-zA-Z]"); 
		if(null != editingGarage && floorName.matches("[0-9][a-zA-Z]*")) {
			float floorNumber = Integer.parseInt(tokens[0]);
			if(floorName.matches("[0-9][a-zA-Z]+")) { //if anything entered other than numbers, assume partial floor change
				floorNumber += 0.5;
			}
			Floor newBorder = new Floor(turnCount, floorNumber, floorName);
			editingGarage.floors.add(newBorder); //might not be in order, we should do better deryption method
			
			saveSettings();
			return true;
		} else {
			return false;
		}
	}
	
	public static GarageLocation getGarageLocation(String searchName) {
		GarageLocation returnValue = null;
		for(GarageLocation garageLocation : allGarageLocations) {
			if(garageLocation.name.equalsIgnoreCase(searchName)) {
				returnValue = garageLocation;
			}
		}
		
		/*
		//catch case just for testing
		if(returnValue == null) {
			returnValue = allGarageLocations.get(0);
		}
		*/
		
		return returnValue;
	}
	
	
	public static ArrayList<String> toArrayList() {
		ArrayList<String> settingData = new ArrayList<String>();
		for(GarageLocation garageLocation : allGarageLocations) {
			String text = "";
			text += garageLocation.name + " " + garageLocation.phoneLocation.getLatitude() + " " 
					+ garageLocation.phoneLocation.getLongitude();
			settingData.add(text);
		}
		return settingData;
	}
	
	class GarageLocation implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6855129699792130834L;
		public String name = "";
		public PhoneLocation phoneLocation;
		public ArrayList<Floor> floors; //structure to hold all the borders between floors for this particular garages
		//add location address etc?
		
		GarageLocation(String newName, PhoneLocation newLocation, ArrayList<Floor> newBorders) {
			name = newName;
			phoneLocation = newLocation;
			if(null == newBorders)
				floors = new ArrayList<Floor>();
			else
				floors = newBorders;
		}	
		
		GarageLocation(String newName, Location location, ArrayList<Floor> newBorders) {
			this(newName, new RecentSensorData().new PhoneLocation(location), newBorders);
		}
		
		public void delete() {
			removeGarageLocation(this);
		}
		
		public Floor getMatchingFloor(float correctedTurnCount) {
			Floor parkedFloor = null;
			
			//iterate through the floor borders until we find our first, minimum floor hit.
			//maybe we could do some cool hashmap thing here?
			if(null != floors && floors.size() > 0) {
				float difference = Math.abs(floors.get(0).turns - correctedTurnCount);
				for(Floor floor : floors) {
					if(Math.abs(floor.turns - correctedTurnCount) < difference) {
						difference = Math.abs(floor.turns - correctedTurnCount);
						parkedFloor = floor;
					}
				}
				return parkedFloor;
			} else {
				return null;
			}
		}
		
		public String toString() {
			return name + " " + phoneLocation.getLatitude() + " " + phoneLocation.getLongitude();
		}
	}
	
	class Floor implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8248771055332604053L;

		public float turns; //max number of quarter turns before crossing to the next floor positive is right, negative is left
		public float floorNum; //numerical representation of a floor
		public String floorString; //text representation of a floor
		
		Floor(float turnCount, float floorNum, String floorString) {
			this.turns = turnCount;
			this.floorNum = floorNum;
			this.floorString = floorString;
		}
		
		public String toString() {
			return turns + ", " + floorNum + ", " + floorString;
		}
		
	}
	
}
