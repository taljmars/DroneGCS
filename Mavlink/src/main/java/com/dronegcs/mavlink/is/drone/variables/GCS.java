package com.dronegcs.mavlink.is.drone.variables;

import org.springframework.stereotype.Component;

import com.dronegcs.mavlink.is.drone.DroneVariable;
import geoTools.Coordinate;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;

@Component("gcs")
public class GCS extends DroneVariable {
	
	private Coordinate pLastPosition = null;

	public Coordinate getPosition() {
		return pLastPosition;
	}

	public void setPosition(Coordinate position) {
		this.pLastPosition = position;
	}
	
	public void UpdateAll() {
		drone.notifyDroneEvent(DroneEventsType.GCS_LOCATION);
	}
	
}