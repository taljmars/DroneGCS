package mavlink.core.gcs.follow;

import mavlink.core.gcs.location.Location;
import mavlink.is.model.Drone;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.units.Length;

public class FollowAbove extends FollowAlgorithm {

	public FollowAbove(Drone drone, Length radius) {
		super(drone, radius);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.ABOVE;
	}

	@Override
	public void processNewLocation(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
		drone.getGuidedPoint().newGuidedCoord(gcsCoord);
	}

}
