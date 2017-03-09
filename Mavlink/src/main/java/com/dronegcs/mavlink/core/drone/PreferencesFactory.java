package com.dronegcs.mavlink.core.mavlink.drone;

import com.dronegcs.mavlink.core.mavlink.firmware.FirmwareType;
import com.dronegcs.mavlink.is.mavlink.drone.Preferences;

public class PreferencesFactory {

	public static Preferences getPreferences() {
		Preferences pref = new PreferencesImpl();
		pref.loadVehicleProfile(FirmwareType.ARDU_COPTER);
		return pref;
	}

}
