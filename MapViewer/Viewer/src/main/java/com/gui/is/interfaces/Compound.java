package com.gui.is.interfaces;

import com.geo_tools.Coordinate;

public interface Compound {
	
	public boolean isContained(Coordinate position);
	
	public Coordinate getClosestPointOnEdge(Coordinate coord);
	
}
