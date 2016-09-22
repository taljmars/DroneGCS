package mavlink.core.gcs.follow;

import logger.Logger;
import mavlink.core.gcs.follow.FollowAlgorithm.FollowModes;
import mavlink.core.gcs.roi.ROIEstimator;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.Handler;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.variables.GuidedPoint;
import mavlink.is.drone.variables.State;
import mavlink.is.location.Location;
import mavlink.is.location.LocationFinder;
import mavlink.is.location.LocationReceiver;
import mavlink.is.protocol.msgbuilder.MavLinkROI;
import mavlink.is.utils.units.Length;

public class Follow implements OnDroneListener, LocationReceiver {

	/** Set of return value for the 'toggleFollowMeState' method.*/
	public enum FollowStates {
		FOLLOW_INVALID_STATE, FOLLOW_DRONE_NOT_ARMED, FOLLOW_DRONE_DISCONNECTED, FOLLOW_START, FOLLOW_RUNNING, FOLLOW_END
	}

	private FollowStates state = FollowStates.FOLLOW_INVALID_STATE;
	private Drone drone;

	private ROIEstimator roiEstimator;
	private LocationFinder locationFinder;
	private FollowAlgorithm followAlgorithm;

	public Follow(Drone drone, Handler handler, LocationFinder locationFinder) {
		this.drone = drone;
		followAlgorithm = FollowAlgorithm.FollowModes.ABOVE.getAlgorithmType(drone);
		this.locationFinder = locationFinder;
		locationFinder.addLocationListener(this);
		roiEstimator = new ROIEstimator(handler, drone);
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

	public Length getRadius() {
		return followAlgorithm.radius;
	}

	@Override
	public void onLocationChanged(Location location) {
		Logger.LogDesignedMessege("Location changed");
		if (location.isAccurate()) {
			Logger.LogDesignedMessege("Process new location");
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
