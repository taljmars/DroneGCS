package mavlink.core.connection.helper;

import org.json.simple.JSONObject;

import is.logger.Logger;
import is.objects.json.JSONHelper;
import is.springConfig.AppConfig;
import geoTools.Coordinate;

public class GCSLocationData {
	
	private final static String LOCATION_ADDRESS = "http://www.sparksapp.eu/public_scripts/QuadGetHomePosition.php";
	
	private Coordinate coordinate = null;
	private int fetchTime = 0;
	
	public GCSLocationData(Coordinate coordinate, int fetchTime) {
		this.coordinate = coordinate;
		this.fetchTime = fetchTime;
	}
	
	public Coordinate getCoordinate() {
		return coordinate;
	}
	
	public int getFetchTime() {
		return fetchTime;
	}
	
	public static GCSLocationData fetch() {
		long startTimestamp = System.currentTimeMillis();
		Logger logger = AppConfig.context.getBean(Logger.class);
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
		return new GCSLocationData(coord3d, fetchTime);
	}
}
