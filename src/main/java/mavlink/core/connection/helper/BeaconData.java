package mavlink.core.connection.helper;

import org.json.simple.JSONObject;

import is.logger.Logger;
import is.objects.json.JSONHelper;
import is.springConfig.AppConfig;
import geoTools.Coordinate;

public class BeaconData {
	
	private final static String LOCATION_ADDRESS = "http://www.sparksapp.eu/public_scripts/QuadGetFollowPosition.php";
	
	private Coordinate coordinate = null;
	private int fetchTime = 0;
	
	public BeaconData(Coordinate coordinate, int fetchTime) {
		this.coordinate = coordinate;
		this.fetchTime = fetchTime;
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
	}
	
	public int getFetchTime() {
		return fetchTime;
	}
	
	public static BeaconData fetch() {
		long startTimestamp = System.currentTimeMillis();
		Logger logger = (Logger) AppConfig.context.getBean(Logger.class);
		logger.LogDesignedMessege("Sending request from '" + LOCATION_ADDRESS + "'");
		JSONObject obj = JSONHelper.makeHttpPostRequest(LOCATION_ADDRESS);
		if (obj == null) {
			return null;
		}		
		int fetchTime = (int) (System.currentTimeMillis() - startTimestamp);
		double lat = Double.parseDouble((String) obj.get("Lat"));
		double lon = Double.parseDouble((String) obj.get("Lng"));
		double alt = Double.parseDouble((String) obj.get("Z"));
		
		Coordinate coord3d = new Coordinate(lat, lon, alt);
		return new BeaconData(coord3d, fetchTime);
	}
}
