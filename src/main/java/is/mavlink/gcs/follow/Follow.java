package is.mavlink.gcs.follow;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import is.logger.Logger;
import is.mavlink.drone.Drone;
import is.mavlink.drone.DroneVariable;
import is.mavlink.drone.DroneInterfaces.DroneEventsType;
import is.mavlink.drone.DroneInterfaces.OnDroneListener;
import is.mavlink.drone.variables.GuidedPoint;
import is.mavlink.drone.variables.State;
import is.mavlink.gcs.follow.FollowAlgorithm.FollowModes;
import is.mavlink.location.Location;
import is.mavlink.location.LocationFinder;
import is.mavlink.location.LocationReceiver;
import is.mavlink.protocol.msgbuilder.MavLinkROI;

@ComponentScan("mavlink.is.gcs.roi")
@Component("follow")
public class Follow extends DroneVariable implements OnDroneListener, LocationReceiver {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;

	/** Set of return value for the 'toggleFollowMeState' method.*/
	public enum FollowStates {
		FOLLOW_INVALID_STATE, FOLLOW_DRONE_NOT_ARMED, FOLLOW_DRONE_DISCONNECTED, FOLLOW_START, FOLLOW_RUNNING, FOLLOW_END
	}

	private FollowStates state = FollowStates.FOLLOW_INVALID_STATE;
	
	//@Resource(name = "locationFinder")
	@Autowired
	private LocationFinder locationFinder;

	//@Resource(name = "roiEstimator")
	@Autowired
	private LocationReceiver roiEstimator;
	
	private FollowAlgorithm followAlgorithm;
	
	static int called;
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		followAlgorithm = FollowAlgorithm.FollowModes.ABOVE.getAlgorithmType(drone);
		locationFinder.addLocationListener(this);
		drone.addDroneListener(this);
	}

	public void toggleFollowMeState() {
		final State droneState = drone.getState();
		if (droneState == null) {
			state = FollowStates.FOLLOW_INVALID_STATE;
			return;
		}

		if (isEnabled()) {
			disableFollowMe();
		} else {
			if (drone.getMavClient().isConnected()) {
				if (drone.getState().isArmed()) {
					GuidedPoint.changeToGuidedMode(drone);
					enableFollowMe();
				} else {
					state = FollowStates.FOLLOW_DRONE_NOT_ARMED;
				}
			} else {
				state = FollowStates.FOLLOW_DRONE_DISCONNECTED;
				
			}
		}
	}

	private void enableFollowMe() {
		locationFinder.enableLocationUpdates();
		state = FollowStates.FOLLOW_START;
		drone.notifyDroneEvent(DroneEventsType.FOLLOW_START);
	}

	private void disableFollowMe() {
		locationFinder.disableLocationUpdates();
		if (isEnabled()) {
			state = FollowStates.FOLLOW_END;
			MavLinkROI.resetROI(drone);

            if(GuidedPoint.isGuidedMode(drone)) {
                drone.getGuidedPoint().pauseAtCurrentLocation();
            }

			drone.notifyDroneEvent(DroneEventsType.FOLLOW_STOP);
		}
	}

	public boolean isEnabled() {
		return state == FollowStates.FOLLOW_RUNNING || state == FollowStates.FOLLOW_START;
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MODE:
			if (!GuidedPoint.isGuidedMode(drone)) {
				disableFollowMe();
			}
			break;

		case DISCONNECTED:
			disableFollowMe();
			break;
		default:
		}
	}

	public double getRadius() {
		return followAlgorithm.radius;
	}

	@Override
	public void onLocationChanged(Location location) {
		logger.LogDesignedMessege("Location changed");
		if (location.isAccurate()) {
			logger.LogDesignedMessege("Process new location");
			state = FollowStates.FOLLOW_RUNNING;
            followAlgorithm.processNewLocation(location);
            roiEstimator.onLocationChanged(location);
		}
		else {
			state = FollowStates.FOLLOW_START;
		}

		drone.notifyDroneEvent(DroneEventsType.FOLLOW_UPDATE);
	}

	public void setType(FollowModes item) {
		followAlgorithm = item.getAlgorithmType(drone);
		drone.notifyDroneEvent(DroneEventsType.FOLLOW_CHANGE_TYPE);
	}

	public void changeRadius(double radius) {
		followAlgorithm.changeRadius(radius);
	}

	public void cycleType() {
		setType(followAlgorithm.getType().next());
	}

	public FollowModes getType() {
		return followAlgorithm.getType();
	}

	public FollowStates getState() {
		return state;
	}
}
