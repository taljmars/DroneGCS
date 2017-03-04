package operations.core.operations;

import is.gui.operations.OperationHandler;
import is.gui.services.DialogManagerSvc;
import is.gui.services.LoggerDisplayerSvc;
import javafx.application.Platform;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import is.mavlink.drone.Drone;
import is.mavlink.protocol.msgbuilder.MavLinkArm;

@ComponentScan("gui.services")
@Component("opArmQuad")
public class OpArmQuad extends OperationHandler {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager when arming quad")
	private DialogManagerSvc dialogManagerSvc;
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
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
			System.out.println(getClass().getName() + "Failed to arm quadcopter, taking off was canceled");
			
			return false;
		}
		
		return super.go();
	}

}
