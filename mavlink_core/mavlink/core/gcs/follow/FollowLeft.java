package mavlink.core.gcs.follow;

import mavlink.drone.Drone;

public class FollowLeft extends FollowHeadingAngle {

	public FollowLeft(Drone drone, double radius) {
		super(drone, radius, -90.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEFT;
	}

}
