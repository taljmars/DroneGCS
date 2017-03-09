package com.dronegcs.mavlink.core.mavlink.gcs.follow;

import com.dronegcs.mavlink.is.mavlink.drone.Drone;
import com.dronegcs.mavlink.is.mavlink.gcs.follow.FollowAlgorithm;
import com.dronegcs.mavlink.is.mavlink.location.Location;
import geoTools.Coordinate;
import geoTools.GeoTools;

public class FollowLeash extends FollowAlgorithm {

	public FollowLeash(Drone drone, double radius) {
		super(drone, radius);
	}

	@Override
	public FollowModes getType() {
		return FollowModes.LEASH;
	}

	@Override
	public void processNewLocation(Location location) {
		final Coordinate locationCoord = location.getCoord();
		final Coordinate dronePosition = drone.getGps().getPosition();

		if (locationCoord == null || dronePosition == null) {
			return;
		}

		final double radiusInMeters = radius;
		if (GeoTools.getDistance(locationCoord, dronePosition) > radiusInMeters) {
			double headingGCStoDrone = GeoTools.getHeadingFromCoordinates(locationCoord,
					dronePosition);
			Coordinate goCoord = GeoTools.newCoordFromBearingAndDistance(locationCoord,
					headingGCStoDrone, radiusInMeters);
			drone.getGuidedPoint().newGuidedCoord(goCoord);
		}
	}

}
