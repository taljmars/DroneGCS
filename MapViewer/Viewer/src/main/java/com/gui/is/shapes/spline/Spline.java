package com.gui.is.shapes.spline;

import com.geo_tools.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class Spline {

	private static final double SPLINE_TENSION = 1.6;

	private Coordinate p0;
	private Coordinate p0_prime;
	private Coordinate a;
	private Coordinate b;

	public Spline(Coordinate pMinus1, Coordinate p0, Coordinate p1, Coordinate p2) {
		this.p0 = p0;

		// derivative at a point is based on difference of previous and next
		// points
		p0_prime = p1.subtract(pMinus1).dot(1 / SPLINE_TENSION);
		Coordinate p1_prime = p2.subtract(this.p0).dot(1 / SPLINE_TENSION);

		// compute a and b coords used in spline formula
		a = Coordinate.sum(this.p0.dot(2), p1.dot(-2), p0_prime, p1_prime);
		b = Coordinate.sum(this.p0.dot(-3), p1.dot(3), p0_prime.dot(-2), p1_prime.negate());
	}

	public List<Coordinate> generateCoordinates(int decimation) {
		ArrayList<Coordinate> result = new ArrayList<Coordinate>();
		float step = 1f / decimation;
		for (float i = 0; i < 1; i += step) {
			result.add(evaluate(i));
		}

		return result;
	}

	private Coordinate evaluate(double t) {
		double tSquared = t * t;
		double tCubed = tSquared * t;

		return Coordinate.sum(a.dot(tCubed), b.dot(tSquared), p0_prime.dot(t), p0);
	}

}
