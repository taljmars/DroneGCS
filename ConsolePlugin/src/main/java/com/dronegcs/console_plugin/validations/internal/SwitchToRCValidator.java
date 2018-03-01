package com.dronegcs.console_plugin.validations.internal;
//
//import com.dronegcs.console.flightControllers.FlightController;
//import com.dronegcs.console.operations.OpChangeFlightControllerQuad;
//import com.dronegcs.console_plugin.validations.SwitchToRC;
//
//import javax.validation.ConstraintValidator;
//import javax.validation.ConstraintValidatorContext;
//
//public class SwitchToRCValidator implements ConstraintValidator<SwitchToRC, OpChangeFlightControllerQuad> {
//
//	public int MIN_THRUST_TO_SWITCH_TO_RC = 1100;
//
//	@Override
//	public void initialize(SwitchToRC arg0) {
//
//	}
//
//	@Override
//	public boolean isValid(OpChangeFlightControllerQuad changeFlightControllerQuad, ConstraintValidatorContext arg1) {
//		if (changeFlightControllerQuad.getRequiredControler() == FlightController.UNKNOWN) {
//			//disable existing violation message
//			arg1.disableDefaultConstraintViolation();
//		    //build new violation message and add it
//			arg1.buildConstraintViolationWithTemplate("Flight controller is unknown").addConstraintViolation();
//			return false;
//		}
//
//		if (changeFlightControllerQuad.getRequiredControler() != FlightController.REMOTE)
//			return true;
//
//		int thrustOut = changeFlightControllerQuad.getDrone().getRC().getThrustOut();
//		if (thrustOut > MIN_THRUST_TO_SWITCH_TO_RC) {
//			//disable existing violation message
//			arg1.disableDefaultConstraintViolation();
//		    //build new violation message and add it
//			arg1.buildConstraintViolationWithTemplate("Thrust is too height, please verify remote is on").addConstraintViolation();
//			return false;
//		}
//
//		return true;
//	}
//
//}