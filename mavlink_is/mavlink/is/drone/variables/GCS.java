package mavlink.is.drone.variables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.utils.coordinates.Coord3D;

@Component("gcs")
public class GCS extends DroneVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 996710962242367506L;
	private Coord3D pLastPosition = null;
	
	@Autowired
	public GCS(Drone myDroneImpl) {
		super(myDroneImpl);
	}

	public Coord3D getPosition() {
		return pLastPosition;
	}

	public void setPosition(Coord3D position) {
		this.pLastPosition = position;
	}
	
	public void UpdateAll() {
		myDrone.notifyDroneEvent(DroneEventsType.GCS_LOCATION);
	}
	
}
