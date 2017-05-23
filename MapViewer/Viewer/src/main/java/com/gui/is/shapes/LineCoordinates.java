package com.gui.is.shapes;

import com.geo_tools.Coordinate;
import com.geo_tools.GeoTools;

public class LineCoordinates {
	private final Coordinate start;
	private final Coordinate end;

	public LineCoordinates(Coordinate start, Coordinate end) {
		this.start = start;
		this.end = end;
	}

	public LineCoordinates(LineCoordinates line) {
		this(line.start, line.end);
	}

	public Coordinate getStart() {
		return start;
	}

	public Coordinate getEnd() {
		return end;
	}

	public double getHeading() {
		return GeoTools.getHeadingFromCoordinates(this.start, this.end);
	}

	public Coordinate getFarthestEndpointTo(Coordinate point) {
		if (getClosestEndpointTo(point).equals(start)) {
			return end;
		} else {
			return start;
		}
	}

	public Coordinate getClosestEndpointTo(Coordinate point) {
		if (getDistanceToStart(point) < getDistanceToEnd(point)) {
			return start;
		} else {
			return end;
		}
	}

	private Double getDistanceToEnd(Coordinate point) {
		return GeoTools.getAproximatedDistance(end, point);
	}

	private Double getDistanceToStart(Coordinate point) {
		return GeoTools.getAproximatedDistance(start, point);
	}

	@Override
	public String toString() {
		return "from:" + start.toString() + "to:" + end.toString();
	}

}