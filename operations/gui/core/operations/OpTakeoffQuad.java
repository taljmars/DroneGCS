package gui.core.operations;

import gui.is.operations.OperationHandler;
import gui.is.services.DialogManagerSvc;
import gui.is.services.LoggerDisplayerSvc;
import javafx.application.Platform;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import mavlink.core.validations.QuadIsArmed;
import mavlink.is.drone.Drone;
import validations.RuntimeValidator;

@ComponentScan("tools.validations")
@ComponentScan("gui.is.services")
@Component("opTakeoffQuad")
public class OpTakeoffQuad extends OperationHandler {
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "drone")
	@NotNull(message = "Internal Error: Failed to get drone")
	@QuadIsArmed
	private Drone drone;
	
	@Min(value=1, message="Expected height must be above 1m")
    @Max(value=50, message="Expected height must be less than 50m")
	private double expectedValue;
	
	@Resource(name = "dialogManagerSvc")
	@NotNull(message = "Internal Error: Failed to get dialog manager when setting takeoff")
	private DialogManagerSvc dialogManagerSvc;
	
	@Resource(name = "validator")
	@NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator validator;
	
	static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	@Override
	public boolean go() throws InterruptedException {
		loggerDisplayerSvc.logGeneral("Start Takeoff Phase");
	
		if (!validator.validate(this))
			return false;
		
		drone.getState().doTakeoff(expectedValue);
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
			loggerDisplayerSvc.logError("Failed to lift quad");
			Platform.runLater( () -> dialogManagerSvc.showAlertMessageDialog("Failed to lift quadcopter, taking off was canceled"));
			System.out.println(getClass().getName() + "Failed to lift quadcopter, taking off was canceled");
			return false;
		}
		
		loggerDisplayerSvc.logGeneral("Takeoff done! Quad height is " + drone.getAltitude().getAltitude() + "m");
		
		return super.go();
	}

	public void setTargetHeight(double real_value) {
		System.out.println(getClass().getName() + " Required height is " + real_value);
		expectedValue = real_value;
	}
}
