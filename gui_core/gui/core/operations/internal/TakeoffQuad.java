package gui.core.operations.internal;

import gui.core.dashboard.Dashboard;
import gui.core.operations.OperationHandler;

import javax.swing.JOptionPane;

import mavlink.is.drone.Drone;
import mavlink.is.utils.units.Altitude;

public class TakeoffQuad extends OperationHandler {
	
	private Drone drone;
	private double expectedValue;

	public TakeoffQuad(Drone drone, double expectedValue) {
		this.drone = drone;
		this.expectedValue = expectedValue;
	}
	
	@Override
	public boolean go() throws InterruptedException {
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Starting Takeoff");
		drone.getState().doTakeoff(new Altitude(expectedValue));
		int takeoff_waiting_time = 15000; // 15 seconds
		long sleep_time = 1000;
		int retry = (int) (takeoff_waiting_time / sleep_time);
		while (retry > 0) {
			double alt = drone.getAltitude().getAltitude();
			if (alt >= expectedValue * 0.95 && alt <= expectedValue * 1.05 )
				break;
			System.out.println("Sleeps for " + sleep_time + " ms (retries " + retry + ")");
			Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Waiting for takeoff to finish (" + retry + ")");
			Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Current height: " + drone.getAltitude().getAltitude() + ", Target height: " + expectedValue);
			Thread.sleep(sleep_time);
			retry--;
		}
		
		if (retry <= 0) {
			JOptionPane.showMessageDialog(null, "Failed to lift quadcopter, taking off was canceled");
			System.out.println(getClass().getName() + "Failed to lift quadcopter, taking off was canceled");
			Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Failed to lift quad");
			return false;
		}
		
		Dashboard.loggerDisplayerManager.addGeneralMessegeToDisplay("Takeoff done! Quad height is " + drone.getAltitude().getAltitude() + "m");
		
		return super.go();
	}
}
