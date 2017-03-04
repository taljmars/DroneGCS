package is.mavlink.drone.variables;

import org.springframework.stereotype.Component;

import is.mavlink.drone.DroneVariable;
import tools.geoTools.Coordinate;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;

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
