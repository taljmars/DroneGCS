package com.dronegcs.console.controllers.internalFrames.internal.Editors;

import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.Shape;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerDraw;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerGroupEditable;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerManagerDbWrapper;
import com.dronegcs.console_plugin.draw_editor.DrawEditor;
import com.dronegcs.console_plugin.draw_editor.DrawManager;
import com.dronegcs.console_plugin.layergroup_editor.LayersGroupsManager;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.gui.core.layers.AbstractLayer;
import com.gui.core.mapViewer.LayeredViewMap;
import com.gui.core.mapViewerObjects.MapLineImpl;
import com.gui.core.mapViewerObjects.MapMarkerCircle;
import com.gui.core.mapViewerObjects.MapMarkerDot;
import com.gui.is.interfaces.mapObjects.MapLine;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Date;

@Component
public class DrawingEditorHelper implements EditorHelper<LayerDraw> {

	private final static Logger LOGGER = LoggerFactory.getLogger(DrawingEditorHelper.class);

	private boolean isBuildMode = false;
	private LayerDraw modifiedLayer = null;
	private DrawEditor drawEditor;

	@Autowired
	public LayersGroupsManager layerGroupManager;

	@Autowired
	private DrawManager drawManager;

	@Autowired
	private ApplicationContext applicationContext;

	public DrawingEditorHelper(@Autowired LayerManagerDbWrapper layerManagerDbWrapper) {

		layerManagerDbWrapper.registerEventHandlerOnGuiLayerChanges(LayerDraw.class, (guiLayer, dbLayer) -> {
			DrawEditor drawEditor = drawManager.openDrawLayerEditor((Layer) dbLayer);
			return drawEditor.getModifiedLayer();
		});

		// Sync the helper to react to layer manager loading of gui layer from db layer
		layerManagerDbWrapper.registerEventHandlerOnDbLayerChanges(Layer.class, new LayerManagerDbWrapper.GuiLayer_From_DatabaseLayer_Loader() {
			@Override
			public boolean isRelevant(BaseObject layer) throws ObjectNotFoundRemoteException {
				ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper = applicationContext.getBean(ObjectCrudSvcRemoteWrapper.class);
				if (((Layer) layer).getObjectsUids().isEmpty())
					return true;
				String coreObjId = ((Layer) layer).getObjectsUids().iterator().next();
				BaseObject coreObj = objectCrudSvcRemoteWrapper.read(coreObjId);
				if  ((coreObj instanceof Mission) ||
					(coreObj instanceof CirclePerimeter) ||
					(coreObj instanceof PolygonPerimeter)) {
					return false;
				}
				return true;
			}

			@Override
			public AbstractLayer load(BaseObject layer) throws ObjectNotFoundRemoteException {
				ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper = applicationContext.getBean(ObjectCrudSvcRemoteWrapper.class);
				Layer coreObj = (Layer) layer;
				drawManager.load(layer);
				for (String child : coreObj.getObjectsUids()) {
					Shape item = objectCrudSvcRemoteWrapper.read(child);
					drawManager.load(item);
				}

				layerGroupManager.load((BaseLayer) layer);
				LayerDraw layerGui = new LayerDraw(((Layer) layer).getName(), applicationContext.getBean(LayeredViewMap.class));
				layerGui.setApplicationContext(applicationContext);
				layerGui.setPayload(layer);
				layerGui.loadMapObjects();
				return layerGui;
			}
		});
	}

	@Override
	public boolean isBuildMode() {
		return isBuildMode;
	}

	@Override
	public void setBuildMode(boolean buildMode) {
		this.isBuildMode = buildMode;
	}

	@Override
	public ContextMenu buildTreeViewPopup(OperationalViewTree layerViewTree, OperationalViewMap layerViewMap, TreeItem treeItem) {
		ContextMenu popup = new ContextMenu();

		if (treeItem == null)
			return popup;

		AbstractLayer layer = (AbstractLayer) treeItem.getValue();

		if (!isBuildMode && ( ! (layer instanceof LayerDraw))) {
			if (layer instanceof LayerGroupEditable) {
				MenuItem menuItemAddDrawingLayer = new MenuItem("Add Drawing Layer");
				popup.getItems().add(menuItemAddDrawingLayer);

				menuItemAddDrawingLayer.setOnAction(handler -> {
					LayerDraw layerDraw = new LayerDraw("LayerDraw" + (new Date().getTime()), layerViewMap);
					layerDraw.setApplicationContext(applicationContext);
					layerDraw.setWasEdited(true);
					TreeItem<AbstractLayer> newChild = layerViewTree.createTreeItem(layerDraw);
					layerViewTree.addTreeItemAction(newChild, treeItem);
				});
			}
		}

		return popup;
	}


	private MapLine tempMapLine = null;
	private boolean isBuildingLine = false;

	@Override
	public ContextMenu buildMapViewPopup(OperationalViewMap layerViewMap, Point point) {
		ContextMenu popup = new ContextMenu();

		if (isBuildingLine) {
			MenuItem menuItemAddPoint = new MenuItem("Add Point");
			menuItemAddPoint.setOnAction(arg -> {
				layerViewMap.removeMapLine(tempMapLine);
				tempMapLine.addCoordinate(layerViewMap.getPosition(point));
				layerViewMap.addMapLine(tempMapLine);
			});
			popup.getItems().add(menuItemAddPoint);

			MenuItem menuItemDone = new MenuItem("Done");
			menuItemDone.setOnAction( arg -> {
				isBuildingLine = false;
				tempMapLine = null;
			});
			popup.getItems().add(menuItemDone);

			return popup;
		}

		if (isBuildMode && !isBuildingLine) {
			MenuItem menuItemAddMarker = new MenuItem("Add Marker");
			popup.getItems().add(menuItemAddMarker);

			MenuItem menuItemAddMarkerImage = new MenuItem("Add Marker with Image");
			popup.getItems().add(menuItemAddMarkerImage);

			MenuItem menuItemAddCircle = new MenuItem("Add Circle");
			popup.getItems().add(menuItemAddCircle);

			MenuItem menuItemAddLine = new MenuItem("Add Line");
			popup.getItems().add(menuItemAddLine);

			MenuItem menuItemAddPolygon = new MenuItem("Add Polygon");
			popup.getItems().add(menuItemAddPolygon);


			javafx.scene.image.Image img = new Image(this.getClass().getResource("/com/mapImages/droneConnected.png").toString());
			javafx.scene.image.ImageView iView = new javafx.scene.image.ImageView(img);
			iView.setFitHeight(40);
			iView.setFitWidth(40);

			menuItemAddMarker.setOnAction(arg -> {
				drawEditor.addMarker(layerViewMap.getPosition(point));
				modifiedLayer.setPayload(drawEditor.getModifiedLayer());
				modifiedLayer.loadMapObjects();
				modifiedLayer.addMapMarker(new MapMarkerDot("12" , layerViewMap.getPosition(point)));
				modifiedLayer.regenerateMapObjects();
			});
			menuItemAddMarkerImage.setOnAction(arg -> {
				modifiedLayer.addMapMarker(new MapMarkerDot(iView, 45.0, layerViewMap.getPosition(point)));
				modifiedLayer.regenerateMapObjects();
			});
			menuItemAddCircle.setOnAction(arg -> {
				modifiedLayer.addMapMarker(new MapMarkerCircle("12", layerViewMap.getPosition(point), 50000));
				modifiedLayer.regenerateMapObjects();
			});
			menuItemAddLine.setOnAction(arg -> {
				tempMapLine = new MapLineImpl();
				modifiedLayer.addMapLine(tempMapLine);
				isBuildingLine = true;
			});
		}

		return popup;
	}

//	@Override
//	public void cancelEditor() {
//		unsetModifiedLayer();
//		setBuildMode(false);
//	}

	@Override
	public void saveEditor() {
		unsetModifiedLayer();
		setBuildMode(false);
	}

	private void setModifiedLayer(LayerDraw layerDraw) {
		this.drawEditor = drawManager.openDrawLayerEditor((Layer) layerDraw.getPayload());
//		if (this.drawEditor == null) {
//			LOGGER.debug("Open new layer editor");
//			this.drawEditor = drawManager.openDrawLayerEditor((Layer) layerDraw.getPayload());
//		}
		this.modifiedLayer = layerDraw;
	}

	private void unsetModifiedLayer() {
		this.drawEditor = null;
		this.modifiedLayer = null;
	}

	@Override
	public LayerDraw startEditing(LayerDraw layer) {
		LOGGER.debug("Working on Drawing Layer");
		modifiedLayer = layer;
		modifiedLayer.setWasEdited(true);
		setBuildMode(true);
		drawEditor = drawManager.openDrawLayerEditor((Layer) layer.getPayload());
		Layer drawLayer = drawEditor.getModifiedLayer();
		modifiedLayer.setName(drawLayer.getName());
		return modifiedLayer;
	}

	@Override
	public void removeItem(LayerDraw value) {
		LOGGER.info("Found drawing layer to remove");
		Layer layer = (Layer) value.getPayload();

		DrawEditor editor = drawManager.openDrawLayerEditor(layer);
		editor.deleteLayer();

		layerGroupManager.removeItem(layer);
	}

	@Override
	public void renameItem(LayerDraw value) {
		try {
			Layer layer = (Layer) value.getPayload();
			DrawEditor drawEditor = drawManager.openDrawLayerEditor(layer);
			drawEditor.setDrawLayerName(value.getName());

			value.setPayload(drawEditor.getModifiedLayer());
		}
		catch (Exception e) {
			LOGGER.error("Failed rename item", e);
		}
	}

	@Override
	public boolean isEdited(AbstractLayer abstractLayer) {
		Layer layer = (Layer) abstractLayer.getPayload();

		if (drawManager.isDirty(layer)) {
			LOGGER.debug("Modified draw layer was found: {}", layer);
			return true;
		}
		return false;
	}
}
