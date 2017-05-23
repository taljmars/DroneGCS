package com.gui.is.shapes;

import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;
import com.geo_tools.PolygonInt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon implements PolygonInt {

	private List<Coordinate> points = new ArrayList<Coordinate>();

	public Polygon(Polygon polygon) {
		for (Coordinate coord2d : polygon.points) points.add(new Coordinate(coord2d));
	}

	public Polygon() {
	}

	public void addPoints(List<Coordinate> pointList) {
		for (Coordinate point : pointList) {
			addPoint(point);
		}
	}

	public void addPoint(Coordinate coord) {
		points.add(coord);
	}

	public void clearPolygon() {
		points.clear();
	}

	public List<Coordinate> getPoints() {
		return points;
	}

	public List<LineCoordinates> getLines() {
		List<LineCoordinates> list = new ArrayList<LineCoordinates>();
		for (int i = 0; i < points.size(); i++) {
			int endIndex = (i == 0) ? points.size() - 1 : i - 1;
			list.add(new LineCoordinates(points.get(i), points.get(endIndex)));
		}
		return list;
	}

	public void movePoint(Coordinate coord, int number) {
		points.get(number).set(coord.getLat(), coord.getLon());
	}

	public double getArea() {
		return GeoTools.getArea(this);
	}

	/*
	 * @Override public List<LatLng> getPathPoints() { List<LatLng> path =
	 * getLatLngList(); if (getLatLngList().size() > 2) { path.add(path.get(0));
	 * } return path; }
	 */

	public void checkIfValid() throws Exception {
		if (points.size() < 3) {
			throw new InvalidPolygon(points.size());
		} else {
		}
	}

	public class InvalidPolygon extends Exception {
		private static final long serialVersionUID = 1L;
		public int size;

		public InvalidPolygon(int size) {
			this.size = size;
		}
	}

	public void reversePoints() {
		Collections.reverse(points);
	}

}
