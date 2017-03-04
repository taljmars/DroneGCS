package main.java.gui_controllers.controllers.internalFrames.internal.view_tree_layers;

import gui.core.mapTreeObjects.LayerSingle;
import gui.core.mapViewer.LayeredViewMap;
import main.java.is.mavlink.drone.variables.Compound;
import tools.geoTools.Coordinate;

public abstract class LayerPerimeter extends LayerSingle implements Compound {
	
	public LayerPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerPerimeter(LayerPerimeter layerPerimeter, LayeredViewMap viewMap) {
		super(layerPerimeter, viewMap);
	}

	public abstract void add(Coordinate position);
}
