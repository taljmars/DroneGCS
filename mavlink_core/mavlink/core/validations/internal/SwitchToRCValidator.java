package mavlink.core.validations.internal;

import gui.core.operations.OpChangeFlightControllerQuad;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import mavlink.core.flightControlers.FlightControler;
import mavlink.core.validations.SwitchToRC;

public class SwitchToRCValidator implements ConstraintValidator<SwitchToRC, OpChangeFlightControllerQuad> {

	public int MIN_THRUST_TO_SWITCH_TO_RC = 1100;
	
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
		
		int thrustOut = changeFlightControllerQuad.getDrone().getRC().getThrustOut();
		if (thrustOut > MIN_THRUST_TO_SWITCH_TO_RC) {
			//disable existing violation message
			arg1.disableDefaultConstraintViolation();
		    //build new violation message and add it
			arg1.buildConstraintViolationWithTemplate("Thrust is too height, please verify remote is on").addConstraintViolation();
			return false;
		}
			
		return true;
	}

}