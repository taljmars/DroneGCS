package mavlink.core.connection;

import gui.core.dashboard.Dashboard;

import java.util.Timer;
import java.util.TimerTask;

import json.JSONHelper;

import org.json.simple.JSONObject;

import logger.Logger;
import mavlink.core.connection.helper.BeaconData;
import mavlink.core.gcs.location.Location;
import mavlink.core.gcs.location.Location.LocationFinder;
import mavlink.core.gcs.location.Location.LocationReceiver;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.coordinates.Coord3D;

public class MyLocationFinder implements LocationFinder {
	private static final int UPDATE_INTERVAL = 1000;// TALMA was 500;
	private static final double SPEED = 3;
	private LocationReceiver receiver;
	
	TimerTask myTimerTask = null;

	@Override
	public void setLocationListener(LocationReceiver receiver) {
		this.receiver = receiver;
	}

	@Override
	public void enableLocationUpdates() {
		Timer timer = new Timer();
		Logger.LogDesignedMessege(getClass() + " Location updates started!");
		myTimerTask = new TimerTask() {
			@Override
			public void run() {
				
				BeaconData beaconDate = BeaconData.fetch();
				if (beaconDate == null) {
					Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Failed to get beacon point from the web");
					return;
				}
				Logger.LogDesignedMessege("Request took " + beaconDate.getFetchTime() + "ms");				
				Coord2D coord = beaconDate.getCoordinate().dot(1);
				
				receiver.onLocationChanged(new Location(coord, 0, (float) SPEED, true));
			}
		};
		timer.scheduleAtFixedRate(myTimerTask, 0, UPDATE_INTERVAL);

	}

	@Override
	public void disableLocationUpdates() {
		if (myTimerTask != null)
			myTimerTask.cancel();
		
		Logger.LogDesignedMessege(getClass() + " Location updates canceled!");
	}	
}