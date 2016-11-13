package gui.core.mapTreeObjects;

import gui.is.Coordinate;
import mavlink.is.drone.variables.Compound;

public abstract class LayerPerimeter extends LayerSingle implements Compound {
	
	public LayerPerimeter(String name) {
		super(name);
	}
	
	public LayerPerimeter(LayerPerimeter layerPerimeter) {
		super(layerPerimeter);
	}

	public abstract void add(Coordinate position);
}
