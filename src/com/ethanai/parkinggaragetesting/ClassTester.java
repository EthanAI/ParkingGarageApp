package com.ethanai.parkinggaragetesting;

import java.io.File;

import com.ethanai.parkinggarageapp.DataAnalyzer;

public class ClassTester {

	public static void main(String[] args) {
		String pathA = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\";
		String pathB = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\Orientation Parking\\";
		String fileName[] = {
				"3F Parking 2014-02-20 13.50 Unknown orientationReadings",
				"2014-02-20 19.11 Unknown orientationReadings",
				"2014-02-21 18.42 Unknown orientationReadings",
				"2014-02-22 15.47 Unknown orientationReadings"};
		
		test(pathB, fileName);
	
		//test individual 
		test(pathB, fileName[0]);
	}
	
	
	public static void test(String path, String... fileName) {
		for(String name : fileName) {
			DataAnalyzer myAnalyzer = new DataAnalyzer(
				new File(path + name + ".csv"));
			System.out.println("Parked Floor is: " + myAnalyzer.getCurrentFloor());
			System.out.println(name + ".csv");
			System.out.println();
		}
	}

}
