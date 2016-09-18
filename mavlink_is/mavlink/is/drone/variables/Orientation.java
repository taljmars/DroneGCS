package mavlink.is.drone.variables;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;

public class Orientation extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5406731553279943072L;
	private double roll = 0;
	private double pitch = 0;
	private double yaw = 0;

	public Orientation(Drone myDrone) {
		super(myDrone);
	}

	public double getRoll() {
		return roll;
	}

	public double getPitch() {
		return pitch;
	}

	public double getYaw() {
		return yaw;
	}

	public void setRollPitchYaw(double roll, double pitch, double yaw) {
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
		myDrone.notifyDroneEvent(DroneEventsType.ATTITUDE);
	}

}