package com.ethanai.parkinggarageapp.dataStructures;

import java.io.Serializable;

public class Floor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8248771055332604053L;

	public float turns; //max number of quarter turns before crossing to the next floor positive is right, negative is left
	public float floorNum; //numerical representation of a floor
	public String floorString; //text representation of a floor
	
	public Floor(float turnCount, float floorNum, String floorString) {
		this.turns = turnCount;
		this.floorNum = floorNum;
		this.floorString = floorString;
	}
	
	public String toString() {
		return turns + ", " + floorNum + ", " + floorString;
	}
	
}