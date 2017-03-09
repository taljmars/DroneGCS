package com.dronegcs.mavlink.is.drone;

import com.dronegcs.mavlink.core.firmware.FirmwareType;
import com.dronegcs.mavlink.is.drone.profiles.VehicleProfile;

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
		
		public Rates(	int extendedStatus, 
						int extra1, int extra2, int extra3, 
						int position, int rcChannels, int rawSensors,
						int rawControler) {
			this.extendedStatus = extendedStatus;
			this.extra1 = extra1;
			this.extra2 = extra2;
			this.extra3 = extra3;
			this.position = position;
			this.rcChannels = rcChannels;
			this.rawSensors = rawSensors;
			this.rawController = rawControler;
		}
	}
}
