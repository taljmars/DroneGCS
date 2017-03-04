package mavlink.drone.variables;

import org.springframework.stereotype.Component;

import mavlink.drone.DroneVariable;
import mavlink.drone.DroneInterfaces.DroneEventsType;

@Component("orientation")
public class Orientation extends DroneVariable {
	
	private double roll = 0;
	private double pitch = 0;
	private double yaw = 0;

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
		drone.notifyDroneEvent(DroneEventsType.ATTITUDE);
	}

}