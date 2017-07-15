package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.gui.core.mapTreeObjects.LayerSingle;
import com.gui.core.mapViewer.LayeredViewMap;
import com.dronegcs.mavlink.is.drone.variables.Compound;
import org.springframework.context.ApplicationContext;

public abstract class LayerPerimeter extends EditedLayer implements Compound {

	protected ApplicationContext applicationContext;
	protected Perimeter perimeter;

	public LayerPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
		startEditing();
	}
	
	public LayerPerimeter(LayerPerimeter layerPerimeter, LayeredViewMap viewMap) throws PerimeterUpdateException {
		super(layerPerimeter, viewMap);
		System.out.println("Before copy " + layerPerimeter.getPerimeter());
		PerimetersManager perimetersManager = applicationContext.getBean(PerimetersManager.class);
		perimeter = perimetersManager.clonePerimeter(layerPerimeter.getPerimeter());
		startEditing();
		System.out.println("After copy " + perimeter);
	}

	public LayerPerimeter(Perimeter perimeter1, LayeredViewMap layeredViewMap) {
		this(perimeter1.getName(), layeredViewMap);
		this.perimeter = perimeter1;
	}

	public LayerPerimeter(Perimeter perimeter1, LayeredViewMap layeredViewMap, boolean isEditing) {
		super(perimeter1.getName(), layeredViewMap);
		this.perimeter = perimeter1;
		if (isEditing)
			startEditing();
		else
			stopEditing();
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
