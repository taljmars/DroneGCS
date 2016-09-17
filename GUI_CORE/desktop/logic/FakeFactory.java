package desktop.logic;

import mavlink.core.firmware.FirmwareType;
import mavlink.is.drone.Preferences;
import mavlink.is.drone.profiles.VehicleProfile;

public class FakeFactory {

	public static Preferences fakePreferences() {
		return new Preferences() {

			@Override
			public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public FirmwareType getVehicleType() {
				// TODO Auto-generated method stub
				return FirmwareType.ARDU_COPTER;
			}

			@Override
			public Rates getRates() {
				return new Rates();
			}
		};
	}

}
