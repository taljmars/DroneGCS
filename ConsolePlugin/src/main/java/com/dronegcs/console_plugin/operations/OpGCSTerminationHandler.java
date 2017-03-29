package com.dronegcs.console_plugin.operations;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dronegcs.console_plugin.services.DialogManagerSvc;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
import com.dronegcs.gcsis.logger.Logger;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.gcsis.validations.RuntimeValidator;
import com.dronegcs.gcsis.validations.ValidatorResponse;

@Component
public class OpGCSTerminationHandler extends OperationHandler {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get GUI event publisher")
	protected EventPublisherSvc eventPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.dronegcs.gcsis.logger")
	private Logger logger;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
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
    		System.out.println("Bye Bye");
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
