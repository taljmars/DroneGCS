package mavlink.is.drone.variables;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import gui.is.Coordinate;
import gui.is.services.DialogManagerSvc;
import gui.is.services.LoggerDisplayerSvc;
import mavlink.is.drone.DroneVariable;
import mavlink.is.drone.DroneInterfaces.Clock;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.Handler;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.MavLinkModes;
import validations.RuntimeValidator;

@Component("state")
public class State extends DroneVariable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4184572064576193573L;
	private static final long failsafeOnScreenTimeout = 5000;
	private String warning = "";
	private boolean armed = false;
	private boolean isFlying = false;
	private ApmModes mode = ApmModes.UNKNOWN;

	// flightTimer
	// ----------------
	private long startTime = 0;
	private long elapsedFlightTime = 0;
	
	@Resource(name = "clock")
	@NotNull( message = "Internal Error: Clock Field wasn't initialized" )
	private Clock clock;

	@Resource(name = "handler")
	@NotNull( message = "Internal Error: Handler Field wasn't initialized" )
	public Handler handler;
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull( message = "Internal Error: Logger Displayer Field wasn't initialized" )
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "dialogManagerSvc")
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Resource(name = "validator")
	@NotNull(message = "Internal Error: Failed to get validator")
	private RuntimeValidator validator;
	
	public Runnable watchdogCallback = () -> removeWarning();
	
	static int called;
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		validator.validate(this);
		resetFlightTimer();
	}

	public boolean isWarning() {
		return !warning.equals("");
	}

	public boolean isArmed() {
		return armed;
	}

	public boolean isFlying() {
		return isFlying;
	}

	public ApmModes getMode() {
		return mode;
	}

	public String getWarning() {
		return warning;
	}

	public void setIsFlying(boolean newState) {
		if (newState != isFlying) {
			isFlying = newState;
			drone.notifyDroneEvent(DroneEventsType.STATE);
			if (isFlying) {
				startTimer();
			} else {
				stopTimer();
			}
		}
	}

	public void setWarning(String newFailsafe) {
		if (!this.warning.equals(newFailsafe)) {
			this.warning = newFailsafe;
			drone.notifyDroneEvent(DroneEventsType.AUTOPILOT_WARNING);
		}
		handler.removeCallbacks(watchdogCallback);
		handler.postDelayed(watchdogCallback, failsafeOnScreenTimeout);
	}

	public void setArmed(boolean newState) {
		if (this.armed != newState) {
			this.armed = newState;
			drone.notifyDroneEvent(DroneEventsType.ARMING);
			if (newState) {
				drone.getWaypointManager().getWaypoints();
			}else{
				if (mode == ApmModes.ROTOR_RTL || mode == ApmModes.ROTOR_LAND) {
					changeFlightMode(ApmModes.ROTOR_LOITER);  // When disarming set the mode back to loiter so we can do a takeoff in the future.					
				}
			}
		}
	}

	public void doTakeoff(double alt) {
		drone.getGuidedPoint().doGuidedTakeoff(alt);
	}
	
	public void doTakeoff(Coordinate coord, double alt) {
		drone.getGuidedPoint().doGuidedTakeoff(coord, alt);
	}

	public void setMode(ApmModes mode) {
		if (this.mode != mode) {
			this.mode = mode;
			System.out.println(getClass().getName() + " New Mode!!");
			System.out.println(getClass().getName() + " " + mode.getName());
			drone.notifyDroneEvent(DroneEventsType.MODE);
		}
	}

	public void changeFlightMode(ApmModes mode) {
		if (mode == null) {
			loggerDisplayerSvc.logError("Unexpected Mode value: Null");
			return;
		}
		
		if (ApmModes.isValid(mode)) {
			loggerDisplayerSvc.logGeneral("Start Mission - Change to " + mode.getName());
			System.out.println(getClass().getName() + mode.getName());
			MavLinkModes.changeFlightMode(drone, mode);
		}
	}

	protected void removeWarning() {
		setWarning("");
	}

	// flightTimer
	// ----------------

	public void resetFlightTimer() {
		elapsedFlightTime = 0;
		startTime = clock.elapsedRealtime();
	}

	public void startTimer() {
		startTime = clock.elapsedRealtime();
	}

	public void stopTimer() {
		// lets calc the final elapsed timer
		elapsedFlightTime += clock.elapsedRealtime() - startTime;
		startTime = clock.elapsedRealtime();
	}

	public long getFlightTime() {
		if (isFlying) {
			// calc delta time since last checked
			elapsedFlightTime += clock.elapsedRealtime() - startTime;
			startTime = clock.elapsedRealtime();
		}
		return elapsedFlightTime / 1000;
	}

}