package com.geo_tools;

import java.util.List;

/**
 * Calculate a rectangle that bounds all inserted points
 */
public class CoordBounds {
	public Coordinate sw_3quadrant;
	public Coordinate ne_1quadrant;

	public CoordBounds(Coordinate point) {
		include(point);
	}

	public CoordBounds(List<Coordinate> points) {
		for (Coordinate point : points) {
			include(point);
		}
	}

	public void include(Coordinate point) {
		if ((sw_3quadrant == null) | (ne_1quadrant == null)) {
			ne_1quadrant = new Coordinate(point);
			sw_3quadrant = new Coordinate(point);
		} else {
			if (point.getY() > ne_1quadrant.getY()) {
				ne_1quadrant.set(ne_1quadrant.getX(), point.getY());
			}
			if (point.getX() > ne_1quadrant.getX()) {
				ne_1quadrant.set(point.getX(), ne_1quadrant.getY());
			}
			if (point.getY() < sw_3quadrant.getY()) {
				sw_3quadrant.set(sw_3quadrant.getX(), point.getY());
			}
			if (point.getX() < sw_3quadrant.getX()) {
				sw_3quadrant.set(point.getX(), sw_3quadrant.getY());
			}
		}
	}

	public double getDiag() {
		return GeoTools.latToMeters(GeoTools.getAproximatedDistance(ne_1quadrant, sw_3quadrant));
	}

	public Coordinate getMiddle() {
		return (new Coordinate((ne_1quadrant.getLat() + sw_3quadrant.getLat()) / 2,
				(ne_1quadrant.getLon() + sw_3quadrant.getLon()) / 2));

	}
}
