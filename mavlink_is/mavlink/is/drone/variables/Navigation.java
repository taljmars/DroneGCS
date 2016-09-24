package mavlink.is.drone.variables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;

@Component("navigation")
public class Navigation extends DroneVariable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8819751370874611541L;
	private double nav_pitch;
	private double nav_roll;
	private double nav_bearing;

	@Autowired
	public Navigation(Drone myDrone) {
		super(myDrone);
	}

	public void setNavPitchRollYaw(float nav_pitch, float nav_roll, short nav_bearing) {
		this.nav_pitch = nav_pitch;
		this.nav_roll = nav_roll;
		this.nav_bearing = nav_bearing;
		myDrone.notifyDroneEvent(DroneEventsType.NAVIGATION);
	}

	public double getNavPitch() {
		return nav_pitch;
	}

	public double getNavRoll() {
		return nav_roll;
	}

	public double getNavBearing() {
		return nav_bearing;
	}

}
