package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.dronedb.persistence.scheme.*;
import com.gui.core.mapTreeObjects.LayerSingle;
import com.gui.core.mapViewer.LayeredViewMap;
import com.dronegcs.mavlink.is.drone.variables.Compound;
import org.springframework.context.ApplicationContext;

public abstract class LayerPerimeter extends LayerSingle implements Compound {

	protected ApplicationContext applicationContext;
	protected Perimeter perimeter;

	public LayerPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
	}
	
	public LayerPerimeter(LayerPerimeter layerPerimeter, LayeredViewMap viewMap) {
		super(layerPerimeter, viewMap);
	}

	public void setPerimeter(Perimeter perimeter) {this.perimeter = perimeter;}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public Perimeter getPerimeter() {
		return perimeter;
	}

    public static LayerPerimeter generateNew(BaseObject perimeter, LayeredViewMap layeredViewMap) {
		if (perimeter instanceof PolygonPerimeter)
			return new LayerPolygonPerimeter((PolygonPerimeter) perimeter, layeredViewMap);

		if (perimeter instanceof CirclePerimeter)
			return new LayerCircledPerimeter((CirclePerimeter) perimeter, layeredViewMap);

		return null;
    }
}
