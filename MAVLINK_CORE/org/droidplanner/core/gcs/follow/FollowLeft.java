package org.droidplanner.core.gcs.follow;

import mavlink.is.model.Drone;
import mavlink.is.utils.units.Length;

public class FollowLeft extends FollowHeadingAngle {

	public FollowLeft(Drone drone, Length radius) {
		super(drone, radius, -90.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEFT;
	}

}
