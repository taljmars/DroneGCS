package mavlink.core.drone.profile;

import logger.Logger;
import mavlink.is.drone.profiles.VehicleProfile;

public class ArduCopterProfile extends VehicleProfile {
	
	public ArduCopterProfile() {
		super.getDefault().setMaxAltitude(100);
		super.getDefault().setWpNavSpeed(3);
		Logger.LogDesignedMessege("ArduCopter Profile was created");
	}

}
