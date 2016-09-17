package org.droidplanner.core.gcs.follow;

import mavlink.is.utils.units.Length;

import org.droidplanner.core.model.Drone;

public class FollowRight extends FollowHeadingAngle {

	public FollowRight(Drone drone, Length radius) {
		super(drone, radius, 90.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.RIGHT;
	}

}
