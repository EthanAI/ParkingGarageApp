package com.ethanai.parkinggarageapp;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class RecentSensorData implements Serializable { //must specify serializable so it can be passed by our intents neatly
	
    public int historyLength = 100;
    private final float ACCELEROMETER_NOISE = (float) 0.5;
	public ArrayList<AccelerometerReading> accRecent = new ArrayList<AccelerometerReading>();
	public ArrayList<CompassReading> magnRecent = new ArrayList<CompassReading>();
	public ArrayList<HumidityReading> humidRecent = new ArrayList<HumidityReading>();
	public ArrayList<PressureReading> pressRecent = new ArrayList<PressureReading>();

	RecentSensorData() {
		
	}
	
	RecentSensorData(int newHistoryLength) {
		historyLength = newHistoryLength;
	}
	
	/*
	 * Call the constructor for whatever sensor type made this, build a sensor reading object
	 * Store object in the appropriate ArrayList
	 */
	public <E> void addUpToLimit(String dateString, SensorEvent event) {
		Sensor sensor = event.sensor;        
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        	addUpToLimit(accRecent, new AccelerometerReading(dateString, event));
        	
        } else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
        	addUpToLimit(magnRecent, new CompassReading(dateString, event));

        } else if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
        	addUpToLimit(humidRecent, new HumidityReading(dateString, event));

        } else if (sensor.getType() == Sensor.TYPE_PRESSURE) {
        	addUpToLimit(pressRecent, new PressureReading(dateString, event));

        }		
    }	
	
	public <E> void addUpToLimit(ArrayList<E> arrayList, E newEntry) {
        if(arrayList != null && arrayList.size() == historyLength)
        	arrayList.remove(0);
        arrayList.add(newEntry);
    }	


	class CompassReading {
		public String dateString;
		public float x;
		public float y;
		public float z;
		
		CompassReading(String dateString, SensorEvent event) {
			this.dateString = dateString;
			this.x = event.values[0];
			this.y = event.values[1];
			this.z = event.values[2];
		}
		
		public String toFormattedString() {
			return dateString + ", " +
	                Float.toString(x) + "," + 
	                Float.toString(y) + "," +
	                Float.toString(z) + "," +
	                "\n";
		}
		
	}
	
	class HumidityReading {
		public String dateString;
		public float humidPercent;
		
		HumidityReading(String dateString, SensorEvent event) {
			this.dateString = dateString;
			this.humidPercent = event.values[0];
		}
		
		public String toFormattedString() {
			return dateString + ", " +
	                Float.toString(humidPercent) + 
	                "\n";
		}
		
	}
	
	class PressureReading {
		public String dateString;
		public float pressure;
		
		PressureReading(String dateString, SensorEvent event)  {
			this.dateString = dateString;
			this.pressure = event.values[0];
		}
		
		public String toFormattedString() {
			return dateString + ", " +
	                Float.toString(pressure) + 
	                "\n";
		}
	
	
	}
	
	class AccelerometerReading {
		//public Date date; //might be nice to return this someday so I can do math, but probably not in the near future
		public String dateString;
		public float x;
		public float y;
		public float z;
		public float mag;
		public float xDel;
		public float yDel;
		public float zDel;
		public float magDel;	
			
		
		AccelerometerReading(String dateString, SensorEvent event) {
			this.dateString = dateString;
			x = event.values[0];
	        y = event.values[1];
	        z = event.values[2];
	        mag = (float) Math.sqrt(x*x + y*y + z*z);
	        
	        if(accRecent.size() == 0) {
	        	xDel = 0;
	        	yDel = 0;
	        	zDel = 0;
	        	magDel = 0;
	        } else {
	        	xDel = Math.abs(accRecent.get(accRecent.size() - 1).x - x);
	            yDel = Math.abs(accRecent.get(accRecent.size() - 1).y - y);
	            zDel = Math.abs(accRecent.get(accRecent.size() - 1).z - z);
	            magDel = Math.abs(accRecent.get(accRecent.size() - 1).mag - mag);
	
	            if (xDel < ACCELEROMETER_NOISE) 
	            	xDel = (float)0.0;
	            if (yDel < ACCELEROMETER_NOISE) 
	            	yDel = (float)0.0;
	            if (zDel < ACCELEROMETER_NOISE) 
	            	zDel = (float)0.0;
	            if (magDel < ACCELEROMETER_NOISE) 
	            	magDel = (float)0.0;
	        } 
		}
		
		public String toFormattedString() {
			return dateString + ", " +
	                Float.toString(x) + ", " +
	                Float.toString(y) + ", " +
	                Float.toString(z) + ", " +
	                Float.toString(mag) + ", " +
	                Float.toString(xDel) + ", " +
	                Float.toString(yDel) + ", " +
	                Float.toString(zDel) + ", " +
	                Float.toString(magDel) + 
	                "\n";
		}
		
		
		/*
		@SuppressLint("SimpleDateFormat")
		AccelerometerReading(String dataLine) throws NumberFormatException, ParseException {
			this(new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dataLine.split(",")[0]), 
					Float.parseFloat(dataLine.split(",")[1]), Float.parseFloat(dataLine.split(",")[2]), 
					Float.parseFloat(dataLine.split(",")[3]), Float.parseFloat(dataLine.split(",")[4]), 
					Float.parseFloat(dataLine.split(",")[5]), Float.parseFloat(dataLine.split(",")[6]), 
					Float.parseFloat(dataLine.split(",")[7]), Float.parseFloat(dataLine.split(",")[8]) );
		}
		
		AccelerometerReading(Date _date, float _x, float _y, float _z, float _mag,	
				float _xDel, float _yDel, float _zDel, float _magDel) { 
			date = _date;
			x = _x;
			y = _y;
			z = _z;
			mag = _mag;
			xDel = _xDel;
			yDel = _yDel;
			zDel = _zDel;
			magDel = _magDel;
		}
			
		public String toFormattedString(String DATE_FORMAT_STRING) {
			String dateString = new SimpleDateFormat(DATE_FORMAT_STRING).format(date);
	
			return dateString + ", " +
	                Float.toString(x) + ", " +
	                Float.toString(y) + ", " +
	                Float.toString(z) + ", " +
	                Float.toString(mag) + ", " +
	                Float.toString(xDel) + ", " +
	                Float.toString(yDel) + ", " +
	                Float.toString(zDel) + ", " +
	                Float.toString(magDel) + 
	                "\n";
		}
		*/
	}//end of accelerometerReading class

}
