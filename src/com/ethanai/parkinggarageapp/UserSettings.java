package com.ethanai.parkinggarageapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.ethanai.parkinggarageapp.dataStructures.Floor;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

public class UserSettings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8023067763707780880L;
	
	public boolean isDebug = true; //for me to autoswitch on dev settings
	
	public ArrayList<GarageLocation> enabledGarageLocations = new ArrayList<GarageLocation>();
	public ArrayList<GarageLocation> allGarageLocations = new ArrayList<GarageLocation>();
	public ArrayList<GarageLocation> userAddedGarageLocations = new ArrayList<GarageLocation>();
	public ArrayList<GarageLocation> presetGarageLocations = new ArrayList<GarageLocation>();


	
	public String carBTName;
	public String carBTMac;
	
	public boolean isFirstRun;
	public boolean isBluetoothUser;
	public boolean isGarageSelectionComplete;
	
	public ParkingRecord parkingRecordRecent;
	

	public int recentDataHistoryCount = 2000; //safe default if loading has trouble
	public int graphHistoryCount;
	public int FLOOR_COLUMN_INDEX;
	
	public String STORAGE_DIRECTORY_NAME = "AppGarageParking";
	public String DATABASE_DIRECTORY_NAME = "AppGarageParking/ResourceData";
	
	public String SETTINGS_FILE_NAME;
	public String PRESETS_FILE_NAME;
	public String CUSTOM_GARAGE_FILE_NAME;
	public File userSettingsFile;
	public File userCustomGaragesFile;
	//public File presetGaragesFile;
	//public static final String GARAGE_LOG_NAME = "garageRecords.ser";
	//public static File garageLocationFile = new File(Environment.getExternalStorageDirectory().toString() 
	//		+ "/" + STORAGE_DIRECTORY_NAME + "/" + GARAGE_LOG_NAME);
	
	UserSettings() {	
		SETTINGS_FILE_NAME 		= "_parkingAppSettings.ser";
		PRESETS_FILE_NAME 		= "_presetGarages.ser";
		CUSTOM_GARAGE_FILE_NAME = "_customGarages.ser";
		
		userSettingsFile = new File(Environment.getExternalStorageDirectory().toString() 
						+ "/" + STORAGE_DIRECTORY_NAME + "/" + SETTINGS_FILE_NAME);
		userCustomGaragesFile = new File(Environment.getExternalStorageDirectory().toString() 
				+ "/" + STORAGE_DIRECTORY_NAME + "/" + CUSTOM_GARAGE_FILE_NAME);
		
		//presetGaragesFile = new File(Environment.getExternalStorageDirectory().toString() 
		//		+ "/" + DATABASE_DIRECTORY_NAME + "/" + PRESETS_FILE_NAME);
		
		//create Directory
		createDirectory(STORAGE_DIRECTORY_NAME);
		//createDirectory(DATABASE_DIRECTORY_NAME);
		
		//load settings from storage
		loadSettings();
		
		//for testing
		//resetSettings();
	}
	
	//temporary hard code, need to be able to add
	/*
	public void setBluetoothRecord() {
		carBTName = "XPLOD";
		carBTMac = "54:42:49:B0:7A:C6";
	}
	*/
	
	//for testing
	public void resetGeneralSettings() {
		//copy values into this()
		allGarageLocations	 	= new ArrayList<GarageLocation>();
		presetGarageLocations 	= new ArrayList<GarageLocation>();
		enabledGarageLocations 	= new ArrayList<GarageLocation>();
		parkingRecordRecent 	= null;
		
		carBTName = null;
		carBTMac = null;
		
		isFirstRun = true;
		isBluetoothUser = false;
		isGarageSelectionComplete = false;
		
		recentDataHistoryCount = 2000;
		graphHistoryCount = 2000;
		FLOOR_COLUMN_INDEX = 3;
		
		Log.i("UserSettings", "Non-garage settings reset to install defaults");
	}
	
	public void restoreDebugSettings() {
		//RecentSensorData recentData = MainActivity.recentData;
		allGarageLocations = new ArrayList<GarageLocation>();
		
		recentDataHistoryCount = 2000;
		graphHistoryCount = 2000;
		FLOOR_COLUMN_INDEX = 3;
		
		carBTName = null;
		carBTMac = null;
		
	    isFirstRun = true;
		isBluetoothUser = false;
		isGarageSelectionComplete = false;
		
		loadPresetGarages();
		if(null != presetGarageLocations) {
			for(GarageLocation garageLocation : presetGarageLocations)
				enabledGarageLocations.add(garageLocation);
		} else {
		}
		
		saveSettings();
		//savePresetGarages();
	}
	
	//saves everything except presets. That should never be altered at runtime
	public void saveSettings() {
		try {
			//save general settings
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(userSettingsFile)); //overwrite old file
			os.writeObject(this);
			os.close();
			
			//save user's custom garages
			os = new ObjectOutputStream(new FileOutputStream(userCustomGaragesFile)); //overwrite old file
			os.writeObject(userAddedGarageLocations);
			os.close();
			
		} catch (Exception e) {
			Log.e("UserSettings", e.toString());
		}
	}
	
	/*
	private void savePresetGarages() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(presetGaragesFile)); //overwrite old file
			os.writeObject(enabledGarageLocations);
			os.close();
		} catch (Exception e) {
			Log.e("UserSettings", e.toString());
		}
	}
	*/
	
	public void loadSettings() {
		loadGeneralSettings();
		loadPresetGarages();
		loadCustomGarages();
	}

	//Load the general settings
	public void loadGeneralSettings() {
		UserSettings loadedSettings = null;

		if(userSettingsFile != null && userSettingsFile.exists()) {
			try {
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(userSettingsFile));
				loadedSettings = (UserSettings) is.readObject();
				is.close();
			} catch (Exception e) {
				Log.e("UserSettings", e.getMessage());
			}
						
			if(null != loadedSettings) {
				//copy values into this()
				if(null != loadedSettings.enabledGarageLocations)
					enabledGarageLocations 	= loadedSettings.enabledGarageLocations;
				if(null != loadedSettings.parkingRecordRecent)
					parkingRecordRecent 	= loadedSettings.parkingRecordRecent;
				
				carBTName = loadedSettings.carBTName;
				carBTMac = loadedSettings.carBTMac;
				
				isFirstRun = loadedSettings.isFirstRun;
				isBluetoothUser = loadedSettings.isBluetoothUser;
				isGarageSelectionComplete = loadedSettings.isGarageSelectionComplete;
				
				if(loadedSettings.recentDataHistoryCount > 100)
					recentDataHistoryCount 	= loadedSettings.recentDataHistoryCount;
				else {
					recentDataHistoryCount = 2000;
					Log.i("UserSettings", "History Count too low. Forcefully corrected. " 
							+ loadedSettings.recentDataHistoryCount);
				}
				graphHistoryCount 		= loadedSettings.graphHistoryCount;
				FLOOR_COLUMN_INDEX 		= loadedSettings.FLOOR_COLUMN_INDEX;
				
				//STORAGE_DIRECTORY_NAME = loadedSettings.STORAGE_DIRECTORY_NAME;
				//SETTINGS_FILE_NAME = loadedSettings.SETTINGS_FILE_NAME;
				//userSettingsFile = loadedSettings.userSettingsFile;
				
				Log.i("UserSettings", "Basic settings loaded");
			} else {
				Log.e("UserSettings", "Basic settings failed to load");
			}
			
		} else {
			resetGeneralSettings(); //deletes EVERYTHING be sure to do this before trying to load custom garages or database garages
		}
	}
		
	@SuppressWarnings("unchecked")
	public void loadCustomGarages() {
		//load user's custom garages, add to the array of all garages
		if(userCustomGaragesFile != null && userCustomGaragesFile.exists()) {
			try {
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(userCustomGaragesFile));
				userAddedGarageLocations = (ArrayList<GarageLocation>) is.readObject();
				is.close();
				
				if(null != userAddedGarageLocations) {
					for(GarageLocation garageLocation : userAddedGarageLocations)
						allGarageLocations.add(garageLocation);
					
					Log.i("UserSettings", "Custom garages loaded " + userAddedGarageLocations.size() + " " + allGarageLocations.size());
				} else {
					Log.e("UserSettings", "Custom garages failed to load");
				}
			} catch (Exception e) {
				Log.e("UserSettings", e.getMessage());
			}
		}
	}
	
	public void loadPresetGarages() {
		presetGarageLocations = PresetGarages.getPresetGarages();
		
		if(null != presetGarageLocations) {
			for(GarageLocation garageLocation : presetGarageLocations)
				allGarageLocations.add(garageLocation);
		
			Log.i("UserSettings", "preset garages loaded " + presetGarageLocations.size() + " " + allGarageLocations.size());
		} else {
			Log.e("UserSettings", "Preset garages failed to load");
		}
		
		/*//garages no longer stored in files
		//load official database of garages, add to the array of all garages
		if(presetGaragesFile != null && presetGaragesFile.exists()) {
			try {
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(presetGaragesFile));
				presetGarageLocations = (ArrayList<GarageLocation>) is.readObject();
				is.close();
				
				if(null != presetGarageLocations) {
					for(GarageLocation garageLocation : presetGarageLocations)
						allGarageLocations.add(garageLocation);
				
					Log.i("UserSettings", "preset garages loaded " + presetGarageLocations.size() + " " + allGarageLocations.size());
				} else {
					Log.e("UserSettings", "Preset garages failed to load");
				}
			} catch (Exception e) {
				Log.e("UserSettings", e.getMessage());
			}
		}
		*/
		
	}
	
	/*
	 * Assert: all garage locations are stored to disk and loaded to allGarageLocations identically
	 * We just add the new one to our array and overwrite the local file
	 */
	// good info on serilizable limitations http://www.javacodegeeks.com/2013/03/serialization-in-java.html
	/*//garage locations more complicated now (preset and user defined separated) use individual methods for adding
	  //to the proper one(s).
	public void addGarageLocation(String name, PhoneLocation phoneLocation, ArrayList<Floor> borders) {
		allGarageLocations.add(new GarageLocation(name, phoneLocation, borders));
		saveSettings();
	}
	
	public void removeGarageLocation(GarageLocation garageLocation) {
		allGarageLocations.remove(garageLocation);
		saveSettings();
	}
	*/

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
	
	public GarageLocation getGarageLocation(String searchName) {
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
	
	
	public ArrayList<String> toArrayList() {
		ArrayList<String> settingData = new ArrayList<String>();
		for(GarageLocation garageLocation : allGarageLocations) {
			String text = "";
			text += garageLocation.name + " " + garageLocation.phoneLocation.getLatitude() + " " 
					+ garageLocation.phoneLocation.getLongitude();
			settingData.add(text);
		}
		return settingData;
	}
	
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read
    * http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
    * */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    
    public void createDirectory(String directoryName) {
        String sdCard = Environment.getExternalStorageDirectory().toString(); //get root of external storage
        File dir = new File(sdCard, directoryName);
        if (!dir.exists()) { //make directory if it doesnt exist
            dir.mkdirs();  //make all parent directories even.
        }
    }
    
	
	
	
	

	
	
	
	class ParkingRecord implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -780668416286718326L;
		public String dateString;
		public double latitude;
		public double longitude;
		public String locationName;
		public String floorName;
		public String sourceFileName;
		
		ParkingRecord(String newDateString, PhoneLocation newestPhoneLocation, String parkedFloor, File sourceFile) {
			dateString 		= newDateString;
			longitude 		= newestPhoneLocation.getLongitude();
			latitude 		= newestPhoneLocation.getLatitude();
			locationName 	= newestPhoneLocation.getLocationName();
			floorName 		= parkedFloor;
			sourceFileName 	= sourceFile.toString();
		}
		
		
		
		
		public String toString() {
			return dateString + ", " 
					+ latitude + " " + longitude + ", "
					+ locationName + ", "
					+ floorName + "," 
					+ sourceFileName + "\n";
		}
	}	
	
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
		this(newName, new PhoneLocation(location, null), newBorders);
	}
	
	/*
	public void delete() {
		removeGarageLocation(this);
	}
	*/
	
	public Floor getMatchingFloor(float correctedTurnCount) {
		Floor parkedFloor = null;
		
		//iterate through the floor borders until we find our first, minimum floor hit.
		//maybe we could do some cool hashmap thing here?
		if(null != floors && floors.size() > 0) {
			float difference = Math.abs(floors.get(0).turns - correctedTurnCount);
			parkedFloor = floors.get(0);
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


