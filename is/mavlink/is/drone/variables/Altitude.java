package mavlink.is.drone.variables;

import org.springframework.stereotype.Component;

import mavlink.is.drone.DroneInterfaces;
import mavlink.is.drone.DroneVariable;

@Component("altitude")
public class Altitude extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3669655537440461359L;
	private static final double FOUR_HUNDRED_FEET_IN_METERS = 121.92;
	private double altitude = 0;
	private double targetAltitude = 0;
	private double previousAltitude = 0;
	private double maxAltitude;

	private boolean isCollisionImminent;

	public double getAltitude() {
		return altitude;
	}

	public double getTargetAltitude() {
		return targetAltitude;
	}
	
	public double getMaxAltitude() {
		return maxAltitude;
	}

	public boolean isCollisionImminent() {
		return isCollisionImminent;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
		if (altitude > FOUR_HUNDRED_FEET_IN_METERS
				&& previousAltitude <= FOUR_HUNDRED_FEET_IN_METERS) {
			drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.WARNING_400FT_EXCEEDED);
		}
		previousAltitude = altitude;
		maxAltitude = Math.max(maxAltitude, altitude);
	}

	public void setAltitudeError(double alt_error) {
		targetAltitude = alt_error + altitude;
	}

	public void setCollisionImminent(boolean isCollisionImminent) {
		if (this.isCollisionImminent != isCollisionImminent) {
			this.isCollisionImminent = isCollisionImminent;
			drone.notifyDroneEvent(DroneInterfaces.DroneEventsType.STATE);
		}
	}

}