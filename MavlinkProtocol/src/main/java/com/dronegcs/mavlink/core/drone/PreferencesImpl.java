package com.dronegcs.mavlink.core.drone;

import com.dronegcs.mavlink.core.drone.profile.ArduCopterProfile;
import com.dronegcs.mavlink.core.firmware.FirmwareType;
import com.dronegcs.mavlink.is.drone.Preferences;
import com.dronegcs.mavlink.is.drone.profiles.VehicleProfile;
import com.generic_tools.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

@ComponentScan(basePackages = "com.generic_tools")
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
