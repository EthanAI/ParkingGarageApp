package com.ethanai.parkinggarageapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.ethanai.parkinggarageapp.RecentSensorData.DerivedOrientation;
import com.ethanai.parkinggarageapp.UserSettings.FloorBorder;
import com.ethanai.parkinggarageapp.UserSettings.UserLocation;

/*
 * Eventually I'd like to let the user drive in their structure to the top floor. Record the path. Use that as a pattern to match
 * For now, just let them select 'Right Turn structure' 'Left turn structure' or 'Split' and have split go to TBC screen
 */
public class DataAnalyzer {

	//for csv retrieval 
	private static final int DEGREE_COL = 10;
	
	/*//changing to not be an object anymore
	DataAnalyzer(RecentSensorData recentData) { 
		for(int i = 0; i < recentData.orientRecent.size()-1; i++) {
			turnDegreesArray.add((float)recentData.orientRecent.get(i).totalTurnDegrees);
		}
	}
	
	DataAnalyzer(ArrayList<DerivedOrientation> orientRecent) { 
		for(int i = 0; i < orientRecent.size()-1; i++) {
			turnDegreesArray.add((float)orientRecent.get(i).totalTurnDegrees);
		}
	}
	
	//for development need to load data from stored example csv files
	public DataAnalyzer(File dataFile) {
		readTurnDegreesFromFile(dataFile);
	}
	*/

	/*
	 * Doesn't hold the full dataset in memory so just an estimate
	 */
	public static String getCurrentFloorEstimate(ArrayList<DerivedOrientation> orientRecent) {
		ArrayList<Float> arrayList = new ArrayList<Float>();
		for(int i = 0; i < orientRecent.size()-1; i++) {
			arrayList.add((float)orientRecent.get(i).totalTurnDegrees);
		}
		return getCurrentFloor(arrayList);
	}
	
	/*
	 * Uses the data stored in the csv, most likely to be correct
	 */
	public static String getCurrentFloorFinal(File dataFile) {
		return getCurrentFloor(readTurnDegreesFromFile(dataFile));
	}
	
	
	public static String getCurrentFloor(ArrayList<Float> turnDegreesArray) {
		String currentLocationName = UserLocationManager.getLocationName();
		UserLocation userLocation = UserSettings.getUserLocation(currentLocationName);
		ArrayList<FloorBorder> floorBorders = userLocation.floorBorders;
		
		String parkedFloor = "";
		float quarterTurnCount = getConsecutiveRightTurns(turnDegreesArray);
		System.out.println("Raw right turns: " + quarterTurnCount);

		quarterTurnCount += fidgitingCorrection(turnDegreesArray); //incase we cant get the sensors to stop immediatly upon ignition stop (likely, if not a BT car person)
		quarterTurnCount += parkTurnCorrection(turnDegreesArray);
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
	public static float getConsecutiveRightTurns(ArrayList<Float> turnDegreesArray) {
		//Time threshold - if we didnt turn after xxx readings ... maybe not good measure
		
		//Intensity threshold - work back from end, until we find a left turn
			//try with floating average
		int meanOffset = 10; //how far left and right to include in the smoothing averaging process
		float leftThreshold = 0.75f; //max left turn before we stop the count
		float leftConsecutiveCount = 0; //total amount turned left without going right
		float rightConsecutiveCount = 0; //total amount turned right without significant left
		//right is high. As we work back in time to 'unwind' we should be decreasing. This tracks how far the lowest point is so far before we find an inflextion point
		//adding more lefts
		float runningLowCount = getFloatingAverage(turnDegreesArray, meanOffset, turnDegreesArray.size()-1) / 90;
		float parkingEndCount = runningLowCount; //will need modified by the removeFidgit() method in future
		for(int i = turnDegreesArray.size() - 1; i > 0 && leftConsecutiveCount < leftThreshold; i--) {
			float floatingMeanDegrees = getFloatingAverage(turnDegreesArray, meanOffset, i);
			float quarterTurnCount = floatingMeanDegrees / 90; //net quarter turns according to sensors
			if(quarterTurnCount < runningLowCount) {
				runningLowCount = quarterTurnCount;
			}
			rightConsecutiveCount = parkingEndCount - runningLowCount;
			leftConsecutiveCount = quarterTurnCount - runningLowCount;
			
			//System.out.println(i+3 + "\tleftCount "+ leftConsecutiveCount + "\trightCount " + rightConsecutiveCount + "\tparkingEndCount " + parkingEndCount + "\trunningLowCount " + runningLowCount);

		}
		/*
		float turnThreshold = 0.75f; //threshold 75% of a turn will be considered enough that a weak left turn will trigger end of right turn chain
		float leftDeltaCount = 0;
		float minTurnCount = turnDegreesArray.get(turnDegreesArray.size()-1) / 90;
		float smoothingThreshold = 0.2f;
		for(int i = turnDegreesArray.size() - 1; i > 0 && leftDeltaCount < turnThreshold; i--) { //iterate back from the end (time of parking)
			float quarterTurnCount = (float) (turnDegreesArray.get(i) / 90); //data point at this time in terms of quarter turns
			if(quarterTurnCount < minTurnCount) {
				minTurnCount = quarterTurnCount;
			}
			//higher values are more 'right'. 
			//As we subtract the right turns away, the turnCount should go down. If our new turncount grows, we must have subtracted a left turn.
				//limit how much a single data point can move us towards the threshold
			float totalNewChange = (quarterTurnCount - minTurnCount) - leftDeltaCount;
			leftDeltaCount += totalNewChange < smoothingThreshold ? totalNewChange : smoothingThreshold;
			
			totalDegreesRight = turnDegreesArray.get(turnDegreesArray.size()-1) - turnDegreesArray.get(i);
			// + 2 for he headers + 1 for ordinal counting. total + 3 to match with exel data
			System.out.println(i+3 + "\tdel "+ leftDeltaCount + "\tmin " + minTurnCount + "\tturnCount " + quarterTurnCount + "\tturns " + totalDegreesRight/90 + "\tdegree " + turnDegreesArray.get(i));
		}
		rightTurnCount = totalDegreesRight/90 + 0.5f*turnThreshold; // + 1/2 since we measured maximums, not averages
		*/
		return rightConsecutiveCount;
	}
	
	private static float getFloatingAverage(ArrayList<Float> turnDegreesArray, int searchOffset, int idx) {
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
	public static float parkTurnCorrection(ArrayList<Float> turnDegreesArray) {
		//identify continual turn threshold/time/speed/localmean
		//once last one is identified, remove that amount (could be left, right, 90 stall/angled stall, multiple movements
		//for now always assume a right quarter turn into the stall
		return -1; //note data convention is opposite of toString() and csv graphed data. Dont get mixed up
	}
	
	//curently no effects
	public static float fidgitingCorrection(ArrayList<Float> turnDegreesArray) {
		return 0; //not yet implemented
	}
	
	//temporary file functions for the design phase
	//
	public static ArrayList<Float> readTurnDegreesFromFile(File dataFile) {
		ArrayList<Float> arrayList = new ArrayList<Float>();
		try {
			FileReader fr = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr);
			br.readLine();//skip the two header lines
			br.readLine();
			String line;
			while((line = br.readLine()) != null) {
				String tokens[] = line.split(",");
				//System.out.println(tokens[8]);
				arrayList.add(Float.parseFloat(tokens[DEGREE_COL]) * -1); //flip the convention from mine, to Android's so fucntion will work on app. 
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
		return arrayList;
	}	
}
