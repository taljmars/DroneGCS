package com.geo_tools;

import java.util.List;

import static java.lang.Math.*;

public class GeoTools {
	private static final double RADIUS_OF_EARTH = 6378137.0;// In meters.
															// Source: WGS84
	public List<Coordinate> waypoints;

	public GeoTools() {
	}

	/**
	 * Returns the distance between two points
	 * 
	 * @return distance between the points in degrees
	 */
	public static Double getAproximatedDistance(Coordinate p1, Coordinate p2) {
		return (Math.hypot((p1.getX() - p2.getX()), (p1.getY() - p2.getY())));
	}

	public static Double metersTolat(double meters) {
		return Math.toDegrees(meters / RADIUS_OF_EARTH);
	}

	public static Double latToMeters(double lat) {
		return Math.toRadians(lat) * RADIUS_OF_EARTH;
	}

	/**
	 * Extrapolate latitude/longitude given a heading and distance thanks to
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 * 
	 * @param origin
	 *            Point of origin
	 * @param bearing
	 *            bearing to navigate
	 * @param distance
	 *            distance to be added
	 * @return New point with the added distance
	 */
	public static Coordinate newCoordFromBearingAndDistance(Coordinate origin, double bearing,
			double distance) {

		double lat = origin.getLat();
		double lon = origin.getLon();
		double lat1 = Math.toRadians(lat);
		double lon1 = Math.toRadians(lon);
		double brng = Math.toRadians(bearing);
		double dr = distance / RADIUS_OF_EARTH;

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dr) + Math.cos(lat1) * Math.sin(dr)
				* Math.cos(brng));
		double lon2 = lon1
				+ Math.atan2(Math.sin(brng) * Math.sin(dr) * Math.cos(lat1),
						Math.cos(dr) - Math.sin(lat1) * Math.sin(lat2));

		return (new Coordinate(Math.toDegrees(lat2), Math.toDegrees(lon2)));
	}

	/**
	 * Calculates the arc between two points
	 * http://en.wikipedia.org/wiki/Haversine_formula
	 * 
	 * @return the arc in degrees
	 */
	static double getArcInRadians(Coordinate from, Coordinate to) {

		double latitudeArc = Math.toRadians(from.getLat() - to.getLat());
		double longitudeArc = Math.toRadians(from.getLon() - to.getLon());

		double latitudeH = Math.sin(latitudeArc * 0.5);
		latitudeH *= latitudeH;
		double lontitudeH = Math.sin(longitudeArc * 0.5);
		lontitudeH *= lontitudeH;

		double tmp = Math.cos(Math.toRadians(from.getLat()))
				* Math.cos(Math.toRadians(to.getLat()));
		return Math.toDegrees(2.0 * Math.asin(Math.sqrt(latitudeH + tmp * lontitudeH)));
	}

	/**
	 * Computes the distance between two coordinates
	 * 
	 * @return distance in meters
	 */
	public static double getDistance(Coordinate from, Coordinate to) {
		return RADIUS_OF_EARTH * Math.toRadians(getArcInRadians(from, to));
	}

	/**
	 * Computes the distance between two coordinates taking in account the
	 * height difference
	 * 
	 * @return distance in meters
	 */
	public static double get3DDistance(Coordinate end, Coordinate start) {
		double horizontalDistance = getDistance(end, start);
		double altitudeDiff = Math.abs((end.getAltitude() - start.getAltitude()));
		return MathUtil.hypot(horizontalDistance, altitudeDiff);
	}

	/**
	 * Computes the heading between two coordinates
	 * 
	 * @return heading in degrees
	 */
	public static double getHeadingFromCoordinates(Coordinate fromLoc, Coordinate toLoc) {
		double fLat = Math.toRadians(fromLoc.getLat());
		double fLng = Math.toRadians(fromLoc.getLon());
		double tLat = Math.toRadians(toLoc.getLat());
		double tLng = Math.toRadians(toLoc.getLon());

		double degree = Math.toDegrees(Math.atan2(
				Math.sin(tLng - fLng) * Math.cos(tLat),
				Math.cos(fLat) * Math.sin(tLat) - Math.sin(fLat) * Math.cos(tLat)
						* Math.cos(tLng - fLng)));

		if (degree >= 0) {
			return degree;
		} else {
			return 360 + degree;
		}
	}

	/**
	 * Copied from android-map-utils (licensed under Apache v2)
	 * com.google.maps.android.SphericalUtil.java
	 * 
	 * @return area in m�
	 */
	public static double getArea(PolygonInt poly) {
		List<Coordinate> path = poly.getPoints();
		int size = path.size();
		if (size < 3) {
			return 0;
		}
		double total = 0;
		Coordinate prev = path.get(size - 1);
		double prevTanLat = tan((PI / 2 - toRadians(prev.getLat())) / 2);
		double prevLng = toRadians(prev.getLon());
		// For each edge, accumulate the signed area of the triangle formed by
		// the North Pole
		// and that edge ("polar triangle").
		for (Coordinate point : path) {
			double tanLat = tan((PI / 2 - toRadians(point.getLat())) / 2);
			double lng = toRadians(point.getLon());
			total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
			prevTanLat = tanLat;
			prevLng = lng;
		}
		return abs(total * (RADIUS_OF_EARTH * RADIUS_OF_EARTH));
	}

	/**
	 * Copied from android-map-utils (licensed under Apache v2)
	 * com.google.maps.android.SphericalUtil.java
	 * 
	 * Returns the signed area of a triangle which has North Pole as a vertex.
	 * Formula derived from
	 * "Area of a spherical triangle given two edges and the included angle" as
	 * per "Spherical Trigonometry" by Todhunter, page 71, section 103, point 2.
	 * See http://books.google.com/books?id=3uBHAAAAIAAJ&pg=PA71 The arguments
	 * named "tan" are tan((pi/2 - latitude)/2).
	 */
	private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
		double deltaLng = lng1 - lng2;
		double t = tan1 * tan2;
		return 2 * atan2(t * sin(deltaLng), 1 + t * cos(deltaLng));
	}

	public static Coordinate pointAlongTheLine(Coordinate start, Coordinate end, int distance) {
		return newCoordFromBearingAndDistance(start, getHeadingFromCoordinates(start, end), distance);
	}
}
