package gui.core.operations.internal;

import gui.core.operations.OperationHandler;
import gui.is.services.LoggerDisplayerSvc;

import javax.annotation.Resource;
import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

import mavlink.is.drone.Drone;
import mavlink.is.protocol.msgbuilder.MavLinkArm;

@Component("armQuad")
public class ArmQuad extends OperationHandler {
	
	@Resource(name = "drone")
	private Drone drone;
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Override
	public boolean go() throws InterruptedException {
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
			JOptionPane.showMessageDialog(null, "Failed to arm quadcopter, taking off was canceled");
			System.out.println(getClass().getName() + "Failed to arm quadcopter, taking off was canceled");
			loggerDisplayerSvc.logError("Failed to arm quad");
			return false;
		}
		
		return super.go();
	}

}
