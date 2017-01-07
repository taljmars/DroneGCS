package mavlink.is.gcs.roi;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import gui.is.Coordinate;
import gui.is.GeoTools;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.Handler;
import mavlink.is.location.Location;
import mavlink.is.location.LocationReceiver;
import mavlink.is.protocol.msgbuilder.MavLinkROI;

/**
 * Uses location data from Android's FusedLocation LocationManager at 1Hz and
 * calculates new points at 10Hz based on Last Location and Last Velocity.
 * 
 */
@Component("roiEstimator")
public class ROIEstimator implements LocationReceiver {

	private static final int TIMEOUT = 100;
	private Location realLocation;
	private long timeOfLastLocation;

	@Resource(name = "drone")
	private Drone drone;
	
	@Resource(name = "handler")
	private Handler handler;
	
	public Runnable watchdogCallback = () -> updateROI();

	public void disableLocationUpdates() {
		handler.removeCallbacks(watchdogCallback);
	}

	@Override
	public void onLocationChanged(Location location) {
		disableLocationUpdates();
		realLocation = location;
		timeOfLastLocation = System.currentTimeMillis();
		updateROI();
	}

	private void updateROI() {
		if (realLocation == null) {
			return;
		}
		Coordinate gcsCoord = new Coordinate(realLocation.getCoord().getLat(), realLocation.getCoord().getLon());

		double bearing = realLocation.getBearing();
		double distanceTraveledSinceLastPoint = realLocation.getSpeed() * (System.currentTimeMillis() - timeOfLastLocation) / 1000f;
		Coordinate goCoord = GeoTools.newCoordFromBearingAndDistance(gcsCoord, bearing,
				distanceTraveledSinceLastPoint);
		if (distanceTraveledSinceLastPoint > 0.0) {
			MavLinkROI.setROI(drone, new Coordinate(goCoord.getLat(), goCoord.getLon(), 1.0));
		}
		handler.postDelayed(watchdogCallback, TIMEOUT);

	}
}
