package mavlink.core.connection;

import gui.core.dashboard.Dashboard;

import java.util.Timer;
import java.util.TimerTask;

import json.JSONHelper;

import org.json.simple.JSONObject;

import logger.Logger;
import mavlink.core.gcs.location.Location;
import mavlink.core.gcs.location.Location.LocationFinder;
import mavlink.core.gcs.location.Location.LocationReceiver;
import mavlink.is.utils.coordinates.Coord2D;

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
			long lastTime = System.currentTimeMillis();

			@Override
			public void run() {
				
				lastTime = System.currentTimeMillis();
				JSONObject obj = JSONHelper.makeHttpPostRequest("http://www.sparksapp.eu/public_scripts/QuadGetFollowPosition.php");
				if (obj == null) {
					Dashboard.loggerDisplayerManager.addErrorMessegeToDisplay("Failed to get beacon point from the web");
					return;
				}
				Logger.LogDesignedMessege("Request took " + (lastTime - System.currentTimeMillis()) + "ms");
				double lat = Double.parseDouble((String) obj.get("Lat"));
				double lon = Double.parseDouble((String) obj.get("Lng"));
				//double alt = Double.parseDouble((String) obj.get("Z"));
				
				Coord2D coord = new Coord2D(lat, lon);
				
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