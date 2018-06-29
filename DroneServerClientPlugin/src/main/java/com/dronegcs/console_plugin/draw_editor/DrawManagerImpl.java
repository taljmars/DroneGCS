package com.dronegcs.console_plugin.draw_editor;

import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.Shape;
import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.QueryRequestRemote;
import com.db.persistence.scheme.QueryResponseRemote;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.LayersCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.QuerySvcRemoteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Component
public class DrawManagerImpl implements DrawManager {

	private final static Logger LOGGER = LoggerFactory.getLogger(DrawManagerImpl.class);

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	private ApplicationContext applicationContext;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
	private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get query")
	private QuerySvcRemoteWrapper querySvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get layers object crud")
	private LayersCrudSvcRemoteWrapper layerCrudSvcRemote;

	private List<ClosableDrawEditor> closableDrawLayerEditorList;

	public DrawManagerImpl() {
		closableDrawLayerEditorList = new ArrayList<>();
	}

	@Override
	public DrawEditor openDrawLayerEditor(String initialName) throws DrawUpdateException {
		LOGGER.debug("Setting new layer to drawLayer editor");
		if (initialName == null || initialName.isEmpty()) {
			throw new RuntimeException("Layer name cannot be empty");
		}
		ClosableDrawEditor drawLayerEditor = applicationContext.getBean(ClosableDrawEditor.class);
		drawLayerEditor.open(initialName);
		closableDrawLayerEditorList.add(drawLayerEditor);
		return drawLayerEditor;
	}

	@Override
	public DrawEditor openDrawLayerEditor(Layer layer) throws DrawUpdateException {
		LOGGER.debug("Setting new layer to drawLayer editor");
		ClosableDrawEditor drawLayerEditor = findDrawLayerEditorByLayer(layer);
		if (drawLayerEditor == null) {
			LOGGER.debug("Editor not exist for layer " + layer.getName() + ", creating new one");
			drawLayerEditor = applicationContext.getBean(ClosableDrawEditor.class);
			drawLayerEditor.open(layer);
			closableDrawLayerEditorList.add(drawLayerEditor);
		}
		else {
			LOGGER.debug("Found existing drawLayer editor");
		}
		return drawLayerEditor;
	}

	@Override
	public void delete(Layer layer) {
		if (layer == null) {
			LOGGER.error("Received Empty layer, skip deletion");
			return;
		}

		try {
			ClosableDrawEditor closableDrawLayerEditor = (ClosableDrawEditor) openDrawLayerEditor(layer);
			closableDrawLayerEditor.delete();
		}
		catch (DrawUpdateException e) {
			LOGGER.error("Failed to delete draw layer", e);
		}
	}

	@Override
	public Layer update(Layer layer) throws DrawUpdateException {
		try {
			ClosableDrawEditor closableDrawLayerEditor = findDrawLayerEditorByLayer(layer);
			if (closableDrawLayerEditor == null)
				return (Layer) objectCrudSvcRemote.update(layer);

			return closableDrawLayerEditor.update(layer);
		}
		catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException  e) {
			throw new DrawUpdateException(e.getMessage());
		}
	}

	@Override
	public Layer cloneDrawLayer(Layer layer) throws DrawUpdateException {
		try {
			return layerCrudSvcRemote.cloneLayer(layer);
		}
		catch (ObjectNotFoundRemoteException | DatabaseValidationRemoteException | ObjectInstanceRemoteException  e) {
			throw new DrawUpdateException(e.getMessage());
		}
	}

	@Override
	public <T extends DrawEditor> ClosingPair closeDrawLayerEditor(T drawLayerEditor, boolean shouldSave) {
		LOGGER.debug("closing draw layer editor");
		if (!(drawLayerEditor instanceof ClosableDrawEditor)) {
			return null;
		}
		ClosingPair<Layer> drawLayerClosingPair = ((ClosableDrawEditor) drawLayerEditor).close(shouldSave);
		closableDrawLayerEditorList.remove(drawLayerEditor);
		return drawLayerClosingPair;
	}

	@Override
	public Collection<ClosingPair<Layer>> closeAllDrawLayersEditors(boolean shouldSave) {
		Collection<ClosingPair<Layer>> closedDrawLayer = new ArrayList<>();
		Iterator<ClosableDrawEditor> it = closableDrawLayerEditorList.iterator();
		while (it.hasNext()) {
			closedDrawLayer.add(it.next().close(shouldSave));
		}
		closableDrawLayerEditorList.clear();
		return closedDrawLayer;
	}

	@Override
	public List<BaseObject> getLayerItems(Layer layer) {
		List<String> itemUids = layer.getObjectsUids();
		List<BaseObject> objects = new ArrayList<>();
		for (String uid : itemUids) {
			try {
				objects.add(objectCrudSvcRemote.read(uid));
			}
			catch (ObjectNotFoundRemoteException e) {
				LOGGER.error("Failed to get layer item", e);
			}
		}
		return objects;
	}

	@Override
	public int loadEditors() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(com.db.gui.persistence.scheme.Layer.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllModifiedLayers");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> modifiedLayersGroupList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} modified layers in total", modifiedLayersGroupList.size());
		modifiedLayersGroupList.forEach(element -> {
			try {
				Layer layer = (Layer) element;
				if (layer.getObjectsUids().isEmpty()) {
					this.openDrawLayerEditor(layer);
					return;
				}

				String objid = layer.getObjectsUids().get(0);
				BaseObject firstObjectOfLayer = objectCrudSvcRemote.read(objid);
				if (firstObjectOfLayer instanceof Shape) {
					this.openDrawLayerEditor(layer);
					return;
				}

				return;
			}
			catch (DrawUpdateException e) {
				e.printStackTrace();
			}
			catch (ObjectNotFoundRemoteException e) {
				e.printStackTrace();
			}
		});

		return modifiedLayersGroupList.size();
	}

	@Override
	public <T extends DrawEditor> T getDrawLayerEditor(Layer layer) {
		if (layer == null) {
			LOGGER.error("Layer is null");
			throw new RuntimeException("Layer is null");
		}
		return (T) findDrawLayerEditorByLayer(layer);
	}

	@Override
	public List<Shape> getDrawLayerItems(Layer layer) {
		Layer leadDrawLayer = layer;
		ClosableDrawEditor closableDrawLayerEditor = findDrawLayerEditorByLayer(layer);
		if (closableDrawLayerEditor != null)
			leadDrawLayer = closableDrawLayerEditor.getModifiedLayer();

		List<Shape> itemList = new ArrayList<>();
		List<String> uuidList = leadDrawLayer.getObjectsUids();
		for (String uuid : uuidList) {
			try {
				itemList.add(objectCrudSvcRemote.readByClass(uuid, Shape.class.getCanonicalName()));
			}
			catch (ObjectNotFoundRemoteException e) {
				LOGGER.error("Failed to get layer item", e);
			}
		}

		return itemList;
	}

	@Override
	public List<BaseObject> getAllDrawLayers() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Layer.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllLayers");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> layersList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} layers in total", layersList.size());
		return layersList;
	}

	@Override
	public List<BaseObject> getAllModifiedDrawLayers() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Layer.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllModifiedLayers");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> layersList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} modified layers in total", layersList.size());
		return layersList;
	}

	private ClosableDrawEditor findDrawLayerEditorByLayer(Layer layer) {
		for (ClosableDrawEditor closableDrawLayerEditor : closableDrawLayerEditorList) {
			Layer closedLayer = closableDrawLayerEditor.getModifiedLayer();
			if (layer.getKeyId().getObjId().equals(closedLayer.getKeyId().getObjId()))
				return closableDrawLayerEditor;

			if (layer.equals(closedLayer)) {
				return closableDrawLayerEditor;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Draw Layers Manager Status:\n");
		for (ClosableDrawEditor closableLayersEditor : closableDrawLayerEditorList) {
			builder.append(closableLayersEditor);
		}
		return builder.toString();
	}
}
