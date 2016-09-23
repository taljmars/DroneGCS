package mavlink.core.location;

import gui.is.services.LoggerDisplayerManager;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import logger.Logger;
import mavlink.core.connection.helper.BeaconData;
import mavlink.is.location.Location;
import mavlink.is.location.LocationFinder;
import mavlink.is.location.LocationReceiver;
import mavlink.is.utils.coordinates.Coord2D;

public class MyLocationImpl implements LocationFinder {
	private static final int UPDATE_INTERVAL = 1000;// TALMA was 500;
	private static final double SPEED = 3;
	
	private Set<LocationReceiver> receivers = null;
	private TimerTask scTimerTask = null;
	
	public MyLocationImpl() {
		receivers = new HashSet<LocationReceiver>();
	}

	@Override
	public void addLocationListener(LocationReceiver receiver) {
		this.receivers.add(receiver);
	}
	
	@Override
	public void removeLocationListener(LocationReceiver receiver) {
		this.receivers.remove(receiver);
	}

	@Override
	public void enableLocationUpdates() {
		Timer timer = new Timer();
		Logger.LogDesignedMessege(getClass() + " Location updates started!");
		scTimerTask = new TimerTask() {
			@Override
			public void run() {
				
				BeaconData beaconDate = BeaconData.fetch();
				if (beaconDate == null) {
					LoggerDisplayerManager.addErrorMessegeToDisplay("Failed to get beacon point from the web");
					return;
				}
				Logger.LogDesignedMessege("Request took " + beaconDate.getFetchTime() + "ms");				
				Coord2D coord = beaconDate.getCoordinate().dot(1);
				
				for (LocationReceiver lr : receivers)
					lr.onLocationChanged(new Location(coord, 0, (float) SPEED, true));
			}
		};
		timer.scheduleAtFixedRate(scTimerTask, 0, UPDATE_INTERVAL);

	}

	@Override
	public void disableLocationUpdates() {
		if (scTimerTask != null)
			scTimerTask.cancel();
		
		scTimerTask = null;
		
		Logger.LogDesignedMessege(getClass() + " Location updates canceled!");
	}	
}