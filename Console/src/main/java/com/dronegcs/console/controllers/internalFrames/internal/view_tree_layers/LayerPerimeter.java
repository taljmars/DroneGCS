package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.db.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.gui.core.mapViewer.LayeredViewMap;
import com.dronegcs.mavlink.is.drone.variables.Compound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public abstract class LayerPerimeter<T extends Perimeter> extends EditedLayer implements Compound {

	private final static Logger LOGGER = LoggerFactory.getLogger(LayerPerimeter.class);

	protected ApplicationContext applicationContext;
	protected T perimeter;

	public LayerPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
		startEditing();
	}
	
	public LayerPerimeter(LayerPerimeter<T> layerPerimeter, LayeredViewMap viewMap) throws PerimeterUpdateException {
		super(layerPerimeter, viewMap);
		LOGGER.debug("Before copy " + layerPerimeter.getPerimeter());
		PerimetersManager perimetersManager = applicationContext.getBean(PerimetersManager.class);
		perimeter = perimetersManager.clonePerimeter(layerPerimeter.getPerimeter());
		startEditing();
		LOGGER.debug("After copy " + perimeter);
	}

	public LayerPerimeter(T perimeter1, LayeredViewMap layeredViewMap) {
		this(perimeter1.getName(), layeredViewMap);
		this.perimeter = perimeter1;
	}

	public LayerPerimeter(T perimeter1, LayeredViewMap layeredViewMap, boolean isEditing) {
		super(perimeter1.getName(), layeredViewMap);
		this.perimeter = perimeter1;
		if (isEditing)
			startEditing();
		else
			stopEditing();
	}

	public void setPerimeter(T perimeter) {this.perimeter = perimeter;}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public T getPerimeter() {
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
