package com.ethanai.parkinggarageapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

//import com.ethanai.parkinggarageapp.RecentSensorData.DerivedOrientation;
import com.ethanai.parkinggarageapp.RecentSensorData.PhoneLocation;
import com.ethanai.parkinggarageapp.UserSettings.FloorBorder;
import com.ethanai.parkinggarageapp.UserSettings.UserLocation;

/*
 * Eventually I'd like to let the user drive in their structure to the top floor. Record the path. Use that as a pattern to match
 * For now, just let them select 'Right Turn structure' 'Left turn structure' or 'Split' and have split go to TBC screen
 */
public class DataAnalyzer {	
	ArrayList<Float> turnDegreesArray = new ArrayList<Float>();
	ArrayList<Double> latArray = new ArrayList<Double>();
	ArrayList<Double> longArray = new ArrayList<Double>();
	PhoneLocation currentPhoneLocation;
	
	DataAnalyzer(RecentSensorData recentData, PhoneLocation newCurrentPhoneLocation) { 
		for(int i = 0; i < recentData.orientRecent.size()-1; i++) {
			turnDegreesArray.add((float)recentData.orientRecent.get(i).totalTurnDegrees);
		}
		
		for(int i = 0; i < recentData.orientRecent.size() - 1; i++) {
			latArray.add(recentData.orientRecent.get(i).location.getLatitude());
			longArray.add(recentData.orientRecent.get(i).location.getLongitude());
		}
		
		currentPhoneLocation = newCurrentPhoneLocation;
	}

	/*
	DataAnalyzer(ArrayList<DerivedOrientation> orientRecent) { 
		for(int i = 0; i < orientRecent.size()-1; i++) {
			turnDegreesArray.add((float)orientRecent.get(i).totalTurnDegrees);
		}
	}
	*/
	
	//for development need to load data from stored example csv files
	public DataAnalyzer(File dataFile) {
		readFile(dataFile);
	}

	
	/*
	 * Doesn't hold the full dataset in memory so just an estimate
	 */
	public String getCurrentFloorEstimate(RecentSensorData recentData) {
		ArrayList<Float> arrayList = new ArrayList<Float>();
		for(int i = 0; i < recentData.orientRecent.size()-1; i++) {
			arrayList.add((float)recentData.orientRecent.get(i).totalTurnDegrees);
		}
		return getCurrentFloor(arrayList, recentData.newestPhoneLocation);
	}
	
	public String getCurrentFloorEstimate() {
		return getCurrentFloor();
	}
	
	/*
	public static String getCurrentFloorEstimate(ArrayList<DerivedOrientation> orientRecent) {
		ArrayList<Float> arrayList = new ArrayList<Float>();
		for(int i = 0; i < orientRecent.size()-1; i++) {
			arrayList.add((float)orientRecent.get(i).totalTurnDegrees);
		}
		PhoneLocation parkedPhoneLocation = new PhoneLocation(orientRecent.get(orientRecent.size() - 1).location);
		return getCurrentFloor(arrayList, parkedPhoneLocation);
	}
	*/
	
	/*
	 * Uses the data stored in the csv, most likely to be correct
	 */
	public String getCurrentFloorFinal(File dataFile) {
		readFile(dataFile);
		return getCurrentFloor();
	}
	
	
	public String getCurrentFloor(ArrayList<Float> turnDegreesArray, PhoneLocation... phoneLocation) {
		String currentLocationName;
		if(null != phoneLocation[0]) {
			PhoneLocation currentLocation = phoneLocation[0];
			currentLocationName = currentLocation.getLocationName();
		} else {
			currentLocationName = "Home"; //hardcode for offline testing. Code should not be used in the wild
		}
		UserLocation userLocation = UserSettings.getUserLocation(currentLocationName);
		ArrayList<FloorBorder> floorBorders = userLocation.floorBorders;
		
		String parkedFloor = "";
		float quarterTurnCount = getConsecutiveRightTurns();
		System.out.println("Raw right turns: " + quarterTurnCount);

		quarterTurnCount += fidgitingCorrection(); //incase we cant get the sensors to stop immediatly upon ignition stop (likely, if not a BT car person)
		quarterTurnCount += parkTurnCorrection();
		int roundedTurnCount = Math.round(quarterTurnCount);
		System.out.println("Corrected Right Turns: " + roundedTurnCount);
		
		//iterate through the floor borders until we find our first, minimum floor hit.
		for(FloorBorder border : floorBorders) {
			if(roundedTurnCount < border.maxTurns && parkedFloor == "") {
				parkedFloor = border.floorString;
			}
		}
		
		return parkedFloor;
	}
	
	public String getCurrentFloor() {
		String currentLocationName;
		if(null != currentPhoneLocation) {
			PhoneLocation currentLocation = currentPhoneLocation;
			currentLocationName = currentLocation.getLocationName();
		} else {
			currentLocationName = "Home"; //default 
		}
		UserLocation userLocation = UserSettings.getUserLocation(currentLocationName);
		ArrayList<FloorBorder> floorBorders = userLocation.floorBorders;
		
		String parkedFloor = "";
		float quarterTurnCount = getConsecutiveRightTurns();
		System.out.println("Raw right turns: " + quarterTurnCount);

		quarterTurnCount += fidgitingCorrection(); //incase we cant get the sensors to stop immediatly upon ignition stop (likely, if not a BT car person)
		quarterTurnCount += parkTurnCorrection();
		int roundedTurnCount = Math.round(quarterTurnCount);
		System.out.println("Corrected Right Turns: " + roundedTurnCount);
		
		//iterate through the floor borders until we find our first, minimum floor hit.
		for(FloorBorder border : floorBorders) {
			if(roundedTurnCount < border.maxTurns && parkedFloor == "") {
				parkedFloor = border.floorString;
			}
		}
		
		return parkedFloor;
	}
	
	//Important we have a long history. CSV has all, but recentData structure has less. Maybe 2k is enough
	//takes values as degrees, returns value as fraction of quarter turns
	/*  3/1/14 Adding guts so this effectively counts all turns
	 */
	
	public float getConsecutiveRightTurns() {
		//Time threshold - if we didnt turn after xxx readings ... maybe not good measure
		
		//Intensity threshold - work back from end, until we find a left turn
			//try with floating average
		int meanOffset = 10; //how far left and right to include in the smoothing averaging process
		float leftThreshold = 0.75f; //max left turn before we stop the count
		float leftConsecutiveCount = 0; //total amount turned left without going right
		float rightConsecutiveCount = 0; //total amount turned right without significant left
		//right is high. As we work back in time to 'unwind' we should be decreasing. This tracks how far the lowest point is so far before we find an inflextion point
		//adding more lefts
		float runningHighCount = getFloatingAverage(turnDegreesArray, meanOffset, turnDegreesArray.size()-1) / 90;
		float parkingEndCount = runningHighCount; //will need modified by the removeFidgit() method in future
		
		for(int i = turnDegreesArray.size() - 1; i > 0 && leftConsecutiveCount < leftThreshold; i--) {
			float floatingMeanDegrees = getFloatingAverage(turnDegreesArray, meanOffset, i);
			float quarterTurnCount = floatingMeanDegrees / 90; //net quarter turns according to sensors
			if(quarterTurnCount > runningHighCount) { //keep consistent with current sign convention
				runningHighCount = quarterTurnCount;
			}
			rightConsecutiveCount = runningHighCount - parkingEndCount;
			leftConsecutiveCount =  runningHighCount - quarterTurnCount;
			
			//System.out.println(i+3 + "\tleftCount "+ leftConsecutiveCount + "\trightCount " + rightConsecutiveCount + "\tparkingEndCount " + parkingEndCount + "\trunningLowCount " + runningLowCount);
			
		}
		return Math.abs(rightConsecutiveCount);
	}
	
	public ArrayList<TurnCount> getAllTurns() {
		int meanOffset = 10; //how far left and right to include in the smoothing averaging process
		
		ArrayList<TurnCount> turnHistory = new ArrayList<TurnCount>(); //hold the detected turns

		//variable to we compare against to see if we've competed more than one turn of a type
		float turnCountingCompareValue = getFloatingAverage(turnDegreesArray, meanOffset, turnDegreesArray.size() - 1);
		for(int i = turnDegreesArray.size() - 1; i > 0; i--) {
			float floatingMeanDegrees = getFloatingAverage(turnDegreesArray, meanOffset, i);
			float changeSinceLastTurnPush = floatingMeanDegrees - turnCountingCompareValue;
			if(changeSinceLastTurnPush > 90) {
				int consecutiveCount;
				if(turnHistory.size() == 0 || !turnHistory.get(turnHistory.size() - 1).direction.equalsIgnoreCase("Left"))
					consecutiveCount = 1;
				else 
					consecutiveCount = 1 + turnHistory.get(turnHistory.size() - 1).count;
				turnHistory.add(new TurnCount("Left", consecutiveCount, i, latArray.get(i), longArray.get(i)));
				turnCountingCompareValue += 90;
			} else if (changeSinceLastTurnPush < -90) {
				int consecutiveCount;
				if(turnHistory.size() == 0 || !turnHistory.get(turnHistory.size() - 1).direction.equalsIgnoreCase("Right"))
					consecutiveCount = 1;
				else 
					consecutiveCount = 1 + turnHistory.get(turnHistory.size() - 1).count;
				turnHistory.add(new TurnCount("Right", consecutiveCount, i, latArray.get(i), longArray.get(i)));
				turnCountingCompareValue += -90;
			}
		}
		return turnHistory;
	}
	
	public class TurnCount {
		public String direction;
		public int count;
		public int index;
		public double latitude;
		public double longitude;
		
		TurnCount(String newDirection, int newCount, int newIndex, double newLatitude, double newLongitude) {
			direction = newDirection;
			count = newCount;
			index = newIndex;
			latitude = newLatitude;
			longitude = newLongitude;
		}
	}

	
	public float getFloatingAverage(ArrayList<Float> turnDegreesArray, int searchOffset, int idx) {
		float mean = 0;
		int summedCount = 0;
		for(int i = idx - searchOffset; i <= idx + searchOffset; i++) {
			if(i >= 0 && i < turnDegreesArray.size()) {
				mean += turnDegreesArray.get(i);
				summedCount++;
			}
		}
		if(summedCount > 0)
			mean = mean / summedCount;
		return mean;
	}
	
	//remove the last turn
	//naieve implementation for now
	public float parkTurnCorrection() {
		//identify continual turn threshold/time/speed/localmean
		//once last one is identified, remove that amount (could be left, right, 90 stall/angled stall, multiple movements
		//for now always assume a right quarter turn into the stall
		return -1; //note data convention is opposite of toString() and csv graphed data. Dont get mixed up
	}
	
	//curently no effects
	public float fidgitingCorrection() {
		return 0; //not yet implemented
	}
	
	//temporary file functions for the design phase
	//
	public void readFile(File dataFile) {
		int degreeIndex = -1;
		try {
			FileReader fr = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr);
			br.readLine();//skip the first of two header lines
			
			//get the correct column out of this one (also skips the last header line)
			String headers[] = br.readLine().split(",");
			for(int i = 0; i < headers.length; i++) {
				//System.out.println(headers[i] + " " + degreeIndex + " " + i);
				if(headers[i].contains("turn degrees")) {
					degreeIndex = i;
				}
			}
			String line;
			while((line = br.readLine()) != null) {
				String tokens[] = line.split(",");
				//System.out.println(tokens[8]);
				turnDegreesArray.add(Float.parseFloat(tokens[degreeIndex]));
				
				latArray.add(Double.parseDouble(tokens[1]));
				longArray.add(Double.parseDouble(tokens[2]));
			}
			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
