package com.ethanai.parkinggaragetesting;

import java.io.File;

import com.ethanai.parkinggarageapp.DataAnalyzer;

public class ClassTester {

	public static void main(String[] args) {
		
		DataAnalyzer myAnalyzer = new DataAnalyzer(
				new File("C:\\Dropbox\\Computer Science\\IDE Workspaces\\Eclipse\\ParkingGarageApp\\csv\\Labeled\\3F Parking 2014-02-20 13.50 Unknown orientationReadings.csv"));
	
		System.out.println("Parked Floor is: " + myAnalyzer.getCurrentFloor());
		
	
	}

}
