package com.ethanai.parkinggaragetesting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.ethanai.parkinggarageapp.DataAnalyzer;
import com.ethanai.parkinggarageapp.PresetGarages;
import com.ethanai.parkinggarageapp.DataAnalyzer.TurnCount;
import com.ethanai.parkinggarageapp.dataStructures.Floor;

public class ClassTester {
	static ArrayList<Floor> homeFloors;
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
		

		homeFloors = PresetGarages.getHomeFloors();
		
		//test(pathB, fileName);
		getTurnHistory(pathA, "2014-03-26 13.09 End CCV6 Fl None noGPS Orig CCV6 3f left orientationReadings");
		//testA(pathB);

	    
		//testA(pathB);
		//testB(pathB);
	    		
		//test individual 
		//test(pathB, fileName[0]);
		//testB(pathB, fileName[1]);
		
		//test(pathA, "2014-02-22 21.33 Unknown orientationReadings");
		//testB(pathB, "2014-02-22 21.33 Unknown orientationReadings");
	}
	
	public static void resultFromTurnCount(String path) {
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
				//System.out.print(" Floor: " + getFloorName2(turnCount));
				System.out.println();
				System.out.println(file.toString());
				System.out.println();
	        }
	    }
	
	}

	
	
	public static void resultFromTurnCount(String path, String... fileName) {
		for(String name : fileName) {
			DataAnalyzer dataAnalyzer = new DataAnalyzer(new File(path + name + ".csv"));
			//String currentFloor = DataAnalyzer.getCurrentFloorFinal(new File(path + name + ".csv"));
			Float turnCount = dataAnalyzer.getConsecutiveTurns();
			System.out.print("RawTurnCount: " + turnCount);
			turnCount += dataAnalyzer.fidgitingCorrection();
			//turnCount += dataAnalyzer.parkTurnCorrection();
			System.out.print(" Modified TurnCount: " + turnCount);
			//System.out.print(" Floor: " + getFloorName2(turnCount) + " ");
			System.out.print(" Dist From Center : " + getCertainty(turnCount));
			System.out.println();
			System.out.println(name + ".csv");
			System.out.println();
		}
	}
	
	public static void resultFromTurnHistory(String path) {
		/*
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
	    */
	
	}
	
	public static void getTurnHistory(String path) {
		File[] files = new File(path).listFiles();
	    for (File file : files) {
	        if (!file.isDirectory()) {        	
	    		DataAnalyzer dataAnalyzer = new DataAnalyzer(file, homeFloors);
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
	
	
	
	public static void getTurnHistory(String path, String... fileName) {
		for(String name : fileName) {
			DataAnalyzer dataAnalyzer = new DataAnalyzer(new File(path + name + ".csv"), homeFloors);
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
	

}
