package com.dronegcs.mavlink.core.mavlink.gcs.follow;

import com.dronegcs.mavlink.is.mavlink.drone.Drone;

public class FollowRight extends FollowHeadingAngle {

	public FollowRight(Drone drone, double radius) {
		super(drone, radius, 90.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.RIGHT;
	}

}
