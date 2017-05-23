package com.dronegcs.mavlink.is.drone.variables;

import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneVariable;
import com.geo_tools.Coordinate;
import org.springframework.stereotype.Component;

@Component
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
