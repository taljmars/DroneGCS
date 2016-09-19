package mavlink.is.drone;

import mavlink.core.firmware.FirmwareType;
import mavlink.is.drone.profiles.VehicleProfile;

public interface Preferences {

	public abstract FirmwareType getVehicleType();

	public abstract VehicleProfile loadVehicleProfile(FirmwareType firmwareType);

	public abstract Rates getRates();

	public class Rates {
		public int extendedStatus;
		public int extra1;
		public int extra2;
		public int extra3;
		public int position;
		public int rcChannels;
		public int rawSensors;
		public int rawController;
	}
}