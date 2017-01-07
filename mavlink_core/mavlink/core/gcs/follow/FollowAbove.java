package mavlink.core.gcs.follow;

import gui.is.Coordinate;
import mavlink.is.drone.Drone;
import mavlink.is.gcs.follow.FollowAlgorithm;
import mavlink.is.location.Location;

public class FollowAbove extends FollowAlgorithm {

	public FollowAbove(Drone drone, double radius) {
		super(drone, radius);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.ABOVE;
	}

	@Override
	public void processNewLocation(Location location) {
		Coordinate gcsCoord = new Coordinate(location.getCoord().getLat(), location.getCoord().getLon());
		drone.getGuidedPoint().newGuidedCoord(gcsCoord);
	}

}
