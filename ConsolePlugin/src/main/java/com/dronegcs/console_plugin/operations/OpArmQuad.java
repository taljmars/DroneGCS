package com.dronegcs.console_plugin.operations;

import com.dronegcs.console_plugin.services.DialogManagerSvc;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import javafx.application.Platform;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkArm;

@Component
public class OpArmQuad extends OperationHandler {

	private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OpArmQuad.class);
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager when arming quad")
	private DialogManagerSvc dialogManagerSvc;
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
	}
	
	@Override
	public boolean go() throws InterruptedException {
		loggerDisplayerSvc.logGeneral("Start Arm Phase");
		
		if (drone.getState().isArmed()) {
			loggerDisplayerSvc.logGeneral("Drone already armed");
			return super.go();
		}
		
		loggerDisplayerSvc.logGeneral("Arming Quad");
		MavLinkArm.sendArmMessage(drone, true);
		int armed_waiting_time = 5000; // 5 seconds
		long sleep_time = 1000;
		int retry = (int) (armed_waiting_time / sleep_time);
		while (retry > 0) {
			if (drone.getState().isArmed())
				break;
			System.out.println("Sleeps for " + sleep_time + " ms (retries " + retry + ")");
			loggerDisplayerSvc.logGeneral("Waiting for arming approval (" + retry + ")");
			Thread.sleep(sleep_time);
			retry--;
		}
		
		if (retry <= 0) {
			loggerDisplayerSvc.logError("Failed to arm quad");
			Platform.runLater( () -> dialogManagerSvc.showAlertMessageDialog("Failed to arm quadcopter, taking off was canceled"));
			LOGGER.error(getClass().getName() + "Failed to arm quadcopter, taking off was canceled");
			
			return false;
		}
		
		return super.go();
	}

}
