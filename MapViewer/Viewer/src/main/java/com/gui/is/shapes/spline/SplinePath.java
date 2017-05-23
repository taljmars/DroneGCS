package com.gui.is.shapes.spline;

import com.geo_tools.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains functions used to generate a spline path.
 */
public class SplinePath {

	/**
	 * Used as tag for logging.
	 */
	@SuppressWarnings("unused")
	private static final String TAG = SplinePath.class.getSimpleName();

	private final static int SPLINE_DECIMATION = 20;

	/**
	 * Process the given map coordinates, and return a set of coordinates
	 * describing the spline path.
	 * 
	 * @param points
	 *            map coordinates decimation factor
	 * @return set of coordinates describing the spline path
	 */
	public static List<Coordinate> process(List<Coordinate> points) {
		final int pointsCount = points.size();
		if (pointsCount < 4) {
			System.err.println("Not enough points!");
			return points;
		}

		final List<Coordinate> results = processPath(points);
		results.add(0, points.get(0));
		results.add(points.get(pointsCount - 1));
		return results;
	}

	private static List<Coordinate> processPath(List<Coordinate> points) {
		final List<Coordinate> results = new ArrayList<Coordinate>();
		for (int i = 3; i < points.size(); i++) {
			results.addAll(processPathSegment(points.get(i - 3), points.get(i - 2),
					points.get(i - 1), points.get(i)));
		}
		return results;
	}

	private static List<Coordinate> processPathSegment(Coordinate l1, Coordinate l2, Coordinate l3, Coordinate l4) {
		Spline spline = new Spline(l1, l2, l3, l4);
		return spline.generateCoordinates(SPLINE_DECIMATION);
	}

}
