package mavlink.core.gcs.follow;

import mavlink.drone.Drone;
import mavlink.gcs.follow.FollowAlgorithm;
import mavlink.location.Location;
import tools.geoTools.Coordinate;

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
