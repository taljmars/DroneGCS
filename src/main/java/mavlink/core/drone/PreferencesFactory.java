package mavlink.core.drone;

import mavlink.core.firmware.FirmwareType;
import is.mavlink.drone.Preferences;

public class PreferencesFactory {

	public static Preferences getPreferences() {
		Preferences pref = new PreferencesImpl();
		pref.loadVehicleProfile(FirmwareType.ARDU_COPTER);
		return pref;
	}

}
