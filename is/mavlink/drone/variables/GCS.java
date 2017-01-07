package mavlink.drone.variables;


import org.springframework.stereotype.Component;

import gui.is.Coordinate;
import mavlink.drone.DroneVariable;
import mavlink.drone.DroneInterfaces.DroneEventsType;

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
