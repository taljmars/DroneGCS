package com.dronegcs.mavlink.core.mavlink.drone.profile;

import com.dronegcs.mavlink.is.mavlink.drone.profiles.VehicleProfile;

public class ArduCopterProfile extends VehicleProfile {
	
	public ArduCopterProfile() {
		super.getDefault().setMaxAltitude(100);
		super.getDefault().setWpNavSpeed(3);
	}

}
