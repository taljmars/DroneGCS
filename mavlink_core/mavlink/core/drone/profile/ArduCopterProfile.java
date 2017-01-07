package mavlink.core.drone.profile;

import mavlink.drone.profiles.VehicleProfile;

public class ArduCopterProfile extends VehicleProfile {
	
	public ArduCopterProfile() {
		super.getDefault().setMaxAltitude(100);
		super.getDefault().setWpNavSpeed(3);
	}

}
