package gui.core.operations.internal;

import gui.core.operations.OperationHandler;
import gui.is.services.LoggerDisplayerSvc;

import javax.annotation.Resource;
import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

import mavlink.is.drone.Drone;
import mavlink.is.utils.units.Altitude;

@Component("takeoffQuad")
public class TakeoffQuad extends OperationHandler {
	
	@Resource(name = "drone")
	private Drone drone;
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	private double expectedValue;
	
	@Override
	public boolean go() throws InterruptedException {
		loggerDisplayerSvc.logGeneral("Start Takeoff Phase");
		drone.getState().doTakeoff(new Altitude(expectedValue));
		int takeoff_waiting_time = 15000; // 15 seconds
		long sleep_time = 1000;
		int retry = (int) (takeoff_waiting_time / sleep_time);
		while (retry > 0) {
			double alt = drone.getAltitude().getAltitude();
			if (alt >= expectedValue * 0.95 && alt <= expectedValue * 1.05 )
				break;
			System.out.println("Sleeps for " + sleep_time + " ms (retries " + retry + ")");
			loggerDisplayerSvc.logGeneral("Waiting for takeoff to finish (" + retry + ")");
			loggerDisplayerSvc.logGeneral("Current height: " + drone.getAltitude().getAltitude() + ", Target height: " + expectedValue);
			Thread.sleep(sleep_time);
			retry--;
		}
		
		if (retry <= 0) {
			JOptionPane.showMessageDialog(null, "Failed to lift quadcopter, taking off was canceled");
			System.out.println(getClass().getName() + "Failed to lift quadcopter, taking off was canceled");
			loggerDisplayerSvc.logError("Failed to lift quad");
			return false;
		}
		
		loggerDisplayerSvc.logGeneral("Takeoff done! Quad height is " + drone.getAltitude().getAltitude() + "m");
		
		return super.go();
	}

	public void setTargetHeight(double real_value) {
		expectedValue = real_value;
	}
}
