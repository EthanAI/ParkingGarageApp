package com.ethanai.parkinggaragetesting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.ethanai.parkinggarageapp.DataAnalyzer;
import com.ethanai.parkinggarageapp.DataAnalyzer.TurnCount;

public class ClassTester {
	
	public static void main(String[] args) {

		@SuppressWarnings("unused")
		String pathA = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\";
		String pathB = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\Home Garage Park\\";
		String fileName[] = {
				"3F Parking 2014-02-20 13.50 Unknown orientationReadings",
				"2014-02-20 19.11 Unknown orientationReadings",
				"2014-02-21 18.42 Unknown orientationReadings",
				"2014-02-22 15.47 Unknown orientationReadings",
				"2014-02-22 21.33 2F Unknown orientationReadings"};
		


		
		//test(pathB, fileName);
		//testA(pathB, "2014-03-05 15.05 21.3027206 -157.8392261 2F orientationReadings");
	    testB(pathA, "2014-03-06 13.15 Home orientationReadings");
	    		
		//test individual 
		//test(pathB, fileName[0]);
		//testB(pathB, fileName[1]);
		
		//test(pathA, "2014-02-22 21.33 Unknown orientationReadings");
		//testB(pathB, "2014-02-22 21.33 Unknown orientationReadings");
	}
	
	public static void testA(String path) {
		File[] files = new File(path).listFiles();
	    for (File file : files) {
	        if (!file.isDirectory()) {        	
				DataAnalyzer dataAnalyzer = new DataAnalyzer(file);
				//String currentFloor = DataAnalyzer.getCurrentFloorFinal(new File(path + name + ".csv"));
				Float turnCount = dataAnalyzer.getConsecutiveRightTurns();
				System.out.print("RawTurnCount: " + turnCount);
				turnCount += dataAnalyzer.fidgitingCorrection();
				turnCount += dataAnalyzer.parkTurnCorrection();
				System.out.print(" Modified TurnCount: " + turnCount);
				System.out.print(" Dist From Center : " + getCertainty(turnCount));
				System.out.print(" Floor: " + getFloorName2(turnCount) + " ");
				System.out.println();
				System.out.println(file.toString());
				System.out.println();
	        }
	    }
	
	}

	
	
	public static void testA(String path, String... fileName) {
		for(String name : fileName) {
			DataAnalyzer dataAnalyzer = new DataAnalyzer(new File(path + name + ".csv"));
			//String currentFloor = DataAnalyzer.getCurrentFloorFinal(new File(path + name + ".csv"));
			Float turnCount = dataAnalyzer.getConsecutiveRightTurns();
			System.out.print("RawTurnCount: " + turnCount);
			turnCount += dataAnalyzer.fidgitingCorrection();
			turnCount += dataAnalyzer.parkTurnCorrection();
			System.out.print(" Modified TurnCount: " + turnCount);
			System.out.print(" Floor: " + getFloorName2(turnCount) + " ");
			System.out.print(" Dist From Center : " + getCertainty(turnCount));
			System.out.println();
			System.out.println(name + ".csv");
			System.out.println();
		}
	}
	
	public static void testB(String path) {
		File[] files = new File(path).listFiles();
	    for (File file : files) {
	        if (!file.isDirectory()) {        	
	    		DataAnalyzer dataAnalyzer = new DataAnalyzer(file);
				//String currentFloor = DataAnalyzer.getCurrentFloorFinal(new File(path + name + ".csv"));

				System.out.println();
				System.out.println(file.getName());
				ArrayList<TurnCount> turnHistory = dataAnalyzer.getAllTurns();
				for(TurnCount turn : turnHistory) {
					System.out.println(turn.direction + "\t" + turn.count + "\t" + turn.index + "\t" + turn.latitude + " " + turn.longitude);
				}
	        }
	    }
		
	}
	
	
	
	public static void testB(String path, String... fileName) {
		for(String name : fileName) {
			DataAnalyzer dataAnalyzer = new DataAnalyzer(new File(path + name + ".csv"));
			//String currentFloor = DataAnalyzer.getCurrentFloorFinal(new File(path + name + ".csv"));

			System.out.println();
			System.out.println(name + ".csv");
			ArrayList<TurnCount> turnHistory = dataAnalyzer.getAllTurns();
			for(TurnCount turn : turnHistory) {
				System.out.println(turn.direction + "\t" + turn.count + "\t" + turn.index + "\t" + turn.latitude + " " + turn.longitude);
			}

			
		}
	}
	
	public static float getCertainty(float turnCount) {
		turnCount = turnCount % 2;
		if(turnCount > 1)
			turnCount -= 2;
		return turnCount;
	}
	
	private static String getFloorName2(Float consecutiveTurns) {
		ArrayList<FloorBorder2> borders = new ArrayList<FloorBorder2>(
				Arrays.asList(
						new FloorBorder2(-1, -1, "Low?"),
						new FloorBorder2(1, 1, "1 Def"),
						new FloorBorder2(3, 2, "2"),
						new FloorBorder2(5, 2.5f, "2B"),
						new FloorBorder2(7, 3, "3"),
						new FloorBorder2(9, 3.5f, "3B"),
						new FloorBorder2(11, 4, "4"),
						new FloorBorder2(13, 99, "High?")
						)); 
		
		
		String parkedFloor = "";
		//int roundedTurnCount = Math.round(consecutiveTurns);
		//iterate through the floor borders until we find our first, minimum floor hit.
		for(FloorBorder2 border : borders) {
			if(consecutiveTurns < border.maxTurns && parkedFloor == "") {
				parkedFloor = border.floorString;
			}
		}
		return parkedFloor;
	}
	
	static class FloorBorder2 {
		public int maxTurns; //max number of quarter turns before crossing to the next floor positive is right, negative is left
		public float floorNum; //numerical representation of a floor
		public String floorString; //text representation of a floor
		
		FloorBorder2 (int maxTurns, float floorNum, String floorString) {
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
