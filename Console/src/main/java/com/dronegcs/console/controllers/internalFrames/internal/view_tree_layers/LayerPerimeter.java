package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.gui.core.mapTreeObjects.LayerSingle;
import com.gui.core.mapViewer.LayeredViewMap;
import com.dronegcs.mavlink.is.drone.variables.Compound;
import com.geo_tools.Coordinate;

public abstract class LayerPerimeter extends LayerSingle implements Compound {
	
	public LayerPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerPerimeter(LayerPerimeter layerPerimeter, LayeredViewMap viewMap) {
		super(layerPerimeter, viewMap);
	}

	public abstract void add(Coordinate position);
}
