package com.dronegcs.mavlink.is.drone.variables;

import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneVariable;
import org.springframework.stereotype.Component;

@Component
public class Navigation extends DroneVariable {
	
	private double nav_pitch;
	private double nav_roll;
	private double nav_bearing;


	public void setNavPitchRollYaw(float nav_pitch, float nav_roll, short nav_bearing) {
		this.nav_pitch = nav_pitch;
		this.nav_roll = nav_roll;
		this.nav_bearing = nav_bearing;
		drone.notifyDroneEvent(DroneEventsType.NAVIGATION);
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
