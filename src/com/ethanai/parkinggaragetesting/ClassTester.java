package com.ethanai.parkinggaragetesting;

import java.io.File;
import java.util.ArrayList;

import com.ethanai.parkinggarageapp.DataAnalyzer;

public class ClassTester {
	
	public static void main(String[] args) {

		@SuppressWarnings("unused")
		String pathA = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\";
		String pathB = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\Orientation Parking\\";
		String fileName[] = {
				"3F Parking 2014-02-20 13.50 Unknown orientationReadings",
				"2014-02-20 19.11 Unknown orientationReadings",
				"2014-02-21 18.42 Unknown orientationReadings",
				"2014-02-22 15.47 Unknown orientationReadings",
				"2014-02-22 21.33 2F Unknown orientationReadings"};
		
		
		
		//test(pathB, fileName);
		testB(pathB, fileName);
		
		//test individual 
		//test(pathB, fileName[0]);
		//testB(pathB, fileName[1]);
		
		//test(pathA, "2014-02-22 21.33 Unknown orientationReadings");
		//testB(pathB, "2014-02-22 21.33 Unknown orientationReadings");
	}
	
	
	public static void test(String path, String... fileName) {
		for(String name : fileName) {
			ArrayList<Float> turnDegreesArray = DataAnalyzer.readTurnDegreesFromFile(new File(path + name + ".csv"));
			//String currentFloor = DataAnalyzer.getCurrentFloorFinal(new File(path + name + ".csv"));
			Float turnCount = DataAnalyzer.getConsecutiveRightTurns(turnDegreesArray);
			System.out.print("RawTurnCount: " + turnCount);
			turnCount += DataAnalyzer.fidgitingCorrection(turnDegreesArray);
			turnCount += DataAnalyzer.parkTurnCorrection(turnDegreesArray);
			System.out.print(" Modified TurnCount: " + turnCount);
			//System.out.print(" Rounded TurnCount: " + Math.round(turnCount));
			System.out.print(" Dist From Center : " + getCertainty(turnCount));
			System.out.println();
			System.out.println(name + ".csv");
			System.out.println();
		}
	}
	
	
	public void testB(String path, String... fileName) {
		for(String name : fileName) {
			ArrayList<Float> turnDegreesArray = DataAnalyzer.readTurnDegreesFromFile(new File(path + name + ".csv"));
			//String currentFloor = DataAnalyzer.getCurrentFloorFinal(new File(path + name + ".csv"));

			System.out.print(getAllTurns(turnDegreesArray));

			System.out.println();
			System.out.println(name + ".csv");
			System.out.println();
		}
	}
	
	public ArrayList<TurnCount> getAllTurns(ArrayList<Float> turnDegreesArray) {
		int meanOffset = 10; //how far left and right to include in the smoothing averaging process
		
		ArrayList<TurnCount> turnHistory = new ArrayList<TurnCount>(); //hold the detected turns

		//variable to we compare against to see if we've competed more than one turn of a type
		float turnCountingCompareValue = DataAnalyzer.getFloatingAverage(turnDegreesArray, meanOffset, turnDegreesArray.size() - 1);
		for(int i = turnDegreesArray.size() - 1; i > 0; i--) {
			float floatingMeanDegrees = DataAnalyzer.getFloatingAverage(turnDegreesArray, meanOffset, i);
			float changeSinceLastTurnPush = floatingMeanDegrees - turnCountingCompareValue;
			if(changeSinceLastTurnPush > 90) {
				int consecutiveCount;
				if(turnHistory.size() == 0 || !turnHistory.get(turnHistory.size() - 1).direction.equalsIgnoreCase("Left"))
					consecutiveCount = 1;
				else 
					consecutiveCount = 1 + turnHistory.get(turnHistory.size() - 1).count;
				turnHistory.add(new TurnCount("Left", consecutiveCount, i));
				turnCountingCompareValue += 90;
			} else if (changeSinceLastTurnPush < -90) {
				int consecutiveCount;
				if(turnHistory.size() == 0 || !turnHistory.get(turnHistory.size() - 1).direction.equalsIgnoreCase("Right"))
					consecutiveCount = 1;
				else 
					consecutiveCount = 1 + turnHistory.get(turnHistory.size() - 1).count;
				turnHistory.add(new TurnCount("Right", consecutiveCount, i));
				turnCountingCompareValue += -90;
			}
		}
		return turnHistory;
	}
	
	class TurnCount {
		String direction;
		int count;
		int index;
		
		TurnCount(String newDirection, int newCount, int newIndex) {
			direction = newDirection;
			count = newCount;
			index = newIndex;
		}
	}
	
	
	public static float getCertainty(float turnCount) {
		turnCount = turnCount % 2;
		if(turnCount > 1)
			turnCount -= 2;
		return turnCount;
	}
	
	public class FloorBorder {
		public int maxTurns; //max number of quarter turns before crossing to the next floor positive is right, negative is left
		public float floorNum; //numerical representation of a floor
		public String floorString; //text representation of a floor
		
		FloorBorder (int maxTurns, float floorNum, String floorString) {
			this.maxTurns = maxTurns;
			this.floorNum = floorNum;
			this.floorString = floorString;
		}
		
	}
	
	/*
	 * 						new FloorBorder(-1, -1, "Low?"),
							new FloorBorder(1, 1, "1 Default"),
							new FloorBorder(3, 2, "2"),
							new FloorBorder(5, 2.5f, "2B"),
							new FloorBorder(7, 3, "3"),
							new FloorBorder(9, 3.5f, "3B"),
							new FloorBorder(11, 4, "4"),
							new FloorBorder(13, 99, "High?")
							
	 */

}
