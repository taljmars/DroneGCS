package com.dronegcs.console_plugin.layergroup_editor;

import com.db.gui.persistence.scheme.LayersGroup;
import com.db.persistence.remote_exception.DatabaseValidationRemoteException;
import com.db.persistence.remote_exception.ObjectInstanceRemoteException;
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
public class LayersGroupManagerImpl implements LayersGroupsManager {

	private final static Logger LOGGER = LoggerFactory.getLogger(LayersGroupManagerImpl.class);

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	private ApplicationContext applicationContext;

	@Autowired @NotNull(message = "Internal Error: Failed to get drone object crud")
	private ObjectCrudSvcRemoteWrapper objectCrudSvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get query")
	private QuerySvcRemoteWrapper querySvcRemote;

	@Autowired @NotNull(message = "Internal Error: Failed to get layers object crud")
	private LayersCrudSvcRemoteWrapper layersCrudSvcRemoteWrapper;

	private List<ClosableLayersGroupEditor> closableLayersGroupEditorList;

	public LayersGroupManagerImpl() {
		closableLayersGroupEditorList = new ArrayList<>();
	}

	@Override
	public LayersGroupEditor openLayersGroupEditor(String initialName) throws LayersGroupUpdateException {
		LOGGER.debug("Setting new layersGroup to layers group editor");
		if (initialName == null || initialName.isEmpty()) {
			throw new RuntimeException("layers group name cannot be empty");
		}
		ClosableLayersGroupEditor layersGroupEditor = applicationContext.getBean(ClosableLayersGroupEditor.class);
		layersGroupEditor.open(initialName);
		closableLayersGroupEditorList.add(layersGroupEditor);
		return layersGroupEditor;
	}

	@Override
	public LayersGroupEditor openLayersGroupEditor(LayersGroup layersGroup) throws LayersGroupUpdateException {
		LOGGER.debug("Setting new layersGroup to layers group editor");
		ClosableLayersGroupEditor layersGroupEditor = findLayersGroupEditorByLayersGroup(layersGroup);
		if (layersGroupEditor == null) {
			LOGGER.debug("Editor not exist for Layers Group " + layersGroup.getName() + ", creating new one");
			layersGroupEditor = applicationContext.getBean(ClosableLayersGroupEditor.class);
			layersGroupEditor.open(layersGroup);
			closableLayersGroupEditorList.add(layersGroupEditor);
		}
		else {
			LOGGER.debug("Found existing layers group editor");
		}
		return layersGroupEditor;
	}

	@Override
	public void delete(LayersGroup layersGroup) {
		if (layersGroup == null) {
			LOGGER.error("Received Empty layers group, skip deletion");
			return;
		}

		try {
			ClosableLayersGroupEditor closableLayersGroupEditor = (ClosableLayersGroupEditor) openLayersGroupEditor(layersGroup);
			closableLayersGroupEditor.delete();
		}
		catch (LayersGroupUpdateException e) {
			LOGGER.error("Failed to delete layers group", e);
		}
	}

	@Override
	public LayersGroup update(LayersGroup layersGroup) throws LayersGroupUpdateException {
		try {
			ClosableLayersGroupEditor layersGroupEditor = findLayersGroupEditorByLayersGroup(layersGroup);
			if (layersGroupEditor == null)
				return objectCrudSvcRemote.update(layersGroup);

			return layersGroupEditor.update(layersGroup);
		}
		catch (DatabaseValidationRemoteException | ObjectInstanceRemoteException  e) {
			throw new LayersGroupUpdateException(e.getMessage());
		}
	}

	@Override
	public LayersGroup cloneLayersGroup(LayersGroup layersGroup) throws LayersGroupUpdateException {
//		try {
//			return layersCrudSvcRemoteWrapper.cloneLayer(layersGroup);
//		}
//		catch (ObjectNotFoundRemoteException | DatabaseValidationRemoteException | ObjectInstanceRemoteException  e) {
//			throw new MissionUpdateException(e.getMessage());
//		}
		throw new RuntimeException("Not implemented");
	}

	@Override
	public <T extends LayersGroupEditor> ClosingPair closeLayersGroupEditor(T layersGroupEditor, boolean shouldSave) {
		LOGGER.debug("closing layers group editor");
		if (!(layersGroupEditor instanceof ClosableLayersGroupEditor)) {
			return null;
		}
		ClosingPair<LayersGroup> layersGroupClosingPair = ((ClosableLayersGroupEditor) layersGroupEditor).close(shouldSave);
		closableLayersGroupEditorList.remove(layersGroupEditor);
		return layersGroupClosingPair;
	}

	@Override
	public Collection<ClosingPair<LayersGroup>> closeAllLayersGroupEditors(boolean shouldSave) {
		Collection<ClosingPair<LayersGroup>> closedlayersGroups = new ArrayList<>();
		Iterator<ClosableLayersGroupEditor> it = closableLayersGroupEditorList.iterator();
		while (it.hasNext()) {
			closedlayersGroups.add(it.next().close(shouldSave));
		}
		closableLayersGroupEditorList.clear();
		return closedlayersGroups;
	}

	@Override
	public int reloadEditors() {
//		if (closableLayersGroupEditorList.isEmpty())
//			throw new RuntimeException("Layers still exist");

		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(com.db.gui.persistence.scheme.LayersGroup.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllModifiedLayersGroup");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> modifiedLayersGroupList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} modified LayersGroup in total", modifiedLayersGroupList.size());
		modifiedLayersGroupList.forEach(element -> {
			try {
				this.openLayersGroupEditor((LayersGroup) element);
			}
			catch (LayersGroupUpdateException e) {
				e.printStackTrace();
			}
		});

		return modifiedLayersGroupList.size();
	}

	@Override
	public <T extends LayersGroupEditor> T getLayersGroupEditor(LayersGroup layersGroup) {
		return (T) findLayersGroupEditorByLayersGroup(layersGroup);
	}

	@Override
	public List<BaseObject> getAllLayersGroup() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(LayersGroup.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllLayersGroups");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> layersGroupList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} layers groups in total", layersGroupList.size());
		return layersGroupList;
	}

	@Override
	public List<BaseObject> getAllModifiedLayersGroup() {
		QueryRequestRemote queryRequestRemote = new QueryRequestRemote();
		queryRequestRemote.setClz(LayersGroup.class.getCanonicalName());
		queryRequestRemote.setQuery("GetAllModifiedLayersGroups");
		QueryResponseRemote queryResponseRemote = querySvcRemote.query(queryRequestRemote);
		List<BaseObject> layersGroupList = queryResponseRemote.getResultList();
		LOGGER.debug("There are currently {} modified layers groups in total", layersGroupList.size());
		return layersGroupList;
	}

	private ClosableLayersGroupEditor findLayersGroupEditorByLayersGroup(LayersGroup layersGroup) {
		for (ClosableLayersGroupEditor closableLayersGroupEditor : closableLayersGroupEditorList) {
			LayersGroup cloLayersGroup = closableLayersGroupEditor.getLayersGroup();
			if (layersGroup.getKeyId().getObjId().equals(cloLayersGroup.getKeyId().getObjId()))
				return closableLayersGroupEditor;

			if (layersGroup.equals(cloLayersGroup)) {
				return closableLayersGroupEditor;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Layers Groups Manager Status:\n");
		for (ClosableLayersGroupEditor closableLayersGroupEditor : closableLayersGroupEditorList) {
			builder.append(closableLayersGroupEditor);
		}
		return builder.toString();
	}
}
