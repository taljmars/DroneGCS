package com.dronegcs.mavlink.core.mavlink.connection.helper;

import geoTools.Coordinate;

public class GCSLocationData {
	
	private Coordinate coordinate = null;
	private int fetchTime = 0;
	
	public GCSLocationData(Coordinate coordinate, int fetchTime) {
		this.coordinate = coordinate;
		this.fetchTime = fetchTime;
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
	}
	
	public int getFetchTime() {
		return fetchTime;
	}
}
