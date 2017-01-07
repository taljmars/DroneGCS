package mavlink.core.gcs.follow;

import gui.is.Coordinate;
import gui.is.GeoTools;
import mavlink.is.drone.Drone;
import mavlink.is.gcs.follow.FollowAlgorithm;
import mavlink.is.location.Location;

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