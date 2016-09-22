package mavlink.core.drone;

import logger.Logger;
import mavlink.core.drone.profile.ArduCopterProfile;
import mavlink.core.firmware.FirmwareType;
import mavlink.is.drone.Preferences;
import mavlink.is.drone.profiles.VehicleProfile;

public class PreferencesImpl implements Preferences {

	private FirmwareType type;
	private VehicleProfile profile;
	private Rates rates;

	@Override
	public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
		switch (firmwareType) {
			case ARDU_COPTER:
				type = FirmwareType.ARDU_COPTER;
				rates = new Rates(1, 1, 1, 1, 4, 1, 1, 1);
				profile = new ArduCopterProfile();
				return profile;
			default:
				Logger.LogErrorMessege("Unsupported frame '" + firmwareType.name() + "'");
		}
		return null;
	}

	@Override
	public FirmwareType getVehicleType() {
		return type;
	}

	@Override
	public Rates getRates() {
		return rates;
	}

}
