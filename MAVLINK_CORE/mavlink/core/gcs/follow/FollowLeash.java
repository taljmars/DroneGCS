package mavlink.core.gcs.follow;

import mavlink.core.gcs.location.Location;
import mavlink.is.model.Drone;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.geoTools.GeoTools;
import mavlink.is.utils.units.Length;

public class FollowLeash extends FollowAlgorithm {

	public FollowLeash(Drone drone, Length radius) {
		super(drone, radius);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEASH;
	}

	@Override
	public void processNewLocation(Location location) {
		final Coord2D locationCoord = location.getCoord();
		final Coord2D dronePosition = drone.getGps().getPosition();

		if (locationCoord == null || dronePosition == null) {
			return;
		}

		final double radiusInMeters = radius.valueInMeters();
		if (GeoTools.getDistance(locationCoord, dronePosition).valueInMeters() > radiusInMeters) {
			double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(locationCoord,
					dronePosition);
			Coord2D goCoord = GeoTools.newCoordFromBearingAndDistance(locationCoord,
					headingGCStoDrone, radiusInMeters);
			drone.getGuidedPoint().newGuidedCoord(goCoord);
		}
	}

}
