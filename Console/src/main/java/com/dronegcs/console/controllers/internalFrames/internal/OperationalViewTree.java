package com.dronegcs.console.controllers.internalFrames.internal;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerCircledPerimeter;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.services.MissionCompilerSvc;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPerimeter;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPolygonPerimeter;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.gui.core.mapTree.CheckBoxViewTree;
import com.gui.core.mapTreeObjects.Layer;
import com.gui.core.mapTreeObjects.LayerGroup;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.TextNotificationPublisherSvc;
import javafx.application.Platform;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnWaypointManagerListener;
import com.dronegcs.mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;
import com.dronegcs.gcsis.validations.RuntimeValidator;
import com.dronegcs.gcsis.validations.ValidatorResponse;

import java.util.List;

import static com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent.QUAD_GUI_COMMAND.EDITMODE_EXISTING_LAYER_START;

@Component
public class OperationalViewTree extends CheckBoxViewTree implements OnWaypointManagerListener {
	
	public static final String UPLOADED_PREFIX = "(CURR) ";
	public static final String EDIT_PREFIX = "*";

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	protected ApplicationContext applicationContext;
	
    @Autowired @NotNull(message = "Internal Error: Missing event publisher")
	protected EventPublisherSvc eventPublisherSvc;
    
	@Autowired @NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.dronegcs.gcsis.logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;

	@Autowired @NotNull(message = "Internal Error: Failed to get mission compiler")
	private MissionCompilerSvc missionCompilerSvc;

	@Autowired @NotNull(message = "Internal Error: Failed to get mission manager")
	private MissionsManager missionsManager;

	@Autowired @NotNull(message = "Internal Error: Failed to get perimeter manager")
	private PerimetersManager perimetersManager;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	private LayerMission uploadedLayerMissionCandidate = null;
	private LayerMission uploadedLayerMission = null;
	private LayerPerimeter uploadedLayerPerimeterCandidate = null;
	private LayerPerimeter uploadedLayerPerimeter = null;
	
	private LayerGroup perimetersGroup;
	private LayerGroup missionsGroup;
	private LayerGroup generalGroup;
	
	public OperationalViewTree() {
		super();
	}

	@Autowired
	public void setOperationalViewMap(OperationalViewMap operationalViewMap) {
		super.setLayeredViewMap(operationalViewMap);
	}


	private static int called;
	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
		
		LayerGroup rootLayer = new LayerGroup("Layers");
		getLayeredViewMap().setRootLayer(rootLayer);
		CheckBoxTreeItem<Layer> rootItem = new CheckBoxTreeItem<Layer> (rootLayer);
		rootItem.setExpanded(true);
		
		perimetersGroup = new LayerGroup("Perimeters");
		missionsGroup = new LayerGroup("Missions");
		generalGroup = new LayerGroup("General");
		rootLayer.addChildren(perimetersGroup);
		rootLayer.addChildren(missionsGroup);
		rootLayer.addChildren(generalGroup);
        CheckBoxTreeItem<Layer> itemGeneral = new CheckBoxTreeItem<Layer> (generalGroup);
        CheckBoxTreeItem<Layer> itemMissions = new CheckBoxTreeItem<Layer> (missionsGroup);            
        CheckBoxTreeItem<Layer> itemPerimeters = new CheckBoxTreeItem<Layer> (perimetersGroup);   
		
        rootItem.getChildren().addAll(itemGeneral, itemMissions, itemPerimeters);        
        setRoot(rootItem);
        
        drone.getWaypointManager().addWaypointManagerListener(this);
       
        addSelectionHandler(getRoot());

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());

		System.err.println("Loading DataBase");
		try {
			List<BaseObject> missionsList = missionsManager.getAllMissions();
			LayerMission layerMission;
			for (BaseObject mission : missionsList) {
				System.err.println("Loading existing mission: " + mission);
				layerMission = new LayerMission(((Mission) mission), getLayeredViewMap());
				layerMission.setApplicationContext(applicationContext);
				addLayer(layerMission);
			}

			List<BaseObject> polygonPerimeter = perimetersManager.getAllPerimeters();
			LayerPerimeter layerPerimeter;
			for (BaseObject perimeter : polygonPerimeter) {
				System.err.println("Loading existing perimeter: " + perimeter);
				layerPerimeter = LayerPerimeter.generateNew(perimeter, getLayeredViewMap());
				if (layerPerimeter == null) {
					loggerDisplayerSvc.logError("Failed to get perimeter");
					continue;
				}
				layerPerimeter.setApplicationContext(applicationContext);
				addLayer(layerPerimeter);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void addSelectionHandler(TreeItem<Layer> cbox) {
		cbox.addEventHandler(CheckBoxTreeItem.<Layer>checkBoxSelectionChangedEvent(),
				(event) -> {
					CheckBoxTreeItem<Layer> cbItem = (CheckBoxTreeItem<Layer>) event.getTreeItem();
					System.out.println("Selected " + cbItem.isSelected());
					if (!cbItem.isIndeterminate())
						getLayeredViewMap().setLayerVisibie(cbItem.getValue(), cbItem.isSelected());
				}
		);
	}

	public void addLayer(Layer layer) {
		CheckBoxTreeItem<Layer> ti = null;
		if (layer instanceof LayerPerimeter) {
			ti = addTreeNode(layer, perimetersGroup);
			getLayeredViewMap().addLayer(layer, perimetersGroup);
		}
		
		else if (layer instanceof LayerMission) {
			ti = addTreeNode(layer, missionsGroup);
			getLayeredViewMap().addLayer(layer, missionsGroup);
		}
		else
			getLayeredViewMap().addLayer(layer, generalGroup);

		ti.selectedProperty().addListener((obs, oldVal, newVal) -> {
			System.out.println(" selection state: " + newVal);
		});
		System.out.println("SIGN");
		addSelectionHandler(ti);
	}

	public void removeLayer(Layer layer) {
		removeFromTreeGroup(layer);
		getLayeredViewMap().removeLayer(layer);
	}

	@Override
	public void updateTreeItemName(TreeItem<Layer> treeItem) {
		if (treeItem.getValue() instanceof LayerMission) {
			System.out.println("Found mission to update it name");
			Mission mission = ((LayerMission) treeItem.getValue()).getMission();
			mission.setName(treeItem.getValue().getName());

			((LayerMission) treeItem.getValue()).setMission(missionsManager.update(mission));
		}

		if (treeItem.getValue() instanceof LayerPolygonPerimeter) {
			System.out.println("Found polygon perimeter to update it name");
			PolygonPerimeter polygonPerimeter = ((LayerPolygonPerimeter) treeItem.getValue()).getPolygonPerimeter();
			polygonPerimeter.setName(treeItem.getValue().getName());

			((LayerPolygonPerimeter) treeItem.getValue()).setPerimeter(perimetersManager.update(polygonPerimeter));
		}

		if (treeItem.getValue() instanceof LayerCircledPerimeter) {
			System.out.println("Found circle perimeter to update it name");
			CirclePerimeter circlePerimeter = ((LayerCircledPerimeter) treeItem.getValue()).getCirclePerimeter();
			circlePerimeter.setName(treeItem.getValue().getName());

			((LayerCircledPerimeter) treeItem.getValue()).setPerimeter(perimetersManager.update(circlePerimeter));
		}
	}

	@Override
	public void handleRemoveTreeItem(TreeItem<Layer> treeItem) {
		if (treeItem.getValue() instanceof LayerMission) {
			System.out.println("Found mission to remove");
			missionsManager.delete(((LayerMission) treeItem.getValue()).getMission());
		}
		if (treeItem.getValue() instanceof LayerPolygonPerimeter) {
			System.out.println("Found perimeter to remove");
			perimetersManager.delete(((LayerPolygonPerimeter) treeItem.getValue()).getPolygonPerimeter());
		}

		if (treeItem.getValue() instanceof LayerCircledPerimeter) {
			System.out.println("Found circle to remove");
			perimetersManager.delete(((LayerCircledPerimeter) treeItem.getValue()).getCirclePerimeter());
		}
		super.handleRemoveTreeItem(treeItem);

	}

	@Override
	public ContextMenu getPopupMenu(TreeItem<Layer> treeItem) {
		ContextMenu popup = super.getPopupMenu(treeItem);
		if (!(treeItem.getValue() instanceof Layer))
			return popup;
		
		ContextMenu operationalPopup = new ContextMenu();
		
		Layer layer = treeItem.getValue();
		
		MenuItem menuItemUploadMission = new MenuItem("Upload DroneMission");
		MenuItem menuItemEditMission = new MenuItem("Edit DroneMission");
		MenuItem menuItemUploadPerimeter = new MenuItem("Upload Perimeter");
		
		menuItemUploadMission.setOnAction( e -> {
			if (layer instanceof LayerMission) {
				uploadedLayerMissionCandidate = (LayerMission) layer;
				if (uploadedLayerMissionCandidate.getMission() != null) {
					loggerDisplayerSvc.logOutgoing("Uploading DroneMission To APM");
					DroneMission droneMission = missionCompilerSvc.compile(uploadedLayerMissionCandidate.getMission());
					droneMission.sendMissionToAPM();
					textNotificationPublisherSvc.publish("Uploading DroneMission");
				}
			}
		});
		
		menuItemEditMission.setOnAction( e -> {
			if (layer instanceof LayerMission) {
				LayerMission layerMission = (LayerMission) layer;
				layer.setName(EDIT_PREFIX + ((LayerMission) layer).getMission().getName());
				refresh();
				eventPublisherSvc.publish(new QuadGuiEvent(EDITMODE_EXISTING_LAYER_START, layerMission));
			}
		});

		menuItemUploadPerimeter.setOnAction( e -> {
			if (layer instanceof LayerPerimeter) {
				uploadedLayerPerimeterCandidate = (LayerPolygonPerimeter) layer;
				if (((LayerPolygonPerimeter) uploadedLayerPerimeterCandidate).getPolygon() != null) {
					loggerDisplayerSvc.logOutgoing("Uploading Perimeter To APM");
					drone.getPerimeter().setCompound(uploadedLayerPerimeterCandidate);
					uploadedLayerPerimeter = (LayerPerimeter) switchCurrentLayer(perimetersGroup, uploadedLayerPerimeter, uploadedLayerPerimeterCandidate);
					uploadedLayerPerimeterCandidate = null;
					textNotificationPublisherSvc.publish("Perimeter Uploaded Successfully");
				}
			}
		});        

		if (layer instanceof LayerPerimeter)
			operationalPopup.getItems().add(menuItemUploadPerimeter);
		
		if (layer instanceof LayerMission) {
			operationalPopup.getItems().add(menuItemEditMission);
			operationalPopup.getItems().add(menuItemUploadMission);
		}
		
		operationalPopup.getItems().addAll(popup.getItems());
		return operationalPopup;
	}

	@Override
	public void handleTreeItemClick(TreeItem<Layer> node) {
		Layer layer = node.getValue();
		if (layer instanceof LayerMission) {
			if (missionsManager.getMissionEditor(((LayerMission) layer).getMission()) == null)
				eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_VIEW_ONLY_STARTED, layer));
			else
				eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_EDITING_STARTED, layer));
		}
		else 
			eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_VIEW_ONLY_FINISHED, layer));
	}

	public Layer switchCurrentLayer(LayerGroup layerGroup, Layer fromLayer, Layer toLayer) {
		Layer finalLayer = null;

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
			//addLayer(finalLayer);
			finalLayer.regenerateMapObjects();
			loggerDisplayerSvc.logGeneral("A new layer was created for current droneMission");
		}

		CurrentPrefixAdd(finalLayer);
		return finalLayer;
	}
	
	private void CurrentPrefixRemove(Layer old_layer) {
		if (old_layer.getName().contains(UPLOADED_PREFIX))
			old_layer.setName(old_layer.getName().substring(UPLOADED_PREFIX.length(), old_layer.getName().length()));
		
		refresh();
	}

	private void CurrentPrefixAdd(Layer finalLayer) {
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
			loggerDisplayerSvc.logIncoming("Start Updloading Waypoints");
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
	
				LayerMission lm = applicationContext.getBean(LayerMission.class);
				lm.setName("UnnamedMission");
				Mission mission = missionCompilerSvc.decompile(drone.getDroneMission());
				lm.setMission(mission);
				addLayer(lm);
				uploadedLayerMission = (LayerMission) switchCurrentLayer(missionsGroup, uploadedLayerMission, lm);
	
				loggerDisplayerSvc.logGeneral("DroneMission was updated in droneMission tree");
				textNotificationPublisherSvc.publish("DroneMission successfully downloaded");
				return;
			}
	
			if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
				loggerDisplayerSvc.logIncoming("Waypoints uploaded");
				if (drone.getDroneMission() == null) {
					loggerDisplayerSvc.logError("Failed to find droneMission");
					return;
				}
				uploadedLayerMission = (LayerMission) switchCurrentLayer(missionsGroup, uploadedLayerMission, uploadedLayerMissionCandidate);
				uploadedLayerMissionCandidate = null;
				loggerDisplayerSvc.logGeneral("DroneMission was updated in droneMission tree");
				textNotificationPublisherSvc.publish("DroneMission successfully uploaded");
				return;
			}
			
			loggerDisplayerSvc.logError("Failed to Sync Waypoints (" + wpEvent.name() + ")");
			textNotificationPublisherSvc.publish("DroneMission Sync failed");
		});
	}

	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
		Platform.runLater( () -> {
			switch (command.getCommand()) {
				case MISSION_EDITING_FINISHED:
					LayerMission layerMission = (LayerMission) command.getSource();
					String missionName = layerMission.getMission().getName();
					if (missionName.startsWith(EDIT_PREFIX))
						missionName = missionName.substring(1, missionName.length());
					layerMission.setName(missionName);
					refresh();
					break;
				case LAYER_PERIMETER_EDITING_FINISHED:
					LayerPerimeter layerPerimeter = (LayerPerimeter) command.getSource();
					String perimeterName = layerPerimeter.getPerimeter().getName();
					if (perimeterName.startsWith(EDIT_PREFIX))
						perimeterName = perimeterName.substring(1, perimeterName.length());
					layerPerimeter.setName(perimeterName);
					refresh();
					break;
			}
		});
	}
}
