package com.dronegcs.console_plugin.validations.internal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.dronegcs.console_plugin.validations.DroneIsArmed;
import com.dronegcs.mavlink.is.drone.Drone;

public class DroneIsArmedValidator implements ConstraintValidator<DroneIsArmed, Drone> {

	@Override
	public void initialize(DroneIsArmed arg0) {

	}

	@Override
	public boolean isValid(Drone drone, ConstraintValidatorContext arg1) {
		if (drone.getState().isArmed())
			return true;
		return false;
	}

}
