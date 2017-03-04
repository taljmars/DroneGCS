package mavlink.core.gcs.follow;

import is.mavlink.drone.Drone;
import is.mavlink.gcs.follow.FollowAlgorithm;
import is.mavlink.location.Location;
import geoTools.Coordinate;

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
