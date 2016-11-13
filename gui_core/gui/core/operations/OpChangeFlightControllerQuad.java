package gui.core.operations;

import gui.is.KeyBoardControler;
import gui.is.operations.OperationHandler;
import gui.is.services.DialogManagerSvc;
import gui.is.services.LoggerDisplayerSvc;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import tools.logger.Logger;
import tools.validations.RuntimeValidator;
import mavlink.core.flightControlers.FlightControler;
import mavlink.core.validations.SwitchToRC;
import mavlink.is.drone.Drone;
import mavlink.is.protocol.msgbuilder.MavLinkRC;

@ComponentScan("tools.validations")
@ComponentScan("mavlink.core.flightControlers")
@SwitchToRC
@Component("opChangeFlightControllerQuad")
public class OpChangeFlightControllerQuad extends OperationHandler {
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "keyBoardControler")
	private KeyBoardControler keyBoardControler;
	
	@Resource(name = "drone")
	@NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Resource(name = "validator")
	@NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator validator;
	
	@Resource(name = "logger")
	@NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;
	
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;

	private FlightControler requiredFlightMode;
	
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
	
		if (!validator.validate(this))
			return false;
		
    	keyBoardControler.HoldIfNeeded();
    	
    	switch (requiredFlightMode) {
    	case KEYBOARD:
    		keyBoardControler.ReleaseIfNeeded();
    		keyBoardControler.Activate();
    		int eAvg = drone.getRC().getAverageThrust();
    		loggerDisplayerSvc.logGeneral("Setting Keyboard Thrust starting value to " + eAvg);
    		keyBoardControler.SetThrust(eAvg);
    		break;
    	case REMOTE:
    		keyBoardControler.ReleaseIfNeeded();
    		keyBoardControler.Deactivate();
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

	public void setFlightMode(FlightControler flightmode) {
		requiredFlightMode = flightmode;
	}

	public Drone getDrone() {
		return drone;
	}

	public FlightControler getRequiredControler() {
		return requiredFlightMode;
	}
}
