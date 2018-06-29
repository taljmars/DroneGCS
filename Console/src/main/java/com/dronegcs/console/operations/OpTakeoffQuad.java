package com.dronegcs.console.operations;


import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import javafx.application.Platform;
import javax.annotation.PostConstruct;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import com.dronegcs.console_plugin.validations.QuadIsArmed;
import com.dronegcs.mavlink.is.drone.Drone;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import org.springframework.util.Assert;

@ComponentScan("tools.com.dronegcs.console_plugin.validations")
@ComponentScan("gui.com.dronegcs.console_plugin.services")
@Component("opTakeoffQuad")
public class OpTakeoffQuad extends OperationHandler {

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OpTakeoffQuad.class);
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	@QuadIsArmed
	private Drone drone;
	
	@Min(value=1, message="Expected height must be above 1m")
    @Max(value=50, message="Expected height must be less than 50m")
	private double expectedValue;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager when setting takeoff")
	private DialogManagerSvc dialogManagerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator runtimeValidator;
	
	static int called;
	@PostConstruct
	public void init() {
		Assert.isTrue(++called == 1, "Not a Singleton");
	}
	
	@Override
	public boolean go() throws InterruptedException {
		loggerDisplayerSvc.logGeneral("Start MavlinkTakeoff Phase");

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			return false;
		
		drone.getState().doTakeoff(expectedValue);
		int takeoff_waiting_time = 15000; // 15 seconds
		long sleep_time = 1000;
		int retry = (int) (takeoff_waiting_time / sleep_time);
		while (retry > 0) {
			double alt = drone.getAltitude().getAltitude();
			if (alt >= expectedValue * 0.95 && alt <= expectedValue * 1.05 )
				break;
			LOGGER.debug("Sleeps for " + sleep_time + " ms (retries " + retry + ")");
			loggerDisplayerSvc.logGeneral("Waiting for takeoff to finish (" + retry + ")");
			loggerDisplayerSvc.logGeneral("Current height: " + drone.getAltitude().getAltitude() + ", Target height: " + expectedValue);
			Thread.sleep(sleep_time);
			retry--;
		}
		
		if (retry <= 0) {
			loggerDisplayerSvc.logError("Failed to lift quad");
			Platform.runLater( () -> dialogManagerSvc.showAlertMessageDialog("Failed to lift quadcopter, taking off was canceled"));
			LOGGER.error(getClass().getName() + "Failed to lift quadcopter, taking off was canceled");
			return false;
		}
		
		loggerDisplayerSvc.logGeneral("MavlinkTakeoff done! Quad height is " + drone.getAltitude().getAltitude() + "m");
		
		return super.go();
	}

	public void setTargetHeight(double real_value) {
		LOGGER.debug(getClass().getName() + " Required height is " + real_value);
		expectedValue = real_value;
	}
}
