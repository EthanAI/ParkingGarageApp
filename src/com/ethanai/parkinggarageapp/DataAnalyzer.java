package com.ethanai.parkinggarageapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;






import com.ethanai.parkinggarageapp.dataStructures.Floor;

import android.util.Log;


//import com.ethanai.parkinggarageapp.RecentSensorData.DerivedOrientation;

/*
 * Eventually I'd like to let the user drive in their structure to the top floor. Record the path. Use that as a pattern to match
 * For now, just let them select 'Right Turn structure' 'Left turn structure' or 'Split' and have split go to TBC screen
 */
public class DataAnalyzer {	
	ArrayList<Float> turnDegreesArray 	= new ArrayList<Float>();
	ArrayList<Double> latArray 			= new ArrayList<Double>();
	ArrayList<Double> longArray 		= new ArrayList<Double>();
	
	ArrayList<Floor> floors;
	
	public UserSettings mySettings; // = MainActivity.mySettings;
	//public RecentSensorData recentData; //can't rely on this. sometimes we read from a file
	public PhoneLocation newestPhoneLocation; // = MainActivity.recentData.newestPhoneLocation;
	
	public boolean isFullAnalysis = false;
	public boolean isTestAnalysis = false;
	
	DataAnalyzer(RecentSensorData recentData) { 
		for(int i = 0; i < recentData.orientRecent.size()-1; i++) {
			turnDegreesArray.add((float)recentData.orientRecent.get(i).totalTurnDegrees);
		}
		
		for(int i = 0; i < recentData.orientRecent.size() - 1; i++) {
			latArray.add(recentData.orientRecent.get(i).phoneLocation.getLatitude());
			longArray.add(recentData.orientRecent.get(i).phoneLocation.getLongitude());
		}		
		
		if(null == MainActivity.mySettings)
			mySettings = DaemonReceiver.mySettings;
		else
			mySettings = MainActivity.mySettings;
		
		if(null == MainActivity.recentData)
			newestPhoneLocation = DaemonReceiver.recentData.newestPhoneLocation;
		else
			newestPhoneLocation = MainActivity.recentData.newestPhoneLocation;
	}
	
	//for final complete analysis, incase user took a long time to park and 
	//recentSensorData ran out of room (and ate old data)
	public DataAnalyzer(File dataFile) {
		readFile(dataFile);
		isFullAnalysis = true;
		
		if(null == MainActivity.mySettings)
			mySettings = DaemonReceiver.mySettings;
		else
			mySettings = MainActivity.mySettings;
		
		if(null == MainActivity.recentData)
			newestPhoneLocation = DaemonReceiver.recentData.newestPhoneLocation;
		else
			newestPhoneLocation = MainActivity.recentData.newestPhoneLocation;
	}
	
	public DataAnalyzer(File dataFile, ArrayList<Floor> newFloors) {
		readFile(dataFile);
		isFullAnalysis = true;
		isTestAnalysis = true;
		
		floors = newFloors;
		
	}
	
	public String getCurrentFloorEstimate() {
		String floorName = null;
		Floor parkedFloor = null;
		
		GarageLocation garageLocation = null;
		if(null != newestPhoneLocation) {		
			float quarterTurnCount = getRawConsecutiveTurns();
			
			garageLocation = mySettings.getGarageLocation(newestPhoneLocation.getLocationName());
			if(null != garageLocation && null != garageLocation.floors) {
				parkedFloor = garageLocation.getMatchingFloor(quarterTurnCount);
			}
		}
		
		if(null == newestPhoneLocation)
			floorName = "None gps";
		else if(null == garageLocation)
			floorName = "None g";
		else if(null == parkedFloor) {
			floorName = "None f";
		} else {
			floorName = parkedFloor.floorString;
		}
		Log.i("DataAnalyzer", "gCFE(): " + garageLocation + " " + parkedFloor);
		
		return floorName;
	}
	
	public String getFloor() {
		String floorName = null;
		Floor parkedFloor = null;
				
		float quarterTurnCount = getConsecutiveTurns(); //includes park turn removal/correction
		
		if(isTestAnalysis) {			
			//iterate through the floor borders until we find our first, minimum floor hit.
			//maybe we could do some cool hashmap thing here?
			if(null != floors && floors.size() > 0) {
				float difference = Math.abs(floors.get(0).turns - quarterTurnCount);
				parkedFloor = floors.get(0);
				for(Floor floor : floors) {
					if(Math.abs(floor.turns - quarterTurnCount) < difference) {
						difference = Math.abs(floor.turns - quarterTurnCount);
						parkedFloor = floor;
					}
				}
				floorName = parkedFloor.floorString;
			}
		} else {
			GarageLocation garageLocation = null;
			if(null != newestPhoneLocation) {
				//quarterTurnCount += fidgitingCorrection(); //incase we cant get the sensors to stop immediatly upon ignition stop (likely, if not a BT car person)			
	
				garageLocation = mySettings.getGarageLocation(newestPhoneLocation.getLocationName());
				if(null != garageLocation && null != garageLocation.floors) {
					parkedFloor = garageLocation.getMatchingFloor(quarterTurnCount);
				}
			}
			
			if(null == newestPhoneLocation)
				floorName = "None noGPS";
			else if(null == garageLocation)
				floorName = "None noGarage";
			else if(null == parkedFloor) {
				floorName = "None noFloorMatch";
			} else {
				floorName = parkedFloor.floorString;
			}
			
			Log.i("DataAnalyzer", "gCF: " + garageLocation + " " + parkedFloor);
		}
		return floorName;
	}
	

	
	/*
	//Important we have a long history. CSV has all, but recentData structure has less. Maybe 2k is enough
	//takes values as degrees, returns value as fraction of quarter turns
	//todo - modify it to find positive or negative counts
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
		return rightConsecutiveCount;
	}
	*/
	
	//positive for left negative right
	// this turn count does the compensation for the final turn. It finds that point, and chooses the end point
	// as being right before that final turn. 
	public float getConsecutiveTurns() {
		//Find the starting point after the parking (could be left or right) movement
		final int LAST = 0; //turnhistory is in reverse chron order
		ArrayList<TurnCount> turnHistory = getAllTurns();
		int finalDriveIndex; //last data point before park turning
		if(turnHistory.size() > 0)
			finalDriveIndex = turnHistory.get(LAST).index; //get the index of the beginning of the final (parking) turn
		else
			finalDriveIndex = turnDegreesArray.size() - 1; //just estimate from the end
		
		
		//Intensity threshold - work back from end, until we find a left turn
			//try with floating average
		int meanOffset = 10; //how far left and right to include in the smoothing averaging process
		float turnThreshold = 0.75f; //max left turn before we stop the count
		float consecutiveTurns = 0; //total amount turned left without changing directions
		//right is high. As we work back in time to 'unwind' we should be decreasing. This tracks how far the lowest point is so far before we find an inflection point
		//adding more lefts
		float runningHighCount = getFloatingAverage(turnDegreesArray, meanOffset, finalDriveIndex) / 90;
		float runningLowCount = getFloatingAverage(turnDegreesArray, meanOffset, finalDriveIndex) / 90;
		float parkingEndCount = runningHighCount; //will need modified by the removeFidgit() method in future
		boolean isTurningLeft = true;
		boolean isTurningRight = true;
		for(int i = finalDriveIndex; i > 0 && (isTurningLeft || isTurningRight); i--) {
			float floatingMeanDegrees = getFloatingAverage(turnDegreesArray, meanOffset, i);
			float quarterTurnCount = floatingMeanDegrees / 90; //net quarter turns according to sensors
			if(quarterTurnCount > runningHighCount) { //keep consistent with current sign convention
				runningHighCount = quarterTurnCount;
			} else if(quarterTurnCount < runningLowCount) {
				runningLowCount = quarterTurnCount;
			}
			
			//if current turn drops too far below the highest seen value, we've gone into a left turn
			if(runningHighCount - quarterTurnCount > turnThreshold) {
				isTurningRight = false;
				consecutiveTurns = parkingEndCount - runningLowCount; //left turns = positive value 
			}
			
			//if current turn goes too far above the minimum turn seen, we've gone into a right turn
			if(quarterTurnCount - runningLowCount > turnThreshold) {
				isTurningLeft = false;
				consecutiveTurns = (runningHighCount - parkingEndCount) * -1; //right turns - negative value
			}			
		}
		return consecutiveTurns;
	}
	
	//positive for left negative right
	//looks at the whole thing, so no correction for the final turn made
	//expects this will be viewed by debugger before parking the car, so correct for this situation
	public float getRawConsecutiveTurns() {
		//Find the starting point after the parking (could be left or right) movement
		int finalDriveIndex = turnDegreesArray.size() - 1; //just estimate from the end
		
		//Intensity threshold - work back from end, until we find a left turn
			//try with floating average
		int meanOffset = 10; //how far left and right to include in the smoothing averaging process
		float turnThreshold = 0.75f; //max left turn before we stop the count
		float consecutiveTurns = 0; //total amount turned left without changing directions
		//right is high. As we work back in time to 'unwind' we should be decreasing. This tracks how far the lowest point is so far before we find an inflection point
		//adding more lefts
		float runningHighCount = getFloatingAverage(turnDegreesArray, meanOffset, finalDriveIndex) / 90;
		float runningLowCount = getFloatingAverage(turnDegreesArray, meanOffset, finalDriveIndex) / 90;
		float parkingEndCount = runningHighCount; //will need modified by the removeFidgit() method in future
		boolean isTurningLeft = true;
		boolean isTurningRight = true;
		for(int i = finalDriveIndex; i > 0 && (isTurningLeft || isTurningRight); i--) {
			float floatingMeanDegrees = getFloatingAverage(turnDegreesArray, meanOffset, i);
			float quarterTurnCount = floatingMeanDegrees / 90; //net quarter turns according to sensors
			if(quarterTurnCount > runningHighCount) { //keep consistent with current sign convention
				runningHighCount = quarterTurnCount;
			} else if(quarterTurnCount < runningLowCount) {
				runningLowCount = quarterTurnCount;
			}
			
			//if current turn drops too far below the highest seen value, we've gone into a left turn
			if(runningHighCount - quarterTurnCount > turnThreshold) {
				isTurningRight = false;
				consecutiveTurns = parkingEndCount - runningLowCount; //left turns = positive value 
			}
			
			//if current turn goes too far above the minimum turn seen, we've gone into a right turn
			if(quarterTurnCount - runningLowCount > turnThreshold) {
				isTurningLeft = false;
				consecutiveTurns = (runningHighCount - parkingEndCount) * -1; //right turns - negative value
			}
			
			//rightConsecutiveCount = runningHighCount - parkingEndCount;
			//leftConsecutiveCount =  runningHighCount - quarterTurnCount;
			
			//System.out.println(i+3 + "\tleftCount "+ leftConsecutiveCount + "\trightCount " + rightConsecutiveCount + "\tparkingEndCount " + parkingEndCount + "\trunningLowCount " + runningLowCount);
			
		}
		return consecutiveTurns;
	}
	
	public ArrayList<TurnCount> getAllTurns() {
		int meanOffset = 10; //how far left and right to include in the smoothing averaging process
		
		ArrayList<TurnCount> turnHistory = new ArrayList<TurnCount>(); //hold the detected turns

		//New method with softer trigger critera:
			//if consecutive turn, you count full 90 degree slices
			//if non-consecutive/terminal, anything geater than 80% of 90 degrees is enough
		//variable to we compare against to see if we've competed more than one turn of a type
		float softThreshold = 70;
		float leftComparisonBaseDegrees = getFloatingAverage(turnDegreesArray, meanOffset, turnDegreesArray.size() - 1);
		float rightComparisonBaseDegrees = leftComparisonBaseDegrees;
		//float turnCountingCompareValue = getFloatingAverage(turnDegreesArray, meanOffset, turnDegreesArray.size() - 1);
		String lastCompletedTurn = "none";
		for(int i = turnDegreesArray.size() - 1; i > 0; i--) {
			float floatingMeanDegrees = getFloatingAverage(turnDegreesArray, meanOffset, i); //use smoothed value (kill outliers)
			//float changeSinceLastTurnPush = floatingMeanDegrees - turnCountingCompareValue;
			
			//update thresholds based on direction we're turning
				//If we are going consecutive turns, our bar will be raised by 90 degree chunks (raised elsewhere)
				//If we might have changed directions, we want to take the we've deviated since last recorded turn
			if(lastCompletedTurn.equalsIgnoreCase("none")) {
				//if(floatingMeanDegrees < leftComparisonBaseDegrees)
				//	leftComparisonBaseDegrees = floatingMeanDegrees;
				//if(floatingMeanDegrees > leftComparisonBaseDegrees)
				//	rightComparisonBaseDegrees = floatingMeanDegrees;
			} else if(lastCompletedTurn.equalsIgnoreCase("left")) {
				if(floatingMeanDegrees < rightComparisonBaseDegrees)
					rightComparisonBaseDegrees = floatingMeanDegrees;
			} else if(lastCompletedTurn.equalsIgnoreCase("right")) {
				if(floatingMeanDegrees > leftComparisonBaseDegrees)
					leftComparisonBaseDegrees = floatingMeanDegrees;
			}
			
			if(leftComparisonBaseDegrees - floatingMeanDegrees > softThreshold) { 
				//Check if this is the first one. If consecutive, count with nth value this is
				int consecutiveCount; //count how many times in a row weve done this same turn
				if(turnHistory.size() == 0 || !turnHistory.get(turnHistory.size() - 1).direction.equalsIgnoreCase("left"))
					consecutiveCount = 1;
				else 
					consecutiveCount = 1 + turnHistory.get(turnHistory.size() - 1).count;
				
				//store this turn record. Its direction, its nth, the sensor reading number, and gps at this time
				turnHistory.add(new TurnCount("Left", consecutiveCount, i, latArray.get(i), longArray.get(i), turnDegreesArray.get(i) / 90));
				//Raise the threshold to get another to a full 90 degrees above this one (only the final can be partial)
				leftComparisonBaseDegrees += -90; //set our new threshold to the end of this turn
				//store completed turn type so we know which turn is soft and which is hard thresholded
				lastCompletedTurn = "left";
			} else if (rightComparisonBaseDegrees - floatingMeanDegrees < -1*softThreshold) {
				int consecutiveCount;
				if(turnHistory.size() == 0 || !turnHistory.get(turnHistory.size() - 1).direction.equalsIgnoreCase("right"))
					consecutiveCount = 1;
				else 
					consecutiveCount = 1 + turnHistory.get(turnHistory.size() - 1).count;
				turnHistory.add(new TurnCount("Right", consecutiveCount, i, latArray.get(i), longArray.get(i), turnDegreesArray.get(i) / 90));
				rightComparisonBaseDegrees += 90;
				lastCompletedTurn = "right";
			}
		}
		
		/*  old version that works, but misses something if it is even 1 degree short of 90 degrees
				float turnCountingCompareValue = getFloatingAverage(turnDegreesArray, meanOffset, turnDegreesArray.size() - 1);
		for(int i = turnDegreesArray.size() - 1; i > 0; i--) {
			float floatingMeanDegrees = getFloatingAverage(turnDegreesArray, meanOffset, i); //use smoothed value (kill outliers)
			float changeSinceLastTurnPush = floatingMeanDegrees - turnCountingCompareValue;
			
			if(changeSinceLastTurnPush > 90) { //should make this a softer value, but keep some memmory so we can absorb wiggles, but still add them to a commited movement
				int consecutiveCount; //count how many times in a row weve done this same turn
				if(turnHistory.size() == 0 || !turnHistory.get(turnHistory.size() - 1).direction.equalsIgnoreCase("Right"))
					consecutiveCount = 1;
				else 
					consecutiveCount = 1 + turnHistory.get(turnHistory.size() - 1).count;
				turnHistory.add(new TurnCount("Right", consecutiveCount, i, latArray.get(i), longArray.get(i)));
				turnCountingCompareValue += 90; //set our new threshold to the end of this turn
			} else if (changeSinceLastTurnPush < -90) {
				int consecutiveCount;
				if(turnHistory.size() == 0 || !turnHistory.get(turnHistory.size() - 1).direction.equalsIgnoreCase("Left"))
					consecutiveCount = 1;
				else 
					consecutiveCount = 1 + turnHistory.get(turnHistory.size() - 1).count;
				turnHistory.add(new TurnCount("Left", consecutiveCount, i, latArray.get(i), longArray.get(i)));
				turnCountingCompareValue += -90;
			}
		}
		 */
		return turnHistory;
	}
	
	public class TurnCount {
		public String direction;
		public int count;
		public int index;
		public double latitude;
		public double longitude;
		public float turnCount;
		
		TurnCount(String newDirection, int newCount, int newIndex, double newLatitude, double newLongitude, float newTurnCount) {
			direction = newDirection;
			count = newCount;
			index = newIndex;
			latitude = newLatitude;
			longitude = newLongitude;
			turnCount = newTurnCount;
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
			
			String headers[] = br.readLine().split(",");
			if(headers[0].contains("Departed") || headers[0].contains("Parked")) { //if this exists we have the older header style with 2 lines
				headers = br.readLine().split(","); //throw that away, replace with next header line
			}
			
			//get the correct column out of this one (also skips the last header line)
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
			Log.e("DataAnalyzer", e.getMessage());
		} catch (IOException e) {
			Log.e("DataAnalyzer", e.getMessage());
		}
	}	
	
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
/*
public String getCurrentFloorFinal(File dataFile) {
	readFile(dataFile);
	return getCurrentFloor();
}
*/

/*
public String getCurrentFloor(ArrayList<Float> turnDegreesArray, PhoneLocation... phoneLocation) {
	String currentLocationName;
	if(null != phoneLocation[0]) {
		PhoneLocation currentLocation = phoneLocation[0];
		currentLocationName = currentLocation.getLocationName();
	} else {
		currentLocationName = "Home"; //hardcode for offline testing. Code should not be used in the wild
	}
	GarageLocation garageLocation = UserSettings.getGarageLocation(currentLocationName);
	ArrayList<Floor> floorBorders = garageLocation.floors;
	
	String parkedFloor = "";
	float quarterTurnCount = getConsecutiveTurns();
	System.out.println("Raw right turns: " + quarterTurnCount);

		//new turn counter removes the park turn end so no need for modification
	//quarterTurnCount += fidgitingCorrection(); //incase we cant get the sensors to stop immediatly upon ignition stop (likely, if not a BT car person)
	//quarterTurnCount += parkTurnCorrection();
	int roundedTurnCount = Math.round(quarterTurnCount);
	System.out.println("Corrected Right Turns: " + roundedTurnCount);
	
	//iterate through the floor borders until we find our first, minimum floor hit.
	for(Floor floor : floorBorders) {
		if(roundedTurnCount < floor.turns && parkedFloor == "") {
			parkedFloor = floor.floorString;
		}
	}
	
	return parkedFloor;
}
*/

/*
//identify continual turn threshold/time/speed/localmean
//once last one is identified, remove that amount (could be left, right, 90 stall/angled stall, multiple movements
//for now always assume a right quarter turn into the stall
return -1; //note data convention is opposite of toString() and csv graphed data. Dont get mixed up
*/
/*
//remove the last turn
//shouldnt be needed. Turn count identifies the park turn and stops analysis before that point
public float parkTurnCorrection() {
	//better version
	final int LAST = 0; //array is in reverse chron order
	ArrayList<TurnCount> turnHistory = getAllTurns();
	if(turnHistory.size() > 0) {
		TurnCount lastTurn = turnHistory.get(LAST);
		if(lastTurn.direction.equalsIgnoreCase("right")) {
			return -1;
		} else {
			return 1;
		}
	} else {
		return 0;
	}
}
*/

/*
DataAnalyzer(RecentSensorData recentData, PhoneLocation newCurrentPhoneLocation) { 
	for(int i = 0; i < recentData.orientRecent.size()-1; i++) {
		turnDegreesArray.add((float)recentData.orientRecent.get(i).totalTurnDegrees);
	}
	
	for(int i = 0; i < recentData.orientRecent.size() - 1; i++) {
		latArray.add(recentData.orientRecent.get(i).phoneLocation.getLatitude());
		longArray.add(recentData.orientRecent.get(i).phoneLocation.getLongitude());
	}
	
	currentPhoneLocation = newCurrentPhoneLocation;
}
*/

/*
DataAnalyzer(ArrayList<DerivedOrientation> orientRecent) { 
	for(int i = 0; i < orientRecent.size()-1; i++) {
		turnDegreesArray.add((float)orientRecent.get(i).totalTurnDegrees);
	}
}
*/

/*
 * Doesn't hold the full dataset in memory so just an estimate
 */
/*
public String getCurrentFloorEstimate(RecentSensorData recentData) {
	ArrayList<Float> arrayList = new ArrayList<Float>();
	for(int i = 0; i < recentData.orientRecent.size()-1; i++) {
		arrayList.add((float)recentData.orientRecent.get(i).totalTurnDegrees);
	}
	return getCurrentFloor(arrayList, recentData.newestPhoneLocation);
}
*/
