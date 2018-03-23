package com.dronegcs.console.operations;

import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console_plugin.remote_services_wrappers.LoginSvcRemoteWrapper;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

@Component
public class OpGCSTerminationHandler extends OperationHandler {

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OpArmQuad.class);
	
	@Autowired @NotNull(message = "Internal Error: Failed to get GUI event publisher")
	protected EventPublisherSvc eventPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger")
	private Logger logger;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;

	@Autowired @NotNull(message = "Internal Error: Failed to get loginSvcRemote")
	private LoginSvcRemoteWrapper loginSvcRemote;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());
	}

	@Override
	public boolean go() throws InterruptedException {
		if (DialogManagerSvc.YES_OPTION == dialogManagerSvc.showConfirmDialog("Are you sure you wand to exit?", "")) {
			LOGGER.debug("Bye Bye");

			loginSvcRemote.logout();

    		logger.LogGeneralMessege("");
    		logger.LogGeneralMessege("Summary:");
    		logger.LogGeneralMessege("--------");
    		logger.LogGeneralMessege("Traveled distance: " + drone.getGps().getDistanceTraveled() + "m");
    		logger.LogGeneralMessege("Max Height: " + drone.getAltitude().getMaxAltitude() + "m");
    		logger.LogGeneralMessege("Max Speed: " + drone.getSpeed().getMaxAirSpeed().valueInMetersPerSecond() + "m/s (" + ((int) (drone.getSpeed().getMaxAirSpeed().valueInMetersPerSecond()*3.6)) + "km/h)");
    		logger.LogGeneralMessege("Flight time: " + drone.getState().getFlightTime() + "");
    		eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.EXIT, this));
			logger.close();
			System.exit(0);
    	}
		
		return false;
	}
}
