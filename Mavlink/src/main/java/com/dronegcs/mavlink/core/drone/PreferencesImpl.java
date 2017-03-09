package com.dronegcs.mavlink.core.mavlink.drone;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.dronegcs.gcsis.logger.Logger;
import com.dronegcs.mavlink.core.mavlink.drone.profile.ArduCopterProfile;
import com.dronegcs.mavlink.core.mavlink.firmware.FirmwareType;
import com.dronegcs.mavlink.is.mavlink.drone.Preferences;
import com.dronegcs.mavlink.is.mavlink.drone.profiles.VehicleProfile;

@ComponentScan("com/dronegcs/gcsis/logger")
@Component
public class PreferencesImpl implements Preferences {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.dronegcs.gcsis.logger")
	private Logger logger;

	private FirmwareType type;
	private VehicleProfile profile;
	private Rates rates;
	
	// Create a factory instead of this
	@PostConstruct
	public void TemporaryLoadMe() {
		loadVehicleProfile(FirmwareType.ARDU_COPTER);
	}

	@Override
	public VehicleProfile loadVehicleProfile(FirmwareType firmwareType) {
		switch (firmwareType) {
			case ARDU_COPTER:
				type = FirmwareType.ARDU_COPTER;
				rates = new Rates(1, 1, 1, 1, 4, 1, 1, 1);
				profile = new ArduCopterProfile();
				logger.LogDesignedMessege("ArduCopter Profile was created");
				return profile;
			default:
				logger.LogErrorMessege("Unsupported frame '" + firmwareType.name() + "'");
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
