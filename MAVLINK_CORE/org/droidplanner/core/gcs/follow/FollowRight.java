package org.droidplanner.core.gcs.follow;

import mavlink.is.model.Drone;
import mavlink.is.utils.units.Length;

public class FollowRight extends FollowHeadingAngle {

	public FollowRight(Drone drone, Length radius) {
		super(drone, radius, 90.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.RIGHT;
	}

}
