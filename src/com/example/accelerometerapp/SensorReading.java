package com.example.accelerometerapp;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class SensorReading {
	public float x;
	public float y;
	public float z;
	public float mag;
	public float xDel;
	public float yDel;
	public float zDel;
	public float magDel;
	
	public Date date; // =  new Date();
	public final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm";
	public String dateString;
	
	SensorReading(String dataLine) throws NumberFormatException, ParseException {
		this(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dataLine.split(",")[0]), 
				Float.parseFloat(dataLine.split(",")[1]), Float.parseFloat(dataLine.split(",")[2]), 
				Float.parseFloat(dataLine.split(",")[3]), Float.parseFloat(dataLine.split(",")[4]), 
				Float.parseFloat(dataLine.split(",")[5]), Float.parseFloat(dataLine.split(",")[6]), 
				Float.parseFloat(dataLine.split(",")[7]), Float.parseFloat(dataLine.split(",")[8]) );
	}
	
	SensorReading(Date _date, float _x, float _y, float _z, float _mag,	
			float _xDel, float _yDel, float _zDel, float _magDel) { 
		date = _date;
		dateString = new SimpleDateFormat(DATE_FORMAT_STRING).format(date);
		x = _x;
		y = _y;
		z = _z;
		mag = _mag;
		xDel = _xDel;
		yDel = _yDel;
		zDel = _zDel;
		magDel = _magDel;
	}
		
	public String toString() {
		return dateString + ", " +
                Float.toString(x) + ", " +
                Float.toString(y) + ", " +
                Float.toString(z) + ", " +
                Float.toString(mag) + ", " +
                Float.toString(xDel) + ", " +
                Float.toString(yDel) + ", " +
                Float.toString(zDel) + ", " +
                Float.toString(magDel) + "\n";
	}
}
