package com.gui.is.shapes;

import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;

import java.util.List;

public class PolylineTools {

	/**
	 * Total length of the polyline in meters
	 * 
	 * @param gridPoints
	 * @return
	 */
	public static double getPolylineLength(List<Coordinate> gridPoints) {
		double lenght = 0;
		for (int i = 1; i < gridPoints.size(); i++) {
			final Coordinate to = gridPoints.get(i - 1);
			if (to == null) {
				continue;
			}

			lenght += GeoTools.getDistance(gridPoints.get(i), to);
		}
		return lenght;
	}

}
