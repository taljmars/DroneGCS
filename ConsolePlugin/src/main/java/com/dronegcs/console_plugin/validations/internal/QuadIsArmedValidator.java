package com.dronegcs.console_plugin.validations.internal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.dronegcs.console_plugin.validations.QuadIsArmed;
import com.dronegcs.mavlink.is.drone.Drone;

public class QuadIsArmedValidator implements ConstraintValidator<QuadIsArmed, Drone> {

	@Override
	public void initialize(QuadIsArmed arg0) {

	}

	@Override
	public boolean isValid(Drone drone, ConstraintValidatorContext arg1) {
		if (drone.getState().isArmed())
			return true;
		return false;
	}

}
