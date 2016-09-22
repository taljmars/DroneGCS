package mavlink.core.gcs.follow;

import mavlink.is.drone.Drone;
import mavlink.is.location.Location;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.geoTools.GeoTools;
import mavlink.is.utils.units.Length;

public abstract class FollowHeadingAngle extends FollowAlgorithm {

	protected double angleOffset;

	protected FollowHeadingAngle(Drone drone, Length radius, double angleOffset) {
		super(drone, radius);
		this.angleOffset = angleOffset;
	}

	@Override
	public void processNewLocation(Location location) {

		Coord2D gcsCoord = new Coord2D(location.getCoord().getLat(), location.getCoord().getLng());
		double bearing = location.getBearing();

		Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing + angleOffset,
				radius.valueInMeters());
		drone.getGuidedPoint().newGuidedCoord(goCoord);
	}

}