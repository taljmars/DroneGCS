package mavlink.core.connection.helper;

import json.JSONHelper;
import logger.Logger;
import mavlink.is.utils.coordinates.Coord2D;
import mavlink.is.utils.coordinates.Coord3D;
import mavlink.is.utils.units.Altitude;

import org.json.simple.JSONObject;

public class GCSLocationData {
	
	private final static String LOCATION_ADDRESS = "http://www.sparksapp.eu/public_scripts/QuadGetHomePosition.php";
	
	private Coord3D coordinate = null;
	private int fetchTime = 0;
	
	public GCSLocationData(Coord3D coordinate, int fetchTime) {
		this.coordinate = coordinate;
		this.fetchTime = fetchTime;
	}
	
	public Coord3D getCoordinate() {
		return coordinate;
	}
	
	public int getFetchTime() {
		return fetchTime;
	}
	
	public static GCSLocationData fetch() {
		long startTimestamp = System.currentTimeMillis();
		Logger.LogDesignedMessege("Sending request from '" + LOCATION_ADDRESS + "'");
		JSONObject obj = JSONHelper.makeHttpPostRequest(LOCATION_ADDRESS);
		if (obj == null) {
			return null;
		}		
		int fetchTime = (int) (System.currentTimeMillis() - startTimestamp);
		double lat = Double.parseDouble((String) obj.get("Lat"));
		double lon = Double.parseDouble((String) obj.get("Lng"));
		double alt = Double.parseDouble((String) obj.get("Z"));
		
		Coord2D coord2d = new Coord2D(lat, lon);
		Coord3D coord3d = new Coord3D(coord2d, new Altitude(alt));
		return new GCSLocationData(coord3d, fetchTime);
	}
}
