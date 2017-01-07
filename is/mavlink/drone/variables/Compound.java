package mavlink.drone.variables;

import gui.is.Coordinate;

public interface Compound {
	
	public boolean isContained(Coordinate position);
	
	public Coordinate getClosestPointOnEdge(Coordinate coord);
	
}
