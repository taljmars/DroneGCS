package is.drone.variables;

import org.springframework.stereotype.Component;

import is.drone.DroneVariable;
import is.drone.DroneInterfaces.DroneEventsType;

@Component("missionStats")
public class MissionStats extends DroneVariable {

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
