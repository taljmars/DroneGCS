package com.dronegcs.console.controllers.internalFrames.internal.Editors;

import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.Layer;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.*;
import com.dronegcs.console_plugin.layergroup_editor.LayersGroupsManager;
import com.dronegcs.console_plugin.perimeter_editor.CirclePerimeterEditor;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterEditor;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.perimeter_editor.PolygonPerimeterEditor;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.TextNotificationPublisherSvc;
import com.dronegcs.console_plugin.services.internal.logevents.DroneGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.gui.core.layers.AbstractLayer;
import com.gui.core.mapViewer.LayeredViewMap;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.awt.*;
import java.util.Arrays;
import java.util.Date;

@Component
public class PerimeterEditorHelper implements EditorHelper<LayerPerimeter> {

	private final static Logger LOGGER = LoggerFactory.getLogger(PerimeterEditorHelper.class);

	@Autowired
	private Drone drone;

	@Autowired
	private LoggerDisplayerSvc loggerDisplayerSvc;

	@Autowired
	private DialogManagerSvc dialogManagerSvc;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private PerimetersManager perimetersManager;

	@Autowired
	public LayersGroupsManager layerGroupManager;

	@Autowired
	private OperationalViewTree operationalViewTree;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;

	@Autowired
	private ApplicationContext applicationContext;

	private PerimeterEditor perimeterEditor;
	private LayerPerimeter modifiedLayerPerimeterOriginal = null;
	private boolean isBuildMode = false;

	public PerimeterEditorHelper(@Autowired LayerManagerDbWrapper layerManagerDbWrapper) {

		layerManagerDbWrapper.registerEventHandlerOnGuiLayerChanges(LayerPolygonPerimeter.class, ((guiLayer, dbLayer) -> {
			PerimeterEditor perimeterEditor = perimetersManager.openPerimeterEditor(guiLayer.getName(), PolygonPerimeter.class);
			((Layer) dbLayer).setObjectsUids(Arrays.asList(perimeterEditor.getPerimeter().getKeyId().getObjId()));
			return dbLayer;
		}));

		layerManagerDbWrapper.registerEventHandlerOnGuiLayerChanges(LayerCircledPerimeter.class, ((guiLayer, dbLayer) -> {
			PerimeterEditor perimeterEditor = perimetersManager.openPerimeterEditor(guiLayer.getName(), CirclePerimeter.class);
			((Layer) dbLayer).setObjectsUids(Arrays.asList(perimeterEditor.getPerimeter().getKeyId().getObjId()));
			return dbLayer;
		}));


		// Sync the helper to react to layer manager loading of gui layer from db layer
		layerManagerDbWrapper.registerEventHandlerOnDbLayerChanges(Layer.class, new LayerManagerDbWrapper.GuiLayer_From_DatabaseLayer_Loader() {
			@Override
			public boolean isRelevant(BaseObject layer) throws ObjectNotFoundRemoteException {
				if (((Layer) layer).getObjectsUids().isEmpty())
					return false;

				ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper = applicationContext.getBean(ObjectCrudSvcRemoteWrapper.class);
				String coreObjId = ((Layer) layer).getObjectsUids().iterator().next();
				BaseObject coreObj = objectCrudSvcRemoteWrapper.read(coreObjId);
				if (coreObj instanceof CirclePerimeter)
					return true;

				if (coreObj instanceof PolygonPerimeter)
					return true;

				return false;
			}

			@Override
			public AbstractLayer load(BaseObject layer) throws ObjectNotFoundRemoteException {
				ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper = applicationContext.getBean(ObjectCrudSvcRemoteWrapper.class);
				String coreObjId = ((Layer) layer).getObjectsUids().iterator().next();
				BaseObject coreObj = objectCrudSvcRemoteWrapper.read(coreObjId);
				perimetersManager.load(coreObj);
				if (coreObj instanceof CirclePerimeter) {
					String key = ((CirclePerimeter) coreObj).getCenter();
					com.dronedb.persistence.scheme.Point point = objectCrudSvcRemoteWrapper.read(key);
					perimetersManager.load(point);
				}
				else if (coreObj instanceof PolygonPerimeter) {
					for (String child : ((PolygonPerimeter) coreObj).getPoints()) {
						com.dronedb.persistence.scheme.Point point = objectCrudSvcRemoteWrapper.read(child);
						perimetersManager.load(point);
					}
				}
				layerGroupManager.load((BaseLayer) layer);

				LayerPerimeter layerGui = null;
				if (coreObj instanceof CirclePerimeter) {
					layerGui = new LayerCircledPerimeter(((Layer) layer).getName(), applicationContext.getBean(LayeredViewMap.class));
				}
				else if (coreObj instanceof PolygonPerimeter) {
					layerGui = new LayerPolygonPerimeter(((Layer) layer).getName(), applicationContext.getBean(LayeredViewMap.class));
				}
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
	public void setBuildMode(boolean isBuildMode) {
		this.isBuildMode = isBuildMode;
	}

	@Override
	public ContextMenu buildTreeViewPopup(OperationalViewTree layerViewTree, OperationalViewMap layerViewMap, TreeItem treeItem) {
		ContextMenu popup = new ContextMenu();

		AbstractLayer layer = (AbstractLayer) treeItem.getValue();

		if ( ! (layer instanceof LayerPerimeter)) {
			if (layer instanceof LayerGroupEditable) {
				MenuItem menuItemAddPolyLayer = new MenuItem("Add Polyline Perimeter");
				popup.getItems().add(menuItemAddPolyLayer);

				menuItemAddPolyLayer.setOnAction(handler -> {
					LayerPolygonPerimeter layerPolygonPerimeter = new LayerPolygonPerimeter("LayerPolyPerimeter" + (new Date().getTime()), layerViewMap);
					layerPolygonPerimeter.setApplicationContext(applicationContext);
					layerPolygonPerimeter.setWasEdited(true);
					TreeItem<AbstractLayer> newChild = layerViewTree.createTreeItem(layerPolygonPerimeter);
					layerViewTree.addTreeItemAction(newChild, treeItem);
				});

				MenuItem menuItemAddCircleLayer = new MenuItem("Add Circle Perimeter");
				popup.getItems().add(menuItemAddCircleLayer);

				menuItemAddCircleLayer.setOnAction(handler -> {
					LayerCircledPerimeter layerCircledPerimeter = new LayerCircledPerimeter("LayerCirclePerimeter" + (new Date().getTime()), layerViewMap);
					layerCircledPerimeter.setApplicationContext(applicationContext);
					layerCircledPerimeter.setWasEdited(true);
					TreeItem<AbstractLayer> newChild = layerViewTree.createTreeItem(layerCircledPerimeter);
					layerViewTree.addTreeItemAction(newChild, treeItem);
				});
			}
			return popup;
		}

		MenuItem menuItemUploadPerimeter = new MenuItem("Upload Perimeter");

		menuItemUploadPerimeter.setOnAction( e -> {
			LayerPerimeter uploadedLayerPerimeterCandidate = (LayerPolygonPerimeter) layer;
			LayerPerimeter uploadedLayerPerimeter = null;
			if (((LayerPolygonPerimeter) uploadedLayerPerimeterCandidate).getPolygon() != null) {
				loggerDisplayerSvc.logOutgoing("Uploading Perimeter To APM");
				drone.getPerimeter().setCompound(uploadedLayerPerimeterCandidate);
				uploadedLayerPerimeter = (LayerPerimeter) layerViewTree.switchCurrentLayer(uploadedLayerPerimeter, uploadedLayerPerimeterCandidate);
				uploadedLayerPerimeterCandidate = null;
				textNotificationPublisherSvc.publish("Perimeter Uploaded Successfully");
			}
		});

		popup.getItems().add(menuItemUploadPerimeter);

		return popup;
	}

	@Override
	public ContextMenu buildMapViewPopup(OperationalViewMap layerViewMap, Point point) {
		LOGGER.debug("Building popup");
		ContextMenu popup = new ContextMenu();

		MenuItem menuItemPerimeterAddPoint = new MenuItem("Add Point");
		MenuItem menuItemCirclePerimeterSetCenter = new MenuItem("Set Center");

		menuItemPerimeterAddPoint.setVisible(isBuildMode && (modifiedLayerPerimeterOriginal instanceof LayerPolygonPerimeter));
		menuItemCirclePerimeterSetCenter.setVisible(isBuildMode && (modifiedLayerPerimeterOriginal instanceof LayerCircledPerimeter));

		menuItemPerimeterAddPoint.setOnAction(arg -> {
			try {
				// Adding Point
				((PolygonPerimeterEditor) perimeterEditor).addPoint(layerViewMap.getPosition(point));

				// Adding Layer in database
				PolygonPerimeter polygonPerimeter = ((PolygonPerimeterEditor) perimeterEditor).getPerimeter();
				Layer layer = (Layer) modifiedLayerPerimeterOriginal.getPayload();
				layer.setObjectsUids(Arrays.asList(polygonPerimeter.getKeyId().getObjId()));
				layer = applicationContext.getBean(ObjectCrudSvcRemoteWrapper.class).update(layer);

				// Updating GUI layer
				modifiedLayerPerimeterOriginal.setPayload(layer);
				modifiedLayerPerimeterOriginal.setPerimeter(perimeterEditor.getPerimeter());
				applicationEventPublisher.publishEvent(new DroneGuiEvent(DroneGuiEvent.DRONE_GUI_COMMAND.PERIMETER_UPDATED_BY_MAP, modifiedLayerPerimeterOriginal));
				modifiedLayerPerimeterOriginal.regenerateMapObjects();
			}
			catch (Throwable e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database, error: " + e.getMessage());
			}
		});

		menuItemCirclePerimeterSetCenter.setOnAction( arg -> {
			String value = dialogManagerSvc.showInputDialog("Choose perimeter radius","",null, null, "50");
			if (value == null || value.isEmpty()) {
				LOGGER.debug("Irrelevant dialog result, result = \"{}\"", value);
				return;
			}
			if (!value.matches("[0-9]*")) {
				LOGGER.error("Value '{}' is illegal, must be numeric", value);
				return;
			}
			((CirclePerimeterEditor) perimeterEditor).setCenter(layerViewMap.getPosition(point));
			((CirclePerimeterEditor) perimeterEditor).setRadius(Integer.parseInt(value));
			modifiedLayerPerimeterOriginal.setPerimeter(perimeterEditor.getPerimeter());
			applicationEventPublisher.publishEvent(new DroneGuiEvent(DroneGuiEvent.DRONE_GUI_COMMAND.PERIMETER_UPDATED_BY_MAP, modifiedLayerPerimeterOriginal));
			modifiedLayerPerimeterOriginal.regenerateMapObjects();
		});

		popup.getItems().add(menuItemPerimeterAddPoint);
		popup.getItems().add(menuItemCirclePerimeterSetCenter);

		return popup;
	}

	@Override
	public void saveEditor() {
		modifiedLayerPerimeterOriginal.setPerimeter(perimeterEditor.getPerimeter());
		modifiedLayerPerimeterOriginal.setName(perimeterEditor.getPerimeter().getName());
		perimeterEditor = null;
		applicationEventPublisher.publishEvent(new DroneGuiEvent(DroneGuiEvent.DRONE_GUI_COMMAND.PERIMETER_EDITING_FINISHED, this.modifiedLayerPerimeterOriginal));

		setModifiedLayerPerimeterOriginal(null);
		setBuildMode(false);
	}

	public LayerPerimeter getModifiedLayerPerimeterOriginal() {
		return modifiedLayerPerimeterOriginal;
	}

	public void setModifiedLayerPerimeterOriginal(LayerPerimeter modifiedLayerPerimeterOriginal) {
		this.modifiedLayerPerimeterOriginal = modifiedLayerPerimeterOriginal;
	}


	@Override
	public LayerPerimeter startEditing(LayerPerimeter layer) {
		LOGGER.debug("Working on Perimeter Layer");
		modifiedLayerPerimeterOriginal = (LayerPerimeter) layer;
		modifiedLayerPerimeterOriginal.setWasEdited(true);
		setBuildMode(true);
		perimeterEditor = perimetersManager.openPerimeterEditor(modifiedLayerPerimeterOriginal.getPerimeter());
		Perimeter perimeter = perimeterEditor.getPerimeter();
		modifiedLayerPerimeterOriginal.setPerimeter(perimeter);

		return modifiedLayerPerimeterOriginal;
	}

	@Override
	public void removeItem(LayerPerimeter value) {
		LOGGER.info("Found perimeter to remove");
		Layer layer = (Layer) value.getPayload();
		Perimeter perimeter = perimetersManager.getPerimeter(layer.getObjectsUids().get(0));
		PerimeterEditor editor = perimetersManager.openPerimeterEditor(perimeter);
		editor.delete();

		layerGroupManager.removeItem(layer);
	}

	@Override
	public void renameItem(LayerPerimeter value) {
		try {
			Layer layerDb = (Layer) value.getPayload();

			String objId = layerDb.getKeyId().getObjId();

			layerDb = (Layer) layerGroupManager.getLayerItem(objId);
			layerDb.setName(value.getName());
			layerGroupManager.updateItem(layerDb);

			String missionUid = layerDb.getObjectsUids().get(0);

			Perimeter perimeter = perimetersManager.getPerimeter(missionUid);
			PerimeterEditor perimeterEditor = perimetersManager.openPerimeterEditor(perimeter);
			perimeterEditor.setPerimeterName(value.getName());

			value.setPayload(layerDb);
		}
		catch (Exception e) {
			LOGGER.error("Failed to rename item", e);
		}
	}

	@Override
	public boolean isEdited(AbstractLayer abstractLayer) {
		Perimeter perimeter = ((LayerPerimeter<Perimeter>) abstractLayer).getPerimeter();
		if (perimetersManager.isDirty(perimeter)) {
			LOGGER.debug("Modified perimeter was found: {}", perimeter);
			return true;
		}
		return false;
	}
}
