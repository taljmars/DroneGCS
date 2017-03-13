package com.dronegcs.console.controllers.internalFrames.internal;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPerimeter;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPolygonPerimeter;
import com.dronegcs.console.services.internal.QuadGuiEvent;
import com.dronegcs.console.validations.LegalTreeView;
import gui.core.mapTree.CheckBoxViewTree;
import gui.core.mapTreeObjects.Layer;
import gui.core.mapTreeObjects.LayerGroup;
import gui.is.events.GuiEvent;
import gui.is.events.GuiEvent.MAPVIEWER_GUI_COMMAND;
import com.dronegcs.console.services.EventPublisherSvc;
import com.dronegcs.console.services.LoggerDisplayerSvc;
import com.dronegcs.console.services.TextNotificationPublisherSvc;
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

@LegalTreeView
@Component
public class OperationalViewTree extends CheckBoxViewTree implements OnWaypointManagerListener {
	
	public static final String UPLOADED_PREFIX = "(CURR) ";
	public static final String EDIT_SUFFIX = "*";

	@Autowired @NotNull(message = "Internal Error: Failed to get application context")
	protected ApplicationContext applicationContext;
	
    @Autowired @NotNull(message = "Internal Error: Missing event publisher")
	protected EventPublisherSvc eventPublisherSvc;
    
	@Autowired @NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.dronegcs.gcsis.logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
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
	}

	private void addSelectionHandler(TreeItem<Layer> cbox) {
		cbox.addEventHandler(CheckBoxTreeItem.<Layer>checkBoxSelectionChangedEvent(),
				(event) -> {
					CheckBoxTreeItem<Layer> cbItem = (CheckBoxTreeItem<Layer>) event.getTreeItem();
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
		
		addSelectionHandler(ti);
	}

	public void removeLayer(Layer layer) {
		removeFromTreeGroup(layer);
		getLayeredViewMap().removeLayer(layer);
	}

	@Override
	public ContextMenu getPopupMenu(TreeItem<Layer> treeItem) {
		ContextMenu popup = super.getPopupMenu(treeItem);
		if (!(treeItem.getValue() instanceof Layer))
			return popup;
		
		ContextMenu operationalPopup = new ContextMenu();
		
		Layer layer = treeItem.getValue();
		
		MenuItem menuItemUploadMission = new MenuItem("Upload Mission");
		MenuItem menuItemEditMission = new MenuItem("Edit Mission");
		MenuItem menuItemUploadPerimeter = new MenuItem("Upload Perimeter");
		
		menuItemUploadMission.setOnAction( e -> {
			if (layer instanceof LayerMission) {
				uploadedLayerMissionCandidate = (LayerMission) layer;
				if (uploadedLayerMissionCandidate.getMission() != null) {
					loggerDisplayerSvc.logOutgoing("Uploading Mission To APM");
					uploadedLayerMissionCandidate.getMission().sendMissionToAPM();
					textNotificationPublisherSvc.publish("Uploading Mission");
				}
			}
		});
		
		menuItemEditMission.setOnAction( e -> {
			if (layer instanceof LayerMission) {
				LayerMission layerMission = (LayerMission) layer;
				eventPublisherSvc.publish(new GuiEvent(MAPVIEWER_GUI_COMMAND.EDITMODE_EXISTING_LAYER_START, layerMission));
			}
		});

		menuItemUploadPerimeter.setOnAction( e -> {
			if (layer instanceof LayerPerimeter) {
				uploadedLayerPerimeterCandidate = (LayerPolygonPerimeter) layer;
				if (((LayerPolygonPerimeter) uploadedLayerPerimeterCandidate).getPerimeter() != null) {
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
		if (layer instanceof LayerMission)
			eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_VIEW_ONLY_STARTED, layer));
		else 
			eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_VIEW_ONLY_FINISHED, layer));
	}

	public Layer switchCurrentLayer(LayerGroup layerGroup, Layer fromLayer, Layer toLayer) {
		Layer finalLayer = null;

		if (toLayer.equals(fromLayer)) {
			loggerDisplayerSvc.logGeneral("Current mission layer is updated");
			return finalLayer;
		}

		finalLayer = toLayer;

		if (fromLayer != null) {
			// Means the GUI is updated with old uploaded mission
			CurrentPrefixRemove(fromLayer);
			loggerDisplayerSvc.logGeneral("Previous mission prefix was removed");
		}
		else {
			// Means we are not aware of any uploaded mission
			//addLayer(finalLayer);
			finalLayer.regenerateMapObjects();
			loggerDisplayerSvc.logGeneral("A new layer was created for current mission");
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
		textNotificationPublisherSvc.publish("Mission Sync failed");
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Downloading Waypoint " + index + "/" + count);
			return;
		}

		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Uploading Waypoint " + index + "/" + count);
			return;
		}

		loggerDisplayerSvc.logError("Unexpected Syncing Failure (" + wpEvent.name() + ")");
		textNotificationPublisherSvc.publish("Mission Sync failed");
	}

	@Override
	public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
		Platform.runLater( () -> { 
			if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
				loggerDisplayerSvc.logIncoming("Waypoints downloaded");
				if (drone.getMission() == null) {
					loggerDisplayerSvc.logError("Failed to find mission");
					return;
				}
	
				LayerMission lm = applicationContext.getBean(LayerMission.class);
				lm.setName("UnnamedMission");
				lm.setMission(drone.getMission());
				addLayer(lm);
				uploadedLayerMission = (LayerMission) switchCurrentLayer(missionsGroup, uploadedLayerMission, lm);
	
				loggerDisplayerSvc.logGeneral("Mission was updated in mission tree");
				textNotificationPublisherSvc.publish("Mission successfully downloaded");
				return;
			}
	
			if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
				loggerDisplayerSvc.logIncoming("Waypoints uploaded");
				if (drone.getMission() == null) {
					loggerDisplayerSvc.logError("Failed to find mission");
					return;
				}
				uploadedLayerMission = (LayerMission) switchCurrentLayer(missionsGroup, uploadedLayerMission, uploadedLayerMissionCandidate);
				uploadedLayerMissionCandidate = null;
				loggerDisplayerSvc.logGeneral("Mission was updated in mission tree");
				textNotificationPublisherSvc.publish("Mission successfully uploaded");
				return;
			}
			
			loggerDisplayerSvc.logError("Failed to Sync Waypoints (" + wpEvent.name() + ")");
			textNotificationPublisherSvc.publish("Mission Sync failed");
		});
	}
}