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
import com.gui.core.layers.LayerGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.*;

@Component
public class DrawManagerImpl implements DrawManager {

	private final static Logger LOGGER = LoggerFactory.getLogger(DrawManagerImpl.class);

	private Map<String, BaseObject> dbItems;
	private Map<String, BaseObject> dirtyDeleted;
	private Map<String, BaseObject> dirtyItems;

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	private ApplicationContext applicationContext;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
	private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get query")
	private QuerySvcRemoteWrapper querySvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get layers object crud")
	private LayersCrudSvcRemoteWrapper layerCrudSvcRemote;

	public DrawManagerImpl() {
		dbItems = new HashMap<>();
		dirtyDeleted = new HashMap<>();
		dirtyItems = new HashMap<>();
	}

	@Override
	public DrawEditor openDrawLayerEditor(String initialName) {
		LOGGER.debug("Setting new layer to drawLayer editor");
		if (initialName == null || initialName.isEmpty()) {
			throw new RuntimeException("Layer name cannot be empty");
		}
		ClosableDrawEditor drawLayerEditor = applicationContext.getBean(ClosableDrawEditor.class);
		drawLayerEditor.open(initialName);
		return drawLayerEditor;
	}

	@Override
	public DrawEditor openDrawLayerEditor(Layer layer) {
		assert layer != null;
		LOGGER.debug("Editor not exist for layer " + layer.getName() + ", creating new one");
		ClosableDrawEditor drawLayerEditor = applicationContext.getBean(ClosableDrawEditor.class);
		drawLayerEditor.open(layer);
		return drawLayerEditor;
	}

    @Override
    public Shape getLayerItems(String drawItemUid) {
        Shape res = (Shape) dirtyItems.get(drawItemUid);
        if (res == null)
            res = (Shape) dbItems.get(drawItemUid);
        return res;
    }

	@Override
	public List<BaseObject> getLayerItems(Layer layer) {
		List<BaseObject> objects = new ArrayList<>();
		List<String> itemUids = layer.getObjectsUids();
		DrawEditor c = openDrawLayerEditor(layer);
		if (c != null) {
			objects.addAll(c.getLayerItems());
			return objects;
		}
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
    public Collection<ClosingPair<Layer>> flushAllItems(boolean isPublish) {
        List<ClosingPair<Layer>> res = new ArrayList<>();
        if (!isPublish){
        	dbItems.clear();
        	dirtyDeleted.clear();
        	dirtyItems.clear();
        	return res;
		}

        try {
            for (BaseObject a : this.dirtyItems.values()) {
                BaseObject updatedObj = objectCrudSvcRemote.update(a);
                if (updatedObj instanceof  Layer) {
                    LOGGER.debug("Adding to Publish-update list: {}", updatedObj);
                    res.add(new ClosingPair<>((Layer) updatedObj, false));
                }
                dbItems.put(updatedObj.getKeyId().getObjId(), updatedObj);
            }
            this.dirtyItems.clear();
            for (BaseObject deletedObj : this.dirtyDeleted.values()) {
                objectCrudSvcRemote.delete(deletedObj);
                if (deletedObj instanceof  Layer) {
                    LOGGER.debug("Adding to Publish-delete list: {}", deletedObj);
                    res.add(new ClosingPair<>((Layer) deletedObj, false));
                }
                dbItems.remove(deletedObj.getKeyId().getObjId());
            }
            this.dirtyDeleted.clear();
        }
        catch (Exception e ) {
            System.out.println(e.getMessage());
        }
        return res;
    }

	@Override
	public List<BaseObject> getAllDrawLayers() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(Layer.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllLayers");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> layersList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} layers in total", layersList.size());
        for (BaseObject a : layersList) {
            dbItems.put(a.getKeyId().getObjId(), a);
        }
		return layersList;
	}

	@Override
	public List<BaseObject> getAllModifiedDrawLayers() {
//		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
//		queryRequestRemote.setClz(Layer.class.getCanonicalName());
//		queryRequestRemote.setQuery("GetAllModifiedLayers");
//		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
//		List<BaseObject> layersList = queryResponseRemote.getResultList();
//		LOGGER.debug("There are currently {} modified layers in total", layersList.size());

        List<BaseObject> layersList = new ArrayList<>();
        for (BaseObject a : dirtyItems.values()) {
            if (a instanceof Layer)
                layersList.add(a);
        }
		return layersList;
	}

    @Override
    public void updateItem(BaseObject object) {
        dirtyItems.put(object.getKeyId().getObjId(), object);
    }

    @Override
    public void removeItem(BaseObject object) {
        String key = object.getKeyId().getObjId();
        dirtyItems.remove(key);
        dbItems.remove(key);
        dirtyDeleted.put(key, object);
    }

    @Override
    public boolean isDirty(BaseObject item) {
        String key = item.getKeyId().getObjId();
        return dirtyItems.containsKey(key) || dirtyDeleted.containsKey(key);
    }

	@Override
	public void load(BaseObject item) {
		dbItems.put(item.getKeyId().getObjId(), item);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Draw Layers Manager Status:\n");
//		for (ClosableDrawEditor closableLayersEditor : closableDrawLayerEditorList.values()) {
//			builder.append(closableLayersEditor);
//		}
		return builder.toString();
	}
}
