package com.dronegcs.mavlink.core.drone;

import com.dronegcs.mavlink.core.firmware.FirmwareType;
import com.dronegcs.mavlink.is.drone.Preferences;

public class PreferencesFactory {

	public static Preferences getPreferences() {
		Preferences pref = new PreferencesImpl();
		pref.loadVehicleProfile(FirmwareType.ARDU_COPTER);
		return pref;
	}

}
