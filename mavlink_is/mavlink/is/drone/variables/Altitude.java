package mavlink.is.drone.variables;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces;
import mavlink.is.drone.DroneVariable;

public class Altitude extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3669655537440461359L;
	private static final double FOUR_HUNDRED_FEET_IN_METERS = 121.92;
	private double altitude = 0;
	private double targetAltitude = 0;
	private double previousAltitude = 0;

	private boolean isCollisionImminent;

	public Altitude(Drone myDrone) {
		super(myDrone);
	}

	public double getAltitude() {
		return altitude;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}

	public boolean isCollisionImminent() {
		return isCollisionImminent;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
		if (altitude > FOUR_HUNDRED_FEET_IN_METERS
				&& previousAltitude <= FOUR_HUNDRED_FEET_IN_METERS) {
			myDrone.notifyDroneEvent(DroneInterfaces.DroneEventsType.WARNING_400FT_EXCEEDED);
		}
		previousAltitude = altitude;
	}

	public void setAltitudeError(double alt_error) {
		targetAltitude = alt_error + altitude;
	}

	public void setCollisionImminent(boolean isCollisionImminent) {
		if (this.isCollisionImminent != isCollisionImminent) {
			this.isCollisionImminent = isCollisionImminent;
			myDrone.notifyDroneEvent(DroneInterfaces.DroneEventsType.STATE);
		}
	}

}