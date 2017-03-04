package main.java.mavlink_core.mavlink.core.gcs.follow;

import mavlink.drone.Drone;

public class FollowLead extends FollowHeadingAngle {

	public FollowLead(Drone drone, double radius) {
		super(drone, radius, 0.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEAD;
	}

}
