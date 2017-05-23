package com.gui.is.shapes;

import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;

import java.util.ArrayList;
import java.util.List;

public class LineSampler {

	private List<Coordinate> points;
	private List<Coordinate> sampledPoints = new ArrayList<Coordinate>();

	public LineSampler(List<Coordinate> points) {
		this.points = points;
	}

	public LineSampler(Coordinate p1, Coordinate p2) {
		points = new ArrayList<Coordinate>();
		points.add(p1);
		points.add(p2);
	}

	public List<Coordinate> sample(double sampleDistance) {
		for (int i = 1; i < points.size(); i++) {
			Coordinate from = points.get(i - 1);
			if (from == null) {
				continue;
			}

			Coordinate to = points.get(i);
			sampledPoints.addAll(sampleLine(from, to, sampleDistance));
		}

		final Coordinate lastPoint = getLast(points);
		if (lastPoint != null) {
			sampledPoints.add(lastPoint);
		}
		return sampledPoints;
	}

	private List<Coordinate> sampleLine(Coordinate from, Coordinate to, double samplingDistance) {
		List<Coordinate> result = new ArrayList<Coordinate>();
		double heading = GeoTools.getHeadingFromCoordinates(from, to);
		double totalLength = GeoTools.getDistance(from, to);
		double distance = 0;

		while (distance < totalLength) {
			result.add(GeoTools.newCoordFromBearingAndDistance(from, heading, distance));
			distance += samplingDistance;
		}
		return result;
	}

	private Coordinate getLast(List<Coordinate> list) {
		return list.get(list.size() - 1);
	}

}
