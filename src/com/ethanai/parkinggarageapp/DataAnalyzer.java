package com.ethanai.parkinggarageapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.ethanai.parkinggarageapp.RecentSensorData.DerivedOrientation;

/*
 * Eventually I'd like to let the user drive in their structure to the top floor. Record the path. Use that as a pattern to match
 * For now, just let them select 'Right Turn structure' 'Left turn structure' or 'Split' and have split go to TBC screen
 */
public class DataAnalyzer {
	ArrayList<Float> turnDegreesArray = new ArrayList<Float>();

	//for testing from csvs
	private final int DEGREE_COL = 10;
	
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

	public String getCurrentFloor() {
		String parkedFloor = "";
		float quarterTurnCount = getConsecutiveRightTurns();
		System.out.println("Raw right turns: " + quarterTurnCount);
		//TODO create array of pairs with border of turns to reach that floor and that floor's name. In future make this adaptable
		//peel off final parking the car turn (could be left or right, but doesn't change your floor any)
			//hardcoded for now. My building only lets compact cars park on the right
		quarterTurnCount += fidgitingCorrection(); //incase we cant get the sensors to stop immediatly upon ignition stop (likely, if not a BT car person)
		quarterTurnCount += parkTurnCorrection();
		System.out.println("Corrected Right Turns: " + quarterTurnCount);
		
		if(quarterTurnCount < -1) {
			parkedFloor = "1?";
		} else if (quarterTurnCount < 1) {
			parkedFloor = "1";
		} else if (quarterTurnCount < 3) {
			parkedFloor = "2";
		} else if (quarterTurnCount < 5) {
			parkedFloor = "2B";
		} else if (quarterTurnCount < 7) {
			parkedFloor = "3";
		} else if (quarterTurnCount < 9) {
			parkedFloor = "3B";
		} else if (quarterTurnCount < 11) {
			parkedFloor = "4";
		}
		
		return parkedFloor;
	}
	
	//Important we have a long history. CSV has all, but recentData structure has less. Maybe 2k is enough
	//takes values as degrees, returns value as fraction of quarter turns
	public float getConsecutiveRightTurns() {
		float totalDegreesRight = 0;
		float rightTurnCount = 0;
		//Time threshold - if we didnt turn after xxx readings ... maybe not good measure
		
		//Intensity threshold - work back from end, until we find a left turn
		float turnThreshold = 0.75f; //threshold 75% of a turn will be considered enough that a weak left turn will trigger end of right turn chain
		float leftDeltaCount = 0;
		float minTurnCount = turnDegreesArray.get(turnDegreesArray.size()-1) / 90;
		float smoothingThreshold = 0.2f;
		for(int i = turnDegreesArray.size() - 1; i > 0 && leftDeltaCount < turnThreshold; i--) { //iterate back from the end (time of parking)
			float quarterTurnCount = (float) (turnDegreesArray.get(i) / 90);
			if(quarterTurnCount < minTurnCount) {
				minTurnCount = quarterTurnCount;
			}
			//higher values are more 'right'. 
			//As we subtract the right turns away, the turnCount should go down. If our new turncount grows, we must have subtracted a left turn.
				//limit how much a single data point can move us towards the threshold
			float totalNewChange = (quarterTurnCount - minTurnCount) - leftDeltaCount;
			leftDeltaCount += totalNewChange < smoothingThreshold ? totalNewChange : smoothingThreshold;
			
			totalDegreesRight = turnDegreesArray.get(turnDegreesArray.size()-1) - turnDegreesArray.get(i);
			// + 2 for he headers + 1 for orindal counting. total + 3 to match with exel data
			//System.out.println(i+3 + "\tdel "+ leftDeltaCount + "\tmin " + minTurnCount + "\tturnCount " + quarterTurnCount + "\tturns " + totalDegreesRight/90 + "\tdegree " + turnDegreesArray.get(i));
		}
		rightTurnCount = totalDegreesRight/90 + 0.5f*turnThreshold; // + 1/2 since we measured maximums, not averages
		return rightTurnCount;
	}
	
	//remove the last turn
	//naieve implementation for now
	private float parkTurnCorrection() {
		//identify continual turn threshold/time/speed/localmean
		//once last one is identified, remove that amount (could be left, right, 90 stall/angled stall, multiple movements
		//for now always assume a right quarter turn into the stall
		return -1; //note data convention is opposite of toString() and csv graphed data. Dont get mixed up
	}
	
	//curently no effects
	private float fidgitingCorrection() {
		return 0; //not yet implemented
	}
	
	//temporary file functions for the design phase
	//
	private ArrayList<Float> readTurnDegreesFromFile(File dataFile) {
		try {
			FileReader fr = new FileReader(dataFile);
			BufferedReader br = new BufferedReader(fr);
			br.readLine();//skip the two header lines
			br.readLine();
			String line;
			while((line = br.readLine()) != null) {
				String tokens[] = line.split(",");
				//System.out.println(tokens[8]);
				turnDegreesArray.add(Float.parseFloat(tokens[DEGREE_COL]) * -1); //flip the convention from mine, to Android's so fucntion will work on app. 
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
		return null;
	}
	
	
}
