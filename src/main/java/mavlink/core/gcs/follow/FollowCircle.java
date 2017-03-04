package mavlink.core.gcs.follow;

import is.mavlink.drone.Drone;
import is.mavlink.gcs.follow.FollowAlgorithm;
import is.mavlink.location.Location;
import geoTools.Coordinate;
import geoTools.GeoTools;
import geoTools.MathUtil;

public class FollowCircle extends FollowAlgorithm {

	/**
	 * °/s
	 */
	private double circleStep = 2;
	private double circleAngle = 0.0;

	public FollowCircle(Drone drone, double radius, double rate) {
		super(drone, radius);
		circleStep = rate;
	}

	@Override
	public FollowModes getType() {
		return FollowModes.CIRCLE;
	}

	@Override
	public void processNewLocation(Location location) {
		Coordinate gcsCoord = new Coordinate(location.getCoord().getLat(), location.getCoord().getLon());
		Coordinate goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, circleAngle, radius);
		circleAngle = MathUtil.constrainAngle(circleAngle + circleStep);
		drone.getGuidedPoint().newGuidedCoord(goCoord);
	}
}
