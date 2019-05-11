package com.dronegcs.console.controllers.internalFrames.internal.Editors;

import com.db.gui.persistence.scheme.BaseLayer;
import com.db.gui.persistence.scheme.Layer;
import com.db.persistence.remote_exception.ObjectNotFoundRemoteException;
import com.db.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.Mission;
import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerGroupEditable;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerManagerDbWrapper;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.remote_services_wrappers.ObjectCrudSvcRemoteWrapper;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.MissionCompilerSvc;
import com.dronegcs.console_plugin.services.TextNotificationPublisherSvc;
import com.dronegcs.console_plugin.services.internal.convertors.MissionCompilationException;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
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
public class MissionEditorHelper implements EditorHelper<LayerMission> {

	private final static Logger LOGGER = LoggerFactory.getLogger(MissionEditorHelper.class);

	@Autowired
	private Drone drone;

	@Autowired
	private LoggerDisplayerSvc loggerDisplayerSvc;

	@Autowired
	private DialogManagerSvc dialogManagerSvc;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private MissionsManager missionsManager;

	@Autowired
	private MissionCompilerSvc missionCompilerSvc;

	@Autowired
	@NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ObjectCrudSvcRemoteWrapper objectCrudSvcRemoteWrapper;

	@Autowired
	private MissionsManager missionManager;

	private MissionEditor missionEditor;
	private LayerMission modifiedLayerMissionOriginal = null;
	private boolean isBuildMode = false;

	public MissionEditorHelper(@Autowired LayerManagerDbWrapper layerManagerDbWrapper) {

		layerManagerDbWrapper.registerDbLayerFromGuiLayerLoader(LayerMission.class, ((guiLayer, dbLayer) -> {
			LOGGER.debug("Populating DB layer from GUI layer named '{}'", guiLayer.getName());
			LOGGER.debug("Open missing editor by name");
			MissionEditor missionEditor = missionManager.openMissionEditor(guiLayer.getName());
			((Layer) dbLayer).setObjectsUids(Arrays.asList(missionEditor.getMission().getKeyId().getObjId()));
			LOGGER.debug("Updating new dblayer with brand new mission");
//			return objectCrudSvcRemoteWrapper.update(dbLayer);
			return dbLayer;
		}));

		// Sync the helper to react to layer manager loading of gui layer from db layer
		layerManagerDbWrapper.registerGuiLayerFromDbLayerLoader(Layer.class, new LayerManagerDbWrapper.GuiLayer_From_DatabaseLayer_Loader() {
			@Override
			public boolean isRelevant(BaseObject layer) throws ObjectNotFoundRemoteException {
				if (((Layer) layer).getObjectsUids().isEmpty())
					return false;

				String coreObjId = ((Layer) layer).getObjectsUids().iterator().next();
				BaseObject coreObj = objectCrudSvcRemoteWrapper.read(coreObjId);
				if (coreObj instanceof Mission)
					return true;

				return false;
			}

			@Override
			public AbstractLayer load(BaseObject layer) throws ObjectNotFoundRemoteException {
				LayerMission layerGui = new LayerMission(((BaseLayer) layer).getName(), applicationContext.getBean(LayeredViewMap.class));
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
		isBuildMode = buildMode;
	}

	@Override
	public ContextMenu buildTreeViewPopup(OperationalViewTree layerViewTree, OperationalViewMap layerViewMap, TreeItem treeItem) {
		LOGGER.debug("Building TreeView Popup");
		ContextMenu popup = new ContextMenu();

		if (treeItem == null)
			return popup;

		AbstractLayer layer = (AbstractLayer) treeItem.getValue();
		if (!isBuildMode && ( ! (layer instanceof LayerMission))) {
			if (layer instanceof LayerGroupEditable) {
				MenuItem menuItemAddLayer = new MenuItem("Add Mission Layer");
				popup.getItems().add(menuItemAddLayer);

				menuItemAddLayer.setOnAction(handler -> {
					LayerMission layerMission = new LayerMission("LayerMission" + (new Date().getTime()), layerViewMap);
					layerMission.setApplicationContext(applicationContext);
					layerMission.setWasEdited(true);
					TreeItem<AbstractLayer> newChild = layerViewTree.createTreeItem(layerMission);
					layerViewTree.addTreeItemAction(newChild, treeItem);
				});
			}
			return popup;
		}

//		if (layer instanceof LayerMission) {
//		Mission mission = ((LayerMission) layer).getMission();
//		boolean hasMission = mission != null;

		MenuItem menuItemUploadMission = new MenuItem("Upload DroneMission");
		menuItemUploadMission.setOnAction(e -> {
			try {
				LayerMission uploadedLayerMissionCandidate = (LayerMission) layer;
				if (uploadedLayerMissionCandidate.getMission() != null) {
					loggerDisplayerSvc.logOutgoing("Uploading DroneMission To APM");
					LOGGER.debug("Uploading DroneMission To APM");
					DroneMission droneMission = null;
					droneMission = missionCompilerSvc.compile(uploadedLayerMissionCandidate.getMission());
					droneMission.sendMissionToAPM();
					textNotificationPublisherSvc.publish("Uploading DroneMission");
				}
			}
			catch(MissionCompilationException e1){
				dialogManagerSvc.showErrorMessageDialog("Failed to upload mission", e1);
			}
		});

		popup.getItems().add(menuItemUploadMission);

		return popup;
	}

	@Override
	public ContextMenu buildMapViewPopup(OperationalViewMap layerViewMap, Point point) {
		LOGGER.debug("Building MapView Popup");
		ContextMenu popup = new ContextMenu();

		MenuItem menuItemMissionAddWayPoint = new MenuItem("Add Way Point");
		MenuItem menuItemMissionAddLoiterTurns = new MenuItem("Add Loitering Turns");
		MenuItem menuItemMissionAddLoiterTime = new MenuItem("Add Loitering Timeframe");
		MenuItem menuItemMissionAddLoiterUnlimited = new MenuItem("Add Loitering Unlimited");
		MenuItem menuItemMissionAddROI = new MenuItem("Add ROI");
		MenuItem menuItemMissionSetHome = new MenuItem("Set Home");
		MenuItem menuItemMissionSetLandPoint = new MenuItem("Set Land Point");
		MenuItem menuItemMissionSetRTL = new MenuItem("Set RTL");
		MenuItem menuItemMissionSetTakeOff = new MenuItem("Set MavlinkTakeoff");
		MenuItem menuItemSyncMission = new MenuItem("Sync Mission");

		menuItemMissionAddWayPoint.setVisible(isBuildMode);
		menuItemMissionAddLoiterTurns.setVisible(isBuildMode);
		menuItemMissionAddLoiterTime.setVisible(isBuildMode);
		menuItemMissionAddLoiterUnlimited.setVisible(isBuildMode);
		menuItemMissionAddROI.setVisible(isBuildMode);
		menuItemMissionSetHome.setVisible(drone.getGps().isPositionValid() && !isBuildMode);// && !isBuildMode);
		menuItemMissionSetLandPoint.setVisible(isBuildMode);
		menuItemMissionSetRTL.setVisible(isBuildMode);
		menuItemMissionSetTakeOff.setVisible(isBuildMode);
		menuItemSyncMission.setVisible(!layerViewMap.isEditing());// || isBuildMode);

		// Create the popup menu.
		popup.getItems().add(menuItemMissionAddWayPoint);
		popup.getItems().add(menuItemMissionAddLoiterTurns);
		popup.getItems().add(menuItemMissionAddLoiterTime);
		popup.getItems().add(menuItemMissionAddLoiterUnlimited);
		popup.getItems().add(menuItemMissionAddROI);
		popup.getItems().add(menuItemMissionSetLandPoint);
		popup.getItems().add(menuItemMissionSetRTL);
		popup.getItems().add(menuItemMissionSetHome);
		popup.getItems().add(menuItemMissionSetTakeOff);
		//popup.getItems().addSeparator();
		popup.getItems().add(menuItemSyncMission);

		menuItemSyncMission.setOnAction( arg -> {
					LOGGER.debug(getClass().getName() + " Start Sync DroneMission");
					drone.getWaypointManager().getWaypoints();
					loggerDisplayerSvc.logOutgoing("Send Sync Request");
				}
		);

		menuItemMissionAddWayPoint.setOnAction( arg -> {
			try {
				missionEditor.addWaypoint(layerViewMap.getPosition(point));
				modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
				applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, modifiedLayerMissionOriginal));
				modifiedLayerMissionOriginal.regenerateMapObjects();
			}
			catch (MissionUpdateException e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database: " + e.getMessage());
				dialogManagerSvc.showErrorMessageDialog("Waypoint point wasn't added.\n" + e.getMessage(), e);
			}
		});

		menuItemMissionAddLoiterTurns.setOnAction( arg -> {
			try {
				String val = dialogManagerSvc.showInputDialog("Choose turns", "",null, null, "3");
				if (val == null) {
					loggerDisplayerSvc.logGeneral(getClass().getName() + " MavlinkLoiterTurns canceled");
					dialogManagerSvc.showAlertMessageDialog("Turns amount must be defined");
					return;
				}
				int turns = Integer.parseInt((String) val);
				missionEditor.addLoiterTurns(layerViewMap.getPosition(point), turns);
				modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
				applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, modifiedLayerMissionOriginal));
				modifiedLayerMissionOriginal.regenerateMapObjects();
			}
			catch (MissionUpdateException e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database: " + e.getMessage());
				dialogManagerSvc.showErrorMessageDialog("Loiter turns point wasn't added.\n" + e.getMessage(), e);
			}
		});

		menuItemMissionAddLoiterTime.setOnAction( arg -> {
			try {
				String val = dialogManagerSvc.showInputDialog("Set loiter time frame (seconds)", "",null, null, "5");
				if (val == null) {
					loggerDisplayerSvc.logGeneral(getClass().getName() + " MavlinkLoiterTime canceled");
					dialogManagerSvc.showAlertMessageDialog("Loitering time frame is a must");
					return;
				}
				int time = Integer.parseInt((String) val);
				missionEditor.addLoiterTime(layerViewMap.getPosition(point), time);
				modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
				applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, modifiedLayerMissionOriginal));
				modifiedLayerMissionOriginal.regenerateMapObjects();
			}
			catch (MissionUpdateException e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database: " + e.getMessage());
				dialogManagerSvc.showErrorMessageDialog("Loiter point wasn't added.\n" + e.getMessage(), e);
			}
		});

		menuItemMissionAddLoiterUnlimited.setOnAction( arg -> {
			try {
				missionEditor.addLoiterUnlimited(layerViewMap.getPosition(point));
				modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
				applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, modifiedLayerMissionOriginal));
				modifiedLayerMissionOriginal.regenerateMapObjects();
			}
			catch (MissionUpdateException e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database: " + e.getMessage());
				dialogManagerSvc.showErrorMessageDialog("Loiter unlimited point wasn't added.\n" + e.getMessage(), e);
			}
		});

		menuItemMissionSetLandPoint.setOnAction( arg -> {
			try {
				missionEditor.addLandPoint(layerViewMap.getPosition(point));
				modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
				applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, modifiedLayerMissionOriginal));
				modifiedLayerMissionOriginal.regenerateMapObjects();
			}
			catch (MissionUpdateException e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database: " + e.getMessage());
				dialogManagerSvc.showErrorMessageDialog("Land point wasn't added.\n" + e.getMessage(), e);
			}
		});

		menuItemMissionAddROI.setOnAction( arg -> {
			try{
				missionEditor.addRegionOfInterest(layerViewMap.getPosition(point));
				modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
				applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, modifiedLayerMissionOriginal));
				modifiedLayerMissionOriginal.regenerateMapObjects();
			}
			catch (MissionUpdateException e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database: " + e.getMessage());
				dialogManagerSvc.showErrorMessageDialog("ROI point wasn't added.\n" + e.getMessage(), e);
			}
		});

		menuItemMissionSetRTL.setOnAction( arg -> {
			try {
				missionEditor.addReturnToLaunch();
				modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
				applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, modifiedLayerMissionOriginal));
				modifiedLayerMissionOriginal.regenerateMapObjects();
			}
			catch (MissionUpdateException e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database: " + e.getMessage());
				dialogManagerSvc.showErrorMessageDialog("RTL point wasn't added.\n" + e.getMessage(), e);
			}
		});

		menuItemMissionSetTakeOff.setOnAction( arg -> {
			try {
				String val = dialogManagerSvc.showInputDialog("Choose altitude", "",null, null, "5");
				if (val == null) {
					loggerDisplayerSvc.logGeneral(getClass().getName() + " MavlinkTakeoff canceled");
					dialogManagerSvc.showAlertMessageDialog("MavlinkTakeoff must be defined with height");
					return;
				}
				double altitude = Double.parseDouble((String) val);
				missionEditor.addTakeOff(altitude);
				modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
				applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_UPDATED_BY_MAP, modifiedLayerMissionOriginal));
				modifiedLayerMissionOriginal.regenerateMapObjects();
			}
			catch (MissionUpdateException e) {
				loggerDisplayerSvc.logError("Critical Error: failed to update item in database: " + e.getMessage());
				dialogManagerSvc.showErrorMessageDialog("Takeoff point wasn't added.\n" + e.getMessage(), e);
			}
		});

		return popup;
	}

	@Override
	public void saveEditor() {
		modifiedLayerMissionOriginal.setMission(missionEditor.getMission());
		modifiedLayerMissionOriginal.setName(missionEditor.getMission().getName());
		missionEditor = null;
		applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_EDITING_FINISHED, this.modifiedLayerMissionOriginal));

		setModifiedLayerMissionOriginal(null);
		setBuildMode(false);
	}

	@Override
	public void removeItem(LayerMission value) {
		try {
			LOGGER.info("Found mission to remove");
			Layer layer = (Layer) value.getPayload();
			Mission mission = objectCrudSvcRemoteWrapper.readByClass(layer.getObjectsUids().get(0), Mission.class.getCanonicalName());
			missionsManager.delete(mission);
			objectCrudSvcRemoteWrapper.delete(layer);
		}
		catch (Exception e) {
			LOGGER.error("Failed to remove item", e);
		}
	}

	@Override
	public void renameItem(LayerMission value) {
		try {
			Layer layerDb = (Layer) value.getPayload();
			layerDb.setName(value.getName());
			layerDb = objectCrudSvcRemoteWrapper.update(layerDb);

			String missionUid = layerDb.getObjectsUids().get(0);
			Mission mission = objectCrudSvcRemoteWrapper.readByClass(missionUid, Mission.class.getCanonicalName());

			MissionEditor missionEditor = missionManager.openMissionEditor(mission);
			missionEditor.setMissionName(value.getName());

			value.setPayload(layerDb);
		}
		catch (Exception e) {
			LOGGER.error("Failed to rename item", e);
		}
	}

	public LayerMission getModifiedLayerMissionOriginal() {
		return modifiedLayerMissionOriginal;
	}

	public void setModifiedLayerMissionOriginal(LayerMission modifiedLayerMissionOriginal) {
		this.modifiedLayerMissionOriginal = modifiedLayerMissionOriginal;
	}


	@Override
	public LayerMission startEditing(LayerMission layer) {
		try {
			LOGGER.debug("Working on DroneMission Layer");
			modifiedLayerMissionOriginal = (LayerMission) layer;
			modifiedLayerMissionOriginal.setWasEdited(true);
			setBuildMode(true);
			missionEditor = missionsManager.openMissionEditor(((LayerMission) layer).getMission());
			Mission mission = missionEditor.getMission();
			modifiedLayerMissionOriginal.setName(mission.getName());
//						applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_EDITING_STARTED, modifiedLayerMissionOriginal));
			return modifiedLayerMissionOriginal;
		}
		catch (MissionUpdateException e) {
			loggerDisplayerSvc.logError("Critical Error: failed to update item in database, error: " + e.getMessage());
			dialogManagerSvc.showErrorMessageDialog("Failed to update item.\n" + e.getMessage(), e);
			return null;
		}
	}

	@Override
	public int reloadEditors() {
		LOGGER.debug("Reload Editors for {}", this.getClass().getCanonicalName());
		LOGGER.debug("Close all current editors");
		missionManager.closeAllMissionEditors(false);
		LOGGER.debug("Load editors from scratch");
		return missionManager.loadEditors();
	}

	@Override
	public boolean isEdited(AbstractLayer abstractLayer) {
		Mission mission = ((LayerMission) abstractLayer).getMission();
		if (missionManager.getMissionEditor(mission) != null) {
			LOGGER.debug("Modified mission was found: {}", mission);
			return true;
		}
		return false;
	}
}
