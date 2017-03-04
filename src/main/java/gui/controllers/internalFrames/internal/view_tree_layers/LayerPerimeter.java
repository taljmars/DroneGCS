package gui.controllers.internalFrames.internal.view_tree_layers;

import gui.core.mapTreeObjects.LayerSingle;
import gui.core.mapViewer.LayeredViewMap;
import is.mavlink.drone.variables.Compound;
import geoTools.Coordinate;

public abstract class LayerPerimeter extends LayerSingle implements Compound {
	
	public LayerPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerPerimeter(LayerPerimeter layerPerimeter, LayeredViewMap viewMap) {
		super(layerPerimeter, viewMap);
	}

	public abstract void add(Coordinate position);
}
