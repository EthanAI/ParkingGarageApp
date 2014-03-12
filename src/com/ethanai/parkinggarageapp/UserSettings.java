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
	public static int recentDataHistoryCount = 2000;
	public static int graphHistoryCount = 2000;
	public static final int FLOOR_COLUMN_INDEX = 3;
	
	public final static String STORAGE_DIRECTORY_NAME = "Documents";
	public static final String GARAGE_LOG_NAME = "garageRecords.ser";
	public static File garageLocationFile = new File(Environment.getExternalStorageDirectory().toString() 
			+ "/" + STORAGE_DIRECTORY_NAME + "/" + GARAGE_LOG_NAME);

	
	UserSettings() {	
		//load garage settings from storage
		allGarageLocations = loadGarageLocations();
		
		//debugMakeLocations();
	}
	
	//for testing
	public void resetGarageLocations() {
		RecentSensorData recentData = new RecentSensorData();
		allGarageLocations = new ArrayList<GarageLocation>();
		
		//temp hardcode to add extra data
		String name = "Home";
		Location location = new Location(name);
		location.setLatitude(21.3474357); 
		location.setLongitude(-157.9035183); 
		PhoneLocation phoneLocation = recentData.new PhoneLocation(location);
		ArrayList<FloorBorder> borders = new ArrayList<FloorBorder>(
				Arrays.asList(
						new FloorBorder(-1, -1, "Low?"),
						new FloorBorder(1, 1, "1 Def"),
						new FloorBorder(3, 2, "2"),
						new FloorBorder(5, 2.5f, "2B"),
						new FloorBorder(7, 3, "3"),
						new FloorBorder(9, 3.5f, "3B"),
						new FloorBorder(11, 4, "4"),
						new FloorBorder(13, 99, "High?")
						)); 
		//form new Location and add it
		addGarageLocation(name, phoneLocation, borders);
		
		name = "UH Lot 20";
		location = new Location(name);
		location.setLatitude(21.295819); 
		location.setLongitude(-157.818232); 
		//21.2930909	-157.8171503
		phoneLocation = recentData.new PhoneLocation(location);
		borders = new ArrayList<FloorBorder>(
				Arrays.asList(
						new FloorBorder(-5, 3, "3L"),
						new FloorBorder(-3, 2, "2L"),
						new FloorBorder(-1, 1, "1L"),
						new FloorBorder(1, 1, "1R"),
						new FloorBorder(3, 2, "1R Looping?")
						)); 
		//form new Location and add it
		addGarageLocation(name, phoneLocation, borders);
		
		name = "TestGarage";
		location = new Location(name);
		location.setLatitude(21.29871750); 
		location.setLongitude(-157.82012939); 
		phoneLocation = recentData.new PhoneLocation(location);
		
		borders = new ArrayList<FloorBorder>(); 
		//form new Location and add it
		addGarageLocation(name, phoneLocation, borders);
	}
	
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
	
	public void saveSettings() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(garageLocationFile)); //overwrite old file
			os.writeObject(allGarageLocations);
			os.close();
		} catch (Exception e) {
			Log.e("UserSettings", e.toString());
		}
	}
	
	/*
	 * Assert: all garage locations are stored to disk and loaded to allGarageLocations identically
	 * We just add the new one to our array and overwrite the local file
	 */
	// good info on serilizable limitations http://www.javacodegeeks.com/2013/03/serialization-in-java.html
	public void addGarageLocation(String name, PhoneLocation phoneLocation, ArrayList<FloorBorder> borders) {
		allGarageLocations.add(new GarageLocation(name, phoneLocation, borders));
		saveSettings();
	}
	
	public void removeGarageLocation(GarageLocation garageLocation) {
		allGarageLocations.remove(garageLocation);
		saveSettings();
	}

	
	public void addFloorRecord(String garageName, String floorName, float turnCount) {
		GarageLocation editingGarage = getGarageLocation(garageName);
		int floorNumber = Integer.parseInt(floorName.split("[a-zA-Z]")[0]);
		FloorBorder newBorder = new FloorBorder(turnCount, floorNumber, floorName);
		editingGarage.floorBorders.add(newBorder); //might not be in order, we should do better deryption method
		
		saveSettings();
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
		public ArrayList<FloorBorder> floorBorders; //structure to hold all the borders between floors for this particular garages
		//add location address etc?
		
		GarageLocation(String newName, PhoneLocation newLocation, ArrayList<FloorBorder> newBorders) {
			name = newName;
			phoneLocation = newLocation;
			if(null == newBorders)
				floorBorders = new ArrayList<FloorBorder>();
			else
				floorBorders = newBorders;
		}	
		
		GarageLocation(String newName, Location location, ArrayList<FloorBorder> newBorders) {
			this(newName, new RecentSensorData().new PhoneLocation(location), newBorders);
		}
		
		public void delete() {
			removeGarageLocation(this);
		}
		
		public String toString() {
			return name + " " + phoneLocation.getLatitude() + " " + phoneLocation.getLongitude();
		}
	}
	
	class FloorBorder implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8248771055332604053L;

		public float maxTurns; //max number of quarter turns before crossing to the next floor positive is right, negative is left
		public float floorNum; //numerical representation of a floor
		public String floorString; //text representation of a floor
		
		FloorBorder (float turnCount, float floorNum, String floorString) {
			this.maxTurns = turnCount;
			this.floorNum = floorNum;
			this.floorString = floorString;
		}
		
		public String toString() {
			return maxTurns + ", " + floorNum + ", " + floorString;
		}
		
	}
	
}
