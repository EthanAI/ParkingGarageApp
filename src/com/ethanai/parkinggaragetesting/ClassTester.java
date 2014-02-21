package com.ethanai.parkinggaragetesting;

import java.io.File;

import com.ethanai.parkinggarageapp.DataAnalyzer;

public class ClassTester {

	public static void main(String[] args) {
		String pathA = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\";
		String pathB = "C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\Labeled\\";
		String fileName = "2014-02-20 19.11 Unknown orientationReadings.csv";
		DataAnalyzer myAnalyzer = new DataAnalyzer(
				new File(pathA+fileName));
	
		System.out.println("Parked Floor is: " + myAnalyzer.getCurrentFloor());
		
	
	}

}
