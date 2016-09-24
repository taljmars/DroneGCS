package mavlink.core.gcs.follow;

import mavlink.is.drone.Drone;
import mavlink.is.gcs.follow.FollowAlgorithm;
import mavlink.is.location.Location;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.geoTools.GeoTools;
import mavlink.is.utils.math.MathUtil;
import mavlink.is.utils.units.Length;

public class FollowCircle extends FollowAlgorithm {

	/**
	 * °/s
	 */
	private double circleStep = 2;
	private double circleAngle = 0.0;

	public FollowCircle(Drone drone, Length radius, double rate) {
		super(drone, radius);
		circleStep = rate;
	}

	@Override
	public FollowModes getType() {
		return FollowModes.CIRCLE;
	}

	@Override
	public void processNewLocation(Location location) {
		Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, circleAngle,
				radius.valueInMeters());
		circleAngle = MathUtil.constrainAngle(circleAngle + circleStep);
		drone.getGuidedPoint().newGuidedCoord(goCoord);
	}
}
