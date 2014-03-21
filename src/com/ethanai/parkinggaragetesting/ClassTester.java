package com.ethanai.parkinggaragetesting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.ethanai.parkinggarageapp.DataAnalyzer;
import com.ethanai.parkinggarageapp.DataAnalyzer.TurnCount;

public class ClassTester {
	
	public static void main(String[] args) {

		String pathA = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\";
		String pathB = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\Home Garage Park\\";
		/*
		String fileName[] = {
				"3F Parking 2014-02-20 13.50 Unknown orientationReadings",
				"2014-02-20 19.11 Unknown orientationReadings",
				"2014-02-21 18.42 Unknown orientationReadings",
				"2014-02-22 15.47 Unknown orientationReadings",
				"2014-02-22 21.33 2F Unknown orientationReadings"}; */
		


		
		//test(pathB, fileName);
		testA(pathA, "2014-03-12 11.02 End Other Fl None Orig TestGarage orientationReadings");
		testA(pathB);

	    
		//testA(pathB);
		//testB(pathB);
	    		
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
				Float turnCount = dataAnalyzer.getConsecutiveTurns();
				System.out.print("RawTurnCount: " + turnCount);
				//turnCount += dataAnalyzer.fidgitingCorrection();
				//turnCount += dataAnalyzer.parkTurnCorrection();
				//System.out.print(" Last Remover: " + dataAnalyzer.parkTurnCorrection());
				System.out.print(" Modified TurnCount: " + turnCount);
				System.out.print(" Dist From Center : " + getCertainty(turnCount));
				System.out.print(" Floor: " + getFloorName2(turnCount));
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
			Float turnCount = dataAnalyzer.getConsecutiveTurns();
			System.out.print("RawTurnCount: " + turnCount);
			turnCount += dataAnalyzer.fidgitingCorrection();
			//turnCount += dataAnalyzer.parkTurnCorrection();
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
	
	private static String getFloorName2(Float correctedTurnCount) {
		ArrayList<Floor2> floors = new ArrayList<Floor2>(
						Arrays.asList(
								new Floor2(2, -1, "Low?"),
								new Floor2(0, 1, "1 Def"),
								new Floor2(-2, 2, "2"),
								new Floor2(-4, 2.5f, "2B"),
								new Floor2(-6, 3, "3"),
								new Floor2(-8, 3.5f, "3B"),
								new Floor2(-10, 4, "4"),
								new Floor2(-12, 99, "High?")
								)); 
		
		
		Floor2 parkedFloor = null;
		
		//iterate through the floor borders until we find our first, minimum floor hit.
		//maybe we could do some cool hashmap thing here?
		float difference = Math.abs(floors.get(0).turns - correctedTurnCount);
		for(Floor2 floor : floors) {
			if(Math.abs(floor.turns - correctedTurnCount) < difference) {
				difference = Math.abs(floor.turns - correctedTurnCount);
				parkedFloor = floor;
			}
		}
		if(null != parkedFloor)
			return parkedFloor.floorString;
		else
			return "None";
	}
	
	static class Floor2 {
		public int turns; //max number of quarter turns before crossing to the next floor positive is right, negative is left
		public float floorNum; //numerical representation of a floor
		public String floorString; //text representation of a floor
		
		Floor2 (int maxTurns, float floorNum, String floorString) {
			this.turns = maxTurns;
			this.floorNum = floorNum;
			this.floorString = floorString;
		}
		
		public String toString() {
			return turns + ", " + floorNum + ", " + floorString;
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
