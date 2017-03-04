package core.operations;

import gui.operations.OperationHandler;
import gui.services.DialogManagerSvc;
import gui.services.LoggerDisplayerSvc;
import logger.Logger;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import devices.KeyBoardController;
import mavlink.core.flightControllers.FlightController;
import mavlink.core.validations.SwitchToRC;
import mavlink.drone.Drone;
import mavlink.protocol.msgbuilder.MavLinkRC;
import validations.RuntimeValidator;

@ComponentScan("tools.validations")
@ComponentScan("mavlink.core.flightControllers")
@ComponentScan("gui.services")
@SwitchToRC
@Component("opChangeFlightControllerQuad")
public class OpChangeFlightControllerQuad extends OperationHandler {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get keyboard controller")
	private KeyBoardController keyBoardController;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator validator;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger")
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
		loggerDisplayerSvc.logGeneral("Start Takeoff Phase");
	
		if (!validator.validate(this)) {
			if (dialogManagerSvc.showYesNoDialog("Would you like to force flight mode change?", "", true) == 1)
				return false;
		}
		
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
