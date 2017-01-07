package mavlink.is.drone.variables;

import org.springframework.stereotype.Component;

import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;

@Component("missionStats")
public class MissionStats extends DroneVariable {

	private static final long serialVersionUID = -8340536929040184879L;
	private double distanceToWp = 0;
	private short currentWP = -1;


	public void setDistanceToWp(double disttowp) {
		this.distanceToWp = disttowp;
	}

	public void setWpno(short seq) {
		if (seq != currentWP) {
			this.currentWP = seq;
			drone.notifyDroneEvent(DroneEventsType.MISSION_WP_UPDATE);
		}
	}

	public int getCurrentWP() {
		return currentWP;
	}

	public double getDistanceToWP() {
		return distanceToWp;
	}

}
