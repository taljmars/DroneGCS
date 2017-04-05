package com.dronegcs.console_plugin.operations;

import com.dronegcs.console_plugin.services.DialogManagerSvc;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.generic_tools.logger.Logger;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.generic_tools.devices.KeyBoardController;
import com.dronegcs.mavlink.core.flightControllers.FlightController;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkRC;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;

@ComponentScan("tools.com.dronegcs.console_plugin.validations")
@ComponentScan("mavlink.mavlink.flightControllers")
@ComponentScan("gui.com.dronegcs.console_plugin.services")
@Component("opChangeFlightControllerQuad")
public class OpChangeFlightControllerQuad extends OperationHandler {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get keyboard controller")
	private KeyBoardController keyBoardController;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator runtimeValidator;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger")
	private Logger logger;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager when changing flight mode")
	private DialogManagerSvc dialogManagerSvc;

	private FlightController requiredFlightMode;
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public boolean go() throws InterruptedException {
		loggerDisplayerSvc.logGeneral("Start MavlinkTakeoff Phase");

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		if (dialogManagerSvc.showYesNoDialog("Would you like to force flight mode change?", "", true) == 1)
			return false;
		
    	keyBoardController.HoldIfNeeded();
    	
    	switch (requiredFlightMode) {
    	case KEYBOARD:
    		keyBoardController.ReleaseIfNeeded();
    		keyBoardController.Activate();
    		int eAvg = drone.getRC().getAverageThrust();
    		loggerDisplayerSvc.logGeneral("Setting Keyboard Thrust starting value to " + eAvg);
    		keyBoardController.SetThrust(eAvg);
    		break;
    	case REMOTE:
    		keyBoardController.ReleaseIfNeeded();
    		keyBoardController.Deactivate();
    		int[] rcOutputs = {0, 0, 0, 0, 0, 0, 0, 0};
    		MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
    		try {
				Thread.sleep(200);
				MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
        		Thread.sleep(200);
        		MavLinkRC.sendRcOverrideMsg(drone, rcOutputs);
			} catch (InterruptedException e1) {
				logger.LogErrorMessege(e1.getMessage());
				dialogManagerSvc.showErrorMessageDialog("Failed to send RC Override command", e1);
			}
    		
    		break;
    	}
		return super.go();
	}

	public void setFlightMode(FlightController flightmode) {
		requiredFlightMode = flightmode;
	}

	public Drone getDrone() {
		return drone;
	}

	public FlightController getRequiredControler() {
		return requiredFlightMode;
	}
}
