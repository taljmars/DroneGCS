package com.dronegcs.console_plugin.layergroup_editor;

import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.LayersGroup;
import com.db.persistence.scheme.BaseObject;
import com.db.persistence.scheme.QueryRequestRemote;
import com.db.persistence.scheme.QueryResponseRemote;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.remote_services_wrappers.QuerySvcRemoteWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LayersGroupManagerImpl implements LayersGroupsManager {

	private final static Logger LOGGER = LoggerFactory.getLogger(LayersGroupManagerImpl.class);

	private Map<String, BaseLayer> dbItems;
	private Map<String, BaseLayer> dirtyDeleted;
	private Map<String, BaseLayer> dirtyItems;

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	private ApplicationContext applicationContext;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
	private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get query")
	private QuerySvcRemoteWrapper querySvcRemote;

	public LayersGroupManagerImpl() {
		dbItems = new HashMap<>();
		dirtyDeleted = new HashMap<>();
		dirtyItems = new HashMap<>();
	}

	@Override
	public LayersGroupEditor openLayersGroupEditor(String initialName) {
		LOGGER.debug("Setting new layersGroup to layers group editor");
		if (initialName == null || initialName.isEmpty()) {
			throw new RuntimeException("layers group name cannot be empty");
		}
		ClosableLayersGroupEditor layersGroupEditor = applicationContext.getBean(ClosableLayersGroupEditor.class);
		layersGroupEditor.open(initialName);
		return layersGroupEditor;
	}

	@Override
	public LayersGroupEditor openLayersGroupEditor(LayersGroup layersGroup) {
		LOGGER.debug("Setting new layersGroup to layers group editor");
		assert layersGroup != null;

		ClosableLayersGroupEditor layersGroupEditor = null;
		if (layersGroupEditor == null) {
			LOGGER.debug("Editor not exist for Layers Group '" + layersGroup.getName() + "', creating new one");
			layersGroupEditor = applicationContext.getBean(ClosableLayersGroupEditor.class);
			layersGroupEditor.open(layersGroup);
		}
		else {
			LOGGER.debug("Found existing layers group editor");
		}
		return layersGroupEditor;
	}

	@Override
	public Collection<ClosingPair<LayersGroup>> flushAllItems(boolean isPublish) {
		List<ClosingPair<LayersGroup>> res = new ArrayList<>();
		if (!isPublish){
			dbItems.clear();
			dirtyDeleted.clear();
			dirtyItems.clear();
			return res;
		}

		try {
			for (BaseLayer a : this.dirtyItems.values()) {
				BaseLayer updatedObj = objectCrudSvcRemote.update(a);
				if (updatedObj instanceof  LayersGroup) {
					LOGGER.debug("Adding to Publish-update list: {}", updatedObj);
					res.add(new ClosingPair<>((LayersGroup) updatedObj, false));
				}
				dbItems.put(updatedObj.getKeyId().getObjId(), updatedObj);
			}
			this.dirtyItems.clear();
			for (BaseLayer deletedObj : this.dirtyDeleted.values()) {
				objectCrudSvcRemote.delete(deletedObj);
				if (deletedObj instanceof  LayersGroup) {
					LOGGER.debug("Adding to Publish-delete list: {}", deletedObj);
					res.add(new ClosingPair<>((LayersGroup) deletedObj, false));
				}
				dbItems.remove(deletedObj.getKeyId().getObjId());
			}
			this.dirtyDeleted.clear();
		}
		catch (Exception e ) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return res;
	}

	@Override
	public BaseLayer getLayerItem(String uuid) {
		BaseLayer res = dirtyItems.get(uuid);
		if (res == null)
			res = dbItems.get(uuid);
		return res;
	}

	@Override
	public void removeItem(BaseLayer obj) {
		String key = obj.getKeyId().getObjId();
		dirtyItems.remove(key);
        dbItems.remove(key);
        dirtyDeleted.put(key, obj);
	}

	@Override
	public void updateItem(BaseLayer layer) {
		dirtyItems.put(layer.getKeyId().getObjId(), layer);
	}

	@Override
	public void load(BaseLayer item) {
		dbItems.put(item.getKeyId().getObjId(), item);
	}

	@Override
	public List<BaseObject> getAllLayers() {
		Set<BaseObject> res = new HashSet<>();
		res.addAll(dbItems.values());
		res.addAll(dirtyItems.values());
		return res.stream().collect(Collectors.toList());
	}

	@Override
	public void refreshAllLayers() {
		List<BaseObject> layersGroupList = new ArrayList();
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		QueryResponseRemote queryResponseRemote;

		queryRequestRemote.setClz(com.db.gui.persistence.scheme.LayersGroup.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllLayersGroup");
		queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		layersGroupList.addAll(queryResponseRemote.getResultList());
		LOGGER.debug("There are currently {} layers Group in total", layersGroupList .size());

		queryRequestRemote.setClz(com.db.gui.persistence.scheme.Layer.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllLayers");
		queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		layersGroupList.addAll(queryResponseRemote.getResultList());
		LOGGER.debug("There are currently {} layers Group in total", layersGroupList .size());

		dbItems.clear();
		dirtyItems.clear();
		dirtyDeleted.clear();

		for (BaseObject b : layersGroupList) {
			dbItems.put(b.getKeyId().getObjId(), (BaseLayer) b);
		}
	}

	@Override
	public List<BaseObject> getAllLayersGroup() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(LayersGroup.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllLayersGroup");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> layersGroupList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} layers groups in total", layersGroupList.size());
		return layersGroupList;
	}

	@Override
	public List<BaseObject> getAllModifiedLayersGroup() {
//		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
//		queryRequestRemote.setClz(LayersGroup.class.getCanonicalName());
//		queryRequestRemote.setQuery("GetAllModifiedLayersGroup");
//		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
//		List<BaseObject> layersGroupList = queryResponseRemote.getResultList();


		List<BaseObject> layersGroupList = new ArrayList<>();
		for (BaseObject a : dirtyItems.values()) {
			if (a instanceof LayersGroup)
				layersGroupList.add(a);
		}

		LOGGER.debug("There are currently {} modified layers groups in total", layersGroupList.size());
		return layersGroupList;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Layers Groups Manager Status:\n");
//		for (ClosableLayersGroupEditor closableLayersGroupEditor : closableLayersGroupEditorList.values()) {
//			builder.append(closableLayersGroupEditor);
//		}
		return builder.toString();
	}
}
