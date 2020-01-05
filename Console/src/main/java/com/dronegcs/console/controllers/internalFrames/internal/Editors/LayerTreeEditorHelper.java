package com.dronegcs.console.controllers.internalFrames.internal.Editors;

import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.LayersGroup;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.EditedLayer;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerGroupEditable;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerManagerDbWrapper;
import com.dronegcs.console_plugin.layergroup_editor.LayersGroupEditor;
import com.dronegcs.console_plugin.layergroup_editor.LayersGroupsManager;
import com.mapviewer.gui.core.layers.AbstractLayer;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Date;

@Component
public class LayerTreeEditorHelper implements EditorHelper<EditedLayer> {

	private final static Logger LOGGER = LoggerFactory.getLogger(LayerTreeEditorHelper.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private LayersGroupsManager layersGroupsManager;

	private LayerTreeEditorHelper(@Autowired LayerManagerDbWrapper layerManagerDbWrapper) {
		LayerManagerDbWrapper.GuiLayer_From_DatabaseLayer_Loader loader = new LayerManagerDbWrapper.GuiLayer_From_DatabaseLayer_Loader() {
			@Override
			public boolean isRelevant(BaseObject layer) throws ObjectNotFoundRemoteException {
				if (layer instanceof LayersGroup)
					return true;
				return false;
			}

			@Override
			public AbstractLayer load(BaseObject layer) throws ObjectNotFoundRemoteException {
				AbstractLayer abstractLayer = new LayerGroupEditable(((BaseLayer) layer).getName());
				abstractLayer.setPayload(layer);
				return abstractLayer;
			}
		};

		// Sync the helper to react to layer manager loading of gui layer from db layer
		layerManagerDbWrapper.registerEventHandlerOnDbLayerChanges(LayersGroup.class, loader);
	}

	@Override
	public boolean isBuildMode() {
		return false;
	}

	@Override
	public void setBuildMode(boolean buildMode) {

	}

	@Override
	public ContextMenu buildTreeViewPopup(OperationalViewTree layerViewTree, OperationalViewMap layerViewMap, TreeItem treeItem) {
		LOGGER.debug("Building TreeView Popup");
		ContextMenu popup = new ContextMenu();

		if (treeItem == null)
			return popup;

		AbstractLayer layer = (AbstractLayer) treeItem.getValue();
		if (layer instanceof LayerGroupEditable) {
			boolean isRoot = layer.getParent() == null;

			if (isRoot) {
				MenuItem menuItemAddLayerGroup = new MenuItem("Add Layer Group");
				popup.getItems().add(menuItemAddLayerGroup);

				menuItemAddLayerGroup.setOnAction(handler -> {
					LayerGroupEditable layerGroup = new LayerGroupEditable("LayerGroup" + (new Date().getTime()));
					layerGroup.setWasEdited(true);
					TreeItem<AbstractLayer> newChild = layerViewTree.createTreeItem(layerGroup);
					layerViewTree.addTreeItemAction(newChild, treeItem);
				});
			}
		}
		else {
			MenuItem menuItemEditLayer = new MenuItem("Edit");
			popup.getItems().add(menuItemEditLayer);

			menuItemEditLayer.setOnAction(handler -> {
				layerViewTree.editTreeItemAction(treeItem);
			});
		}

		MenuItem menuItemRenameLayer = new MenuItem("Rename");
		popup.getItems().add(menuItemRenameLayer);

		menuItemRenameLayer.setOnAction(handler -> {
//			editTreeItemAction(treeItem);
		});

		MenuItem menuItemDeleteLayer = new MenuItem("Delete");
		popup.getItems().add(menuItemDeleteLayer);

		menuItemDeleteLayer.setOnAction(handler -> {
			layerViewTree.removeTreeItemAction(treeItem);
		});

		return popup;
	}

	@Override
	public ContextMenu buildMapViewPopup(OperationalViewMap layerViewMap, Point point) {
		return new ContextMenu();
	}

	@Override
	public void saveEditor() {

	}

	@Override
	public EditedLayer startEditing(EditedLayer layer) {
		return layer;
	}

	@Override
	public void removeItem(EditedLayer value) {
		LOGGER.info("Found layer to remove {}", value.getName());
		BaseLayer layer = (BaseLayer) value.getPayload();
		LayersGroupEditor editor = layersGroupsManager.openLayersGroupEditor((LayersGroup) layer);
		editor.deleteLayer();
	}

	@Override
	public void renameItem(EditedLayer value) {
		try {
			BaseLayer layer = (BaseLayer) value.getPayload();

			LayersGroupEditor layersGroupEditor = layersGroupsManager.openLayersGroupEditor((LayersGroup) layer);
			layersGroupEditor.setLayersGroupName(value.getName());
			layer = layersGroupEditor.getLayersGroup();

			value.setPayload(layer);
		}
		catch (Exception e) {
			LOGGER.error("Failed rename item named '{}', value='{}'",value.getName(), value, e);
		}
	}

	@Override
	public boolean isEdited(AbstractLayer abstractLayer) {
		LayersGroup layersGroup = (LayersGroup) abstractLayer.getPayload();
//		if (layersGroupsManager.getLayersGroupEditor(layersGroup) != null) {
//			LOGGER.debug("Modified layer group was found: {}", layersGroup);
//			return true;
//		}
		return false;
	}
}
