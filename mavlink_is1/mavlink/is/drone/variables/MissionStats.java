package mavlink.is.drone.variables;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.utils.units.Length;

public class MissionStats extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8340536929040184879L;
	private double distanceToWp = 0;
	private short currentWP = -1;

	public MissionStats(Drone myDrone) {
		super(myDrone);
	}

	public void setDistanceToWp(double disttowp) {
		this.distanceToWp = disttowp;
	}

	public void setWpno(short seq) {
		if (seq != currentWP) {
			this.currentWP = seq;
			myDrone.notifyDroneEvent(DroneEventsType.MISSION_WP_UPDATE);
		}
	}

	public int getCurrentWP() {
		return currentWP;
	}

	public Length getDistanceToWP() {
		return new Length(distanceToWp);
	}

}
