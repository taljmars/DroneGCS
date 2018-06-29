package com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers;

import com.db.gui.persistence.scheme.Layer;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.gui.core.layers.AbstractLayer;
import com.gui.core.mapViewer.LayeredViewMap;
import com.dronegcs.mavlink.is.drone.variables.Compound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public abstract class LayerPerimeter<T extends Perimeter> extends EditedLayerImpl implements Compound, EditedLayer {

	private final static Logger LOGGER = LoggerFactory.getLogger(LayerPerimeter.class);

	protected T perimeter;

	public LayerPerimeter(String name, LayeredViewMap viewMap) {
		super(name, viewMap);
		startEditing();
	}

	public LayerPerimeter(T perimeter1, LayeredViewMap layeredViewMap) {
		this(perimeter1.getName(), layeredViewMap);
		this.perimeter = perimeter1;
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

	@Override
	public void setPayload(Object payload) {
		super.setPayload(payload);
		try {
			ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper = applicationContext.getBean(ObjectCrudSvcRemoteWrapper.class);
			this.perimeter = objectCrudSvcRemoteWrapper.read(((Layer)payload).getObjectsUids().get(0));
		}
		catch (ObjectNotFoundRemoteException e) {
			e.printStackTrace();
		}
	}

    public static LayerPerimeter generateNew(BaseObject perimeter, LayeredViewMap layeredViewMap) {
		if (perimeter instanceof PolygonPerimeter)
			return new LayerPolygonPerimeter((PolygonPerimeter) perimeter, layeredViewMap);

		if (perimeter instanceof CirclePerimeter)
			return new LayerCircledPerimeter((CirclePerimeter) perimeter, layeredViewMap);

		return null;
    }

	@Override
	public String getCaption() {
		return "Perimeter";
	}
}
