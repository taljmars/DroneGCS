package gui.core.operations.internal;

import gui.core.dashboard.Dashboard;
import gui.core.operations.OperationHandler;

import javax.swing.JOptionPane;

import mavlink.is.drone.Drone;
import mavlink.is.protocol.msgbuilder.MavLinkArm;

public class ArmQuad extends OperationHandler {
	
	Drone drone = null;
	
	public ArmQuad(Drone drone) {
		this.drone = drone;
	}
	
	@Override
	public boolean go() throws InterruptedException {
		if (drone.getState().isArmed()) {
			Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Drone already armed");
			return super.go();
		}
		
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Arming Quad");
		MavLinkArm.sendArmMessage(Dashboard.drone, true);
		int armed_waiting_time = 5000; // 5 seconds
		long sleep_time = 1000;
		int retry = (int) (armed_waiting_time / sleep_time);
		while (retry > 0) {
			if (drone.getState().isArmed())
				break;
			System.out.println("Sleeps for " + sleep_time + " ms (retries " + retry + ")");
			Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Waiting for arming approval (" + retry + ")");
			Thread.sleep(sleep_time);
			retry--;
		}
		
		if (retry <= 0) {
			JOptionPane.showMessageDialog(null, "Failed to arm quadcopter, taking off was canceled");
			System.out.println(getClass().getName() + "Failed to arm quadcopter, taking off was canceled");
			Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Failed to arm quad");
			return false;
		}
		
		return super.go();
	}

}
