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
		
		test(pathB, fileName);
	
		//test individual 
		//test(pathB, fileName[0]);
		
		//test(pathA, "2014-02-22 21.33 Unknown orientationReadings");
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
