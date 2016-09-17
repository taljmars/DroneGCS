package desktop.logic;

import mavlink.core.drone.Preferences;
import mavlink.core.drone.profiles.VehicleProfile;
import mavlink.core.firmware.FirmwareType;

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
