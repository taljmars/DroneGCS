package mavlink.core.gcs.follow;

import mavlink.is.model.Drone;
import mavlink.is.utils.units.Length;

public class FollowLead extends FollowHeadingAngle {

	public FollowLead(Drone drone, Length radius) {
		super(drone, radius, 0.0);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEAD;
	}

}
