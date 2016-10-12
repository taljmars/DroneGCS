package gui.core.validations;

import gui.core.operations.internal.OpChangeFlightControllerQuad;
import gui.is.validations.SwitchToRC;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import mavlink.core.flightControlers.FlightControler;

public class SwitchToRCValidator implements ConstraintValidator<SwitchToRC, OpChangeFlightControllerQuad> {

	public int MIN_AVG_THRUST_TO_SWITCH_TO_RC = 1000;
	
	@Override
	public void initialize(SwitchToRC arg0) {
		
	}

	@Override
	public boolean isValid(OpChangeFlightControllerQuad changeFlightControllerQuad, ConstraintValidatorContext arg1) {
		if (changeFlightControllerQuad.getRequiredControler() == FlightControler.UNKNOWN) {
			//disable existing violation message
			arg1.disableDefaultConstraintViolation();
		    //build new violation message and add it
			arg1.buildConstraintViolationWithTemplate("Flight controler is unknown").addConstraintViolation();
			return false;
		}
			
		if (changeFlightControllerQuad.getRequiredControler() != FlightControler.REMOTE)
			return true;
		
		if (!changeFlightControllerQuad.getDrone().getState().isFlying())
			return true;
		
		int avgThrust = changeFlightControllerQuad.getDrone().getRC().getAverageThrust();
		if (avgThrust < MIN_AVG_THRUST_TO_SWITCH_TO_RC)
			return false;
			
		return true;
	}

}