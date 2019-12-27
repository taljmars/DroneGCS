package com.dronegcs.console.operations;

import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import javafx.application.Platform;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkArm;
import org.springframework.util.Assert;

@Component
public class OpArmDrone extends OperationHandler {

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OpArmDrone.class);
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager when arming drone")
	private DialogManagerSvc dialogManagerSvc;
	
	static int called;
	@PostConstruct
	public void init() {
		Assert.isTrue(++called == 1, "Not a Singleton");
	}
	
	@Override
	public boolean go() throws InterruptedException {
		loggerDisplayerSvc.logGeneral("Start Arm Phase");
		
		if (drone.getState().isArmed()) {
			loggerDisplayerSvc.logGeneral("Drone already armed");
			return super.go();
		}
		
		loggerDisplayerSvc.logGeneral("Arming Drone");
		MavLinkArm.sendArmMessage(drone, true);
		int armed_waiting_time = 5000; // 5 seconds
		long sleep_time = 1000;
		int retry = (int) (armed_waiting_time / sleep_time);
		while (retry > 0) {
			if (drone.getState().isArmed())
				break;
			LOGGER.debug("Sleeps for " + sleep_time + " ms (retries " + retry + ")");
			loggerDisplayerSvc.logGeneral("Waiting for arming approval (" + retry + ")");
			Thread.sleep(sleep_time);
			retry--;
		}
		
		if (retry <= 0) {
			loggerDisplayerSvc.logError("Failed to arm drone");
			Platform.runLater( () -> dialogManagerSvc.showAlertMessageDialog("Failed to arm drone, taking off was canceled"));
			LOGGER.error(getClass().getName() + "Failed to arm drone, taking off was canceled");
			
			return false;
		}
		
		return super.go();
	}

}