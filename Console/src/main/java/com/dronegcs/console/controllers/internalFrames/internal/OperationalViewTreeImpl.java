package com.dronegcs.console.controllers.internalFrames.internal;

import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console.controllers.internalFrames.internal.Editors.*;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.*;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.services.*;
import com.dronegcs.console_plugin.services.internal.MissionComparatorException;
import com.dronegcs.console_plugin.services.internal.convertors.MissionCompilationException;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnWaypointManagerListener;
import com.dronegcs.mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.gui.core.layers.AbstractLayer;
import com.gui.core.layers.LayerGroup;
import com.gui.core.mapTree.internal.CheckBoxViewTree;
import javafx.application.Platform;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent.QUAD_GUI_COMMAND.EDITMODE_EXISTING_LAYER_START;
import static com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent.QUAD_GUI_COMMAND.PRIVATE_SESSION_STARTED;

@Component
public class OperationalViewTreeImpl extends CheckBoxViewTree implements OnWaypointManagerListener, OperationalViewTree {

	private final static Logger LOGGER = LoggerFactory.getLogger(OperationalViewTreeImpl.class);

	@Autowired
	@NotNull(message = "Internal Error: Failed to get application context")
	protected ApplicationContext applicationContext;

	@Autowired
	@NotNull(message = "Internal Error: Missing event publisher")
	protected ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get mission compiler")
	private MissionCompilerSvc missionCompilerSvc;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get mission manager")
	private MissionsManager missionsManager;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get perimeter manager")
	private PerimetersManager perimetersManager;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;

	@Autowired @NotNull(message = "Internal Error: Failed to get missions editor helper")
	private MissionEditorHelper missionEditorMode;

	@Autowired @NotNull(message = "Internal Error: Failed to get perimeter editor helper")
	private PerimeterEditorHelper perimeterEditorMode;

	@Autowired @NotNull(message = "Internal Error: Failed to get perimeter editor helper")
	private DrawingEditorHelper drawingEditorMode;

	@Autowired @NotNull(message = "Internal Error: Failed to get layers editor helper")
	private LayerTreeEditorHelper layerTreeEditorMode;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;

	@Autowired
	private RuntimeValidator runtimeValidator;

	private LayerMission uploadedLayerMissionCandidate = null;
	private LayerMission uploadedLayerMission = null;

	private TreeItem modifiedItem = null;
	private boolean activePrivateSession = false;
	private HashMap<Class<? extends EditedLayer>, EditorHelper> helpers;

	public OperationalViewTreeImpl() {
		super(new EditableLayeredCheckBoxTreeCellEditorConvertor());
	}

	private static int called;

	@SuppressWarnings("unchecked")
	@PostConstruct
	protected void init() {
		Assert.isTrue(++called == 1, "Not a Singleton");

		drone.getWaypointManager().addWaypointManagerListener(this);

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		helpers = new HashMap<>();
		helpers.put(LayerMission.class, missionEditorMode);
		helpers.put(LayerCircledPerimeter.class, perimeterEditorMode);
		helpers.put(LayerPolygonPerimeter.class, perimeterEditorMode);
		helpers.put(LayerDraw.class, drawingEditorMode);
		helpers.put(LayerGroupEditable.class, layerTreeEditorMode);
	}

	@Override
	public void reloadData() {
		LOGGER.info("Load Tree Skeleton");

		CheckBoxTreeItem<AbstractLayer> currentRootItem = (CheckBoxTreeItem<AbstractLayer>) getRoot();

		LOGGER.info("Clean layer Manger for any objects");
		layerManager.flush();

		LOGGER.info("Reload root and all the tree to layer manager");
		LayerGroupEditable rootLayer = (LayerGroupEditable) layerManager.getRoot();//new LayerGroup("Layers");

		LOGGER.info("Clear the map");
		getLayeredViewMap().flush();

		LOGGER.info("Reload tree");
		CheckBoxTreeItem<AbstractLayer> rootItem = loadTree(rootLayer);
		LOGGER.info("Setting root");
		setRoot(rootItem);

		LOGGER.info("Restore View setting");
		restoreViewSettings(currentRootItem, rootItem);

		LOGGER.info("Reload Editors");
		markEditedItems();
	}

	private void markEditedItems() {
//		int editorsAmount = 0;
//		for (EditorHelper helper : new HashSet<>(helpers.values())) {
//			LOGGER.debug("Regenerating editors for {}", helper.getClass().getCanonicalName());
//			editorsAmount += helper.reloadEditors();
//		}
//		if (editorsAmount > 0) {
//			LOGGER.debug("Start marking tree item as edited");
//			markEditedItems(getRoot().getValue());
//		}
//
//		if (editorsAmount > 0) {
//			applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PRIVATE_SESSION_STARTED));
//		}
	}

	private void markEditedItems(AbstractLayer abstractLayer) {
		if (abstractLayer instanceof LayerGroup) {
			for (AbstractLayer a : ((LayerGroup) abstractLayer).getChildren()) {
				markEditedItems(a);
			}
		}
		LOGGER.debug("Searching for class {} obj {}", abstractLayer.getClass().getCanonicalName(), abstractLayer);
		if (helpers.get(abstractLayer.getClass()).isEdited(abstractLayer)) {
			LOGGER.debug("Found modified layer {}", abstractLayer);
			abstractLayer.setWasEdited(true);
		}
	}

	private void getExpanded(CheckBoxTreeItem<AbstractLayer> itemPointer, Set<String> list) {
		if (itemPointer.isExpanded())
			list.add(itemPointer.getValue().getName());

		itemPointer.getChildren().forEach(node -> getExpanded((CheckBoxTreeItem<AbstractLayer>) node, list));
	}

	private void setExpanded(CheckBoxTreeItem<AbstractLayer> itemPointer, Set<String> list) {
		itemPointer.setExpanded(list.contains(itemPointer.getValue().getName()));
		itemPointer.getChildren().forEach(node -> setExpanded((CheckBoxTreeItem<AbstractLayer>) node, list));
	}

	private void getSelected(CheckBoxTreeItem<AbstractLayer> itemPointer, Set<String> list) {
		if (itemPointer.isSelected())
			list.add(itemPointer.getValue().getName());

		itemPointer.getChildren().forEach(node -> getSelected((CheckBoxTreeItem<AbstractLayer>) node, list));
	}

	private void setSelected(CheckBoxTreeItem<AbstractLayer> itemPointer, Set<String> list) {
		itemPointer.setSelected(list.contains(itemPointer.getValue().getName()));
		itemPointer.getChildren().forEach(node -> setSelected((CheckBoxTreeItem<AbstractLayer>) node, list));
	}

	private void restoreViewSettings(CheckBoxTreeItem<AbstractLayer> oldRootItem, CheckBoxTreeItem<AbstractLayer> newRootItem) {
		if (oldRootItem == null) {
			LOGGER.debug("Old tree doesn't exist setting root as expanded only");
			newRootItem.setExpanded(true);
			return;
		}

		Set<String> expandedNodes = new HashSet<>();
		Set<String> selectedNodes = new HashSet<>();

		getExpanded(oldRootItem, expandedNodes);
		getSelected(oldRootItem, selectedNodes);

		LOGGER.debug("Expanded should be:");
		expandedNodes.forEach(node -> LOGGER.debug(node));
		LOGGER.debug("Selected should be:");
		selectedNodes.forEach(node -> LOGGER.debug(node));

		setExpanded(newRootItem, expandedNodes);
		setSelected(newRootItem, selectedNodes);

		refresh();
	}

	@Override
	public void updateTreeItemName(String fromText, TreeItem<AbstractLayer> treeItem) {
		LOGGER.debug("Named changed from '" + fromText + "' to '" + treeItem.getValue().getName() + "'");
		helpers.get(treeItem.getValue().getClass()).renameItem((EditedLayer) treeItem.getValue());
		applicationEventPublisher.publishEvent(new QuadGuiEvent(PRIVATE_SESSION_STARTED, treeItem.getValue()));
	}

	@Override
	public ContextMenu getPopupMenu(TreeItem<AbstractLayer> treeItem) {
//		ContextMenu popup = super.getPopupMenu(treeItem);
		ContextMenu popup = new ContextMenu();


		popup.getItems().addAll(layerTreeEditorMode.buildTreeViewPopup(this, (OperationalViewMap) getLayeredViewMap(), treeItem).getItems());
		popup.getItems().addAll(missionEditorMode.buildTreeViewPopup(this, (OperationalViewMap) getLayeredViewMap(), treeItem).getItems());
		popup.getItems().addAll(perimeterEditorMode.buildTreeViewPopup(this, (OperationalViewMap) getLayeredViewMap(), treeItem).getItems());
		popup.getItems().addAll(drawingEditorMode.buildTreeViewPopup(this, (OperationalViewMap) getLayeredViewMap(), treeItem).getItems());

		if (!(treeItem.getValue() instanceof AbstractLayer))
			return popup;

//		// Check if we are in editing mode
		if (treeItem.equals(modifiedItem)) {
			LOGGER.debug("Same layer , limit popups");
			return popup;
		}

		ContextMenu operationalPopup = new ContextMenu();

		AbstractLayer layer = treeItem.getValue();

		MenuItem menuItemEdit = new MenuItem("Edit");

		menuItemEdit.setOnAction(e -> {
			if (layer instanceof LayerMission || layer instanceof LayerPerimeter || layer instanceof LayerDraw) {
				((CheckBoxTreeItem) treeItem).selectedProperty().setValue(true);
				modifiedItem = treeItem;
				refresh();
				applicationEventPublisher.publishEvent(new QuadGuiEvent(EDITMODE_EXISTING_LAYER_START, layer));
			}
		});
		
		operationalPopup.getItems().addAll(popup.getItems());
		return operationalPopup;
	}

	@Override
	public <P extends TreeItem<AbstractLayer>> void handleTreeItemClick(P node) {
		if (node == null) {
			LOGGER.debug("Ignore this message - it a bug in the GUI I/S were event is raised once replacing the root");
			return;
		}
		QuadGuiEvent.QUAD_GUI_COMMAND eventType = null;
		AbstractLayer layer = node.getValue();
		if (layer instanceof LayerMission) {
			if (modifiedItem != null && modifiedItem.getValue() == layer)
				eventType = QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_EDITING_STARTED;
			else
				eventType = QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_VIEW_ONLY_STARTED;
		}
		if (layer instanceof LayerPerimeter) {
			if (modifiedItem != null && modifiedItem.getValue() == layer)
				eventType = QuadGuiEvent.QUAD_GUI_COMMAND.PERIMETER_EDITING_STARTED;
			else
				eventType = QuadGuiEvent.QUAD_GUI_COMMAND.PERIMETER_VIEW_ONLY_STARTED;
		}
		else {
			applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_VIEW_ONLY_FINISHED, layer));
			applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PERIMETER_VIEW_ONLY_FINISHED, layer));
		}

		if (eventType != null)
			applicationEventPublisher.publishEvent(new QuadGuiEvent(eventType, layer));
	}

	@Override
	public AbstractLayer switchCurrentLayer(AbstractLayer fromLayer, AbstractLayer toLayer) {
		AbstractLayer finalLayer = null;

		if (toLayer.equals(fromLayer)) {
			loggerDisplayerSvc.logGeneral("Current droneMission layer is updated");
			return finalLayer;
		}

		finalLayer = toLayer;

		if (fromLayer != null) {
			// Means the GUI is updated with old uploaded droneMission
			CurrentPrefixRemove(fromLayer);
			loggerDisplayerSvc.logGeneral("Previous droneMission prefix was removed");
		}
		else {
			// Means we are not aware of any uploaded droneMission
			//addSubLayer(finalLayer);
			finalLayer.regenerateMapObjects();
			loggerDisplayerSvc.logGeneral("A new layer was created for current droneMission");
		}

		CurrentPrefixAdd(finalLayer);
		return finalLayer;
	}
	
	private void CurrentPrefixRemove(AbstractLayer old_layer) {
		if (old_layer.getName().contains(UPLOADED_PREFIX))
			old_layer.setName(old_layer.getName().substring(UPLOADED_PREFIX.length(), old_layer.getName().length()));
		
		refresh();
	}

	private void CurrentPrefixAdd(AbstractLayer finalLayer) {
		if (!finalLayer.getName().contains(UPLOADED_PREFIX))
			finalLayer.setName(UPLOADED_PREFIX + finalLayer.getName());
		
		refresh();
	}
	
	@Override
	public void onBeginWaypointEvent(WaypointEvent_Type wpEvent) {		
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Start Downloading Waypoints");
			return;
		}
		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Start Uploading Waypoints");
			return;
		}

		loggerDisplayerSvc.logError("Failed to Start Syncing (" + wpEvent.name() + ")");
		textNotificationPublisherSvc.publish("DroneMission Sync failed");
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Downloading MavlinkWaypoint " + index + "/" + count);
			return;
		}

		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Uploading MavlinkWaypoint " + index + "/" + count);
			return;
		}

		loggerDisplayerSvc.logError("Unexpected Syncing Failure (" + wpEvent.name() + ")");
		textNotificationPublisherSvc.publish("DroneMission Sync failed");
	}

	@Override
	public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
		Platform.runLater( () -> { 
			if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
				loggerDisplayerSvc.logIncoming("Waypoints downloaded");
				if (drone.getDroneMission() == null) {
					loggerDisplayerSvc.logError("Failed to find droneMission");
					return;
				}

				try {
					Mission mission = missionCompilerSvc.decompile(drone.getDroneMission());
					LayerMission layerMission = getLayerMissionWithSimilarPropertiesToMission(mission);
					if (layerMission == null) {
						LOGGER.debug("Creating layer for the newly created mission");
						layerMission = new LayerMission(mission, getLayeredViewMap());
						layerMission.startEditing();
						layerMission.setApplicationContext(applicationContext);
						addLayer(layerMission, (LayerGroupEditable) getRoot().getValue());
						LOGGER.debug("New layer was added {}", layerMission.toString2());
					}
					else {
						LOGGER.debug("Found layer with the same mission named '{}'", layerMission.getName());
						LOGGER.debug("Clearing autogenerated mission");
//						MissionEditor missionEditor = missionsManager.getMissionEditor(mission);
//						missionEditor.delete();
//						loggerDisplayerSvc.logGeneral("Mission already exist in the DB");
						throw new RuntimeException("Uper 3 lines should be handled");
					}

					uploadedLayerMission = (LayerMission) switchCurrentLayer(uploadedLayerMission, layerMission);

					loggerDisplayerSvc.logGeneral("DroneMission was updated in droneMission tree");
					textNotificationPublisherSvc.publish("DroneMission successfully downloaded");
				}
				catch (MissionCompilationException | MissionComparatorException  e) {
					dialogManagerSvc.showErrorMessageDialog("Failed to decompile mission", e);
					return;
				}
				return;
			}
	
			if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
				loggerDisplayerSvc.logIncoming("Waypoints uploaded");
				if (drone.getDroneMission() == null) {
					loggerDisplayerSvc.logError("Failed to find droneMission");
					return;
				}
				uploadedLayerMission = (LayerMission) switchCurrentLayer(uploadedLayerMission, uploadedLayerMissionCandidate);
				uploadedLayerMissionCandidate = null;
				loggerDisplayerSvc.logGeneral("DroneMission was updated in droneMission tree");
				textNotificationPublisherSvc.publish("DroneMission successfully uploaded");
				return;
			}
			
			loggerDisplayerSvc.logError("Failed to Sync Waypoints (" + wpEvent.name() + ")");
			textNotificationPublisherSvc.publish("DroneMission Sync failed");
			 dialogManagerSvc.showErrorMessageDialog("Failed to upload mission", null);
		});
	}

	public void regenerateTree() {
		System.err.println("Regenerate Tree");
		this.refresh();
	}

	@Override
	public boolean hasPrivateSession() {
		return activePrivateSession;
	}

	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
		Platform.runLater(() -> {
			switch (command.getCommand()) {
				case MISSION_EDITING_FINISHED: {
//					LayerMission layerMission = (LayerMission) command.getSource();
//					if (layerMission.getMission() == null) {
////						Means we were left without a mission, we need to clear the item
//						removeLayer(layerMission);
//					} else {
//						String missionName = layerMission.getMission().getName();
//						layerMission.setName(missionName);
//					}
					modifiedItem = null;
					refresh();
					break;
				}
				case PERIMETER_EDITING_FINISHED: {
					LayerPerimeter layerPerimeter = (LayerPerimeter) command.getSource();
					if (layerPerimeter.getPerimeter() == null) {
						removeLayer(layerPerimeter);
					} else {
						String perimeterName = layerPerimeter.getPerimeter().getName();
						layerPerimeter.setName(perimeterName);
					}
					modifiedItem = null;
					refresh();
					break;
				}
				case EDITMODE_EXISTING_LAYER_START: {
					EditedLayer editedLayer = (EditedLayer) command.getSource();
					editedLayer.startEditing();
					refresh();
					break;
				}
//				case EDITMODE_EXISTING_LAYER_CANCELED: {
//					EditedLayer editedLayer = (EditedLayer) command.getSource();
//					editedLayer.stopEditing();
//					refresh();
//					break;
//				}
//				case NEW_MISSION_EDITING_STARTED: {
//					LayerMission layerMission = (LayerMission) command.getSource();
//					addSubLayer(layerMission, missionsGroup);
//					refresh();
//					break;
//				}
				case NEW_MISSION_EDITING_CANCELED: {
					LayerMission layerMission = (LayerMission) command.getSource();
					removeLayer(layerMission);
					refresh();
					break;
				}
//				case NEW_PERIMETER_EDITING_STARTED: {
//					LayerPerimeter layerPerimeter = (LayerPerimeter) command.getSource();
//					addSubLayer(layerPerimeter, perimetersGroup);
//					refresh();
//					break;
//				}
				case NEW_PERIMETER_EDITING_CANCELED: {
					LayerPerimeter layerPerimeter = (LayerPerimeter) command.getSource();
					removeLayer(layerPerimeter);
					refresh();
					break;
				}
				case PUBLISH:
			}
		});
	}

	public String dumpTree() {
		return super.dumpTree();
	}

	/**
	 * Dedicated function to find a mission layer with a mission related to the one on the drone
	 * the mission will not be exactly the same:
	 * 1) mission on drone doesn't have a name at this point.
	 * 2) we don't have identifier except coordinates, item amount and types
	 */
	public LayerMission getLayerMissionWithSimilarPropertiesToMission(Mission missionFromDrone) throws MissionComparatorException {
		LOGGER.debug("Searching for '{}'", missionFromDrone.getName());
		DownloadedMissionComparator downloadedMissionComparator = applicationContext.getBean(DownloadedMissionComparator.class);
		Queue<AbstractLayer> treeItemsToSearch = new ConcurrentLinkedQueue<>();
		treeItemsToSearch.add(getRoot().getValue());
		while (!treeItemsToSearch.isEmpty()) {
			AbstractLayer layer = treeItemsToSearch.poll();
			if (layer instanceof LayerGroupEditable) {
				treeItemsToSearch.addAll(((LayerGroupEditable) layer).getChildren());
				continue;
			}

			// An actual layer
			if (layer instanceof LayerMission) {
				LayerMission layerMission = (LayerMission) layer;
				Mission mission = layerMission.getMission();
				LOGGER.debug("Checking equals to '{}'", mission.getName());
				if (downloadedMissionComparator.isEqual(mission, missionFromDrone)) {
					LOGGER.debug("Found identical mission named '{}'", mission.getName());
					return layerMission;
				}
			}
		}

		LOGGER.debug("Layer with mission '{}' wasn't found", missionFromDrone.getName());
		return null;
	}

	@Override
	public void addTreeItemAction(TreeItem<AbstractLayer> newItem, TreeItem<AbstractLayer> parentOfNewItem) {
		super.addTreeItemAction(newItem, parentOfNewItem);
		((LayerManagerDbWrapper) layerManager).create(newItem.getValue());

		applicationEventPublisher.publishEvent(new QuadGuiEvent(PRIVATE_SESSION_STARTED));
	}

	@Override
	public void editTreeItemAction(TreeItem<AbstractLayer> item) {
		super.editTreeItemAction(item);
		AbstractLayer layer = item.getValue();
		EditorHelper editorHelper = helpers.get(layer.getClass());
		if (editorHelper == null) {
			LOGGER.error("Failed to get helper for layer '{}' of type '{}'", layer, layer.getClass().getSimpleName());
			for (Map.Entry<Class<? extends EditedLayer>, EditorHelper> helper : helpers.entrySet()) {
				LOGGER.error("Helper: {} {}", helper.getKey(), helper.getValue());
			}
			System.exit(-2);
		}

		layer = (AbstractLayer) editorHelper.startEditing((EditedLayer) layer);
		((CheckBoxTreeItem) item).selectedProperty().setValue(true);
		modifiedItem = item;
		refresh();
		applicationEventPublisher.publishEvent(new QuadGuiEvent(EDITMODE_EXISTING_LAYER_START, layer));
	}

	@Override
	public void removeTreeItemAction(TreeItem<AbstractLayer> treeItem) {
		try {
			helpers.get(treeItem.getValue().getClass()).removeItem((EditedLayer) treeItem.getValue());
			((LayerManagerDbWrapper) layerManager).delete(treeItem.getValue());
			super.removeTreeItemAction(treeItem);
			applicationEventPublisher.publishEvent(new QuadGuiEvent(PRIVATE_SESSION_STARTED));
		}
		catch (Throwable e) {
			loggerDisplayerSvc.logError(e.getMessage());
		}
	}

}
