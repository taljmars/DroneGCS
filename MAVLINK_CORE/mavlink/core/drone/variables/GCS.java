package mavlink.core.drone.variables;

import mavlink.core.drone.DroneVariable;
import mavlink.core.drone.MyDroneImpl;
import mavlink.core.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.utils.coordinates.Coord3D;

public class GCS extends DroneVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 996710962242367506L;
	private Coord3D pLastPosition = null;
	
	public GCS(MyDroneImpl myDroneImpl) {
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
