package com.dronegcs.mavlink.core.mavlink.connection.helper;

import geoTools.Coordinate;
import com.dronegcs.gcsis.json.JSONHelper;
import com.dronegcs.gcsis.logger.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Created by oem on 3/9/17.
 */
@Component
public class BeaconDataFactory {

    private final static String LOCATION_ADDRESS = "http://www.sparksapp.eu/public_scripts/QuadGetFollowPosition.php";

    @Autowired @NotNull(message = "Internal Error: Failed to get com.dronegcs.gcsis.logger")
    public Logger logger;

    public BeaconData fetch() {
        long startTimestamp = System.currentTimeMillis();
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
