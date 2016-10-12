package gui.core.operations.internal;

import gui.core.operations.OperationHandler;
import gui.is.interfaces.KeyBoardControler;
import gui.is.services.LoggerDisplayerSvc;
import gui.is.validations.RuntimeValidator;
import gui.is.validations.SwitchToRC;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import logger.Logger;
import mavlink.core.flightControlers.FlightControler;
import mavlink.is.drone.Drone;
import mavlink.is.protocol.msgbuilder.MavLinkRC;

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
	
		if (!RuntimeValidator.validate(this))
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
				Logger.LogErrorMessege(e1.getMessage());
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
