package controllers.internalFrames.internal.view_tree_layers;

import gui.core.mapTreeObjects.LayerSingle;
import gui.core.mapViewer.LayeredViewMap;
import gui.is.Coordinate;
import mavlink.drone.variables.Compound;

public abstract class LayerPerimeter extends LayerSingle implements Compound {
	
	public LayerPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerPerimeter(LayerPerimeter layerPerimeter, LayeredViewMap viewMap) {
		super(layerPerimeter, viewMap);
	}

	public abstract void add(Coordinate position);
}
