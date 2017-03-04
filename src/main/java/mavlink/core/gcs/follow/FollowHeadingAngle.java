package mavlink.core.gcs.follow;

import is.mavlink.drone.Drone;
import is.mavlink.gcs.follow.FollowAlgorithm;
import is.mavlink.location.Location;
import tools.geoTools.Coordinate;
import tools.geoTools.GeoTools;

public abstract class FollowHeadingAngle extends FollowAlgorithm {

	protected double angleOffset;

	protected FollowHeadingAngle(Drone drone, double radius, double angleOffset) {
		super(drone, radius);
		this.angleOffset = angleOffset;
	}

	@Override
	public void processNewLocation(Location location) {

		Coordinate gcsCoord = new Coordinate(location.getCoord().getLat(), location.getCoord().getLon());
		double bearing = location.getBearing();

		Coordinate goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing + angleOffset, radius);
		drone.getGuidedPoint().newGuidedCoord(goCoord);
	}

}