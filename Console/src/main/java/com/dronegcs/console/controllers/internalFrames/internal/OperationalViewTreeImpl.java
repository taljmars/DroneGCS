package com.dronegcs.console.controllers.internalFrames.internal;

import com.dronedb.persistence.scheme.*;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.*;
import com.dronegcs.console_plugin.mission_editor.MissionEditor;
import com.dronegcs.console_plugin.mission_editor.MissionUpdateException;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterEditor;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.services.*;
import com.dronegcs.console_plugin.services.internal.convertors.MissionCompilationException;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnWaypointManagerListener;
import com.dronegcs.mavlink.is.drone.mission.DroneMission;
import com.dronegcs.mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.gui.core.mapTree.CheckBoxViewTree;
import com.gui.core.mapTreeObjects.Layer;
import com.gui.core.mapTreeObjects.LayerGroup;
import javafx.application.Platform;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected EventPublisherSvc eventPublisherSvc;

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
    @NotNull(message = "Internal Error: Failed to get drone")
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

    private TreeItem modifiedItem = null;
    private boolean activePrivateSession = false;

    public OperationalViewTreeImpl() {
        super(new EditableLayeredCheckBoxTreeCellEditorConvertor());
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
        CheckBoxTreeItem<Layer> rootItem = new CheckBoxTreeItem<Layer>(rootLayer);
        rootItem.setExpanded(true);

        perimetersGroup = new LayerGroup("Perimeters");
        missionsGroup = new LayerGroup("Missions");
        generalGroup = new LayerGroup("General");
        rootLayer.addChildren(perimetersGroup);
        rootLayer.addChildren(missionsGroup);
        rootLayer.addChildren(generalGroup);
        CheckBoxTreeItem<Layer> itemGeneral = new CheckBoxTreeItem<Layer>(generalGroup);
        CheckBoxTreeItem<Layer> itemMissions = new CheckBoxTreeItem<Layer>(missionsGroup);
        CheckBoxTreeItem<Layer> itemPerimeters = new CheckBoxTreeItem<Layer>(perimetersGroup);

        rootItem.getChildren().addAll(itemGeneral, itemMissions, itemPerimeters);
        setRoot(rootItem);

        drone.getWaypointManager().addWaypointManagerListener(this);

        addSelectionHandler(getRoot());

        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        LOGGER.info("Load Database data");
        try {
            // Missions
            List<BaseObject> modifiedMissionsList = missionsManager.getAllModifiedMissions();
            LOGGER.debug("Found {} modified missions", modifiedMissionsList.size());
            if (!modifiedMissionsList.isEmpty()) {
                LOGGER.debug("Activate private session");
                activePrivateSession = true;
            }
            for (BaseObject obj : modifiedMissionsList) {
                Mission mission = (Mission) obj;
                LOGGER.debug("Modify mission: {}", mission.getKeyId().getObjId());
            }
            List<BaseObject> missionsList = missionsManager.getAllMissions();
            LayerMission layerMission;
            for (BaseObject mission : missionsList) {
                boolean isEditing = modifiedMissionsList.stream().anyMatch(
                        (BaseObject baseObject) -> baseObject.getKeyId().getObjId().equals(mission.getKeyId().getObjId())
                );
                LOGGER.debug("Loading existing mission: {} , edit mode: {}", mission, isEditing);
                layerMission = new LayerMission(((Mission) mission), getLayeredViewMap(), isEditing);
                layerMission.setApplicationContext(applicationContext);
                addLayer(layerMission);
            }

            // Polyline Perimeter
            List<BaseObject> modifiedPerimeterList = perimetersManager.getAllModifiedPerimeters();
            LOGGER.debug("Found {} modified perimeters", modifiedPerimeterList.size());
            if (!modifiedPerimeterList.isEmpty()) {
                LOGGER.debug("Activate private session");
                activePrivateSession = true;
            }
            for (BaseObject obj : modifiedPerimeterList) {
                Perimeter perimeter = (Perimeter) obj;
                LOGGER.debug("Modify perimeter: {}", perimeter.getKeyId().getObjId());
            }
            List<BaseObject> perimeterList = perimetersManager.getAllPerimeters();
            LayerPerimeter layerPerimeter;
            for (BaseObject perimeter : perimeterList) {
                boolean isEditing = modifiedPerimeterList.stream().anyMatch(
                        (BaseObject baseObject) -> baseObject.getKeyId().getObjId().equals(perimeter.getKeyId().getObjId())
                );
                LOGGER.debug("Loading existing perimeter: {} , edit mode: {}", perimeter, isEditing);
                if (perimeter instanceof PolygonPerimeter)
                    layerPerimeter = new LayerPolygonPerimeter((PolygonPerimeter) perimeter, getLayeredViewMap(), isEditing);
                else
                    layerPerimeter = new LayerCircledPerimeter((CirclePerimeter) perimeter, getLayeredViewMap(), isEditing);
                layerPerimeter.setApplicationContext(applicationContext);
                addLayer(layerPerimeter);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error occur when loading DB", e);
        }
    }

    private void addSelectionHandler(TreeItem<Layer> cbox) {
        cbox.addEventHandler(CheckBoxTreeItem.<Layer>checkBoxSelectionChangedEvent(),
                (event) -> {
                    CheckBoxTreeItem<Layer> cbItem = event.getTreeItem();
                    LOGGER.info("Selected {}", cbItem.isSelected());
                    if (!cbItem.isIndeterminate())
                        getLayeredViewMap().setLayerVisibie(cbItem.getValue(), cbItem.isSelected());
                }
        );
    }

    @Override
    public void addLayer(Layer layer) {
        CheckBoxTreeItem<Layer> ti = null;
        if (layer instanceof LayerPerimeter) {
            ti = addTreeNode(layer, perimetersGroup);
            getLayeredViewMap().addLayer(layer, perimetersGroup);
        } else if (layer instanceof LayerMission) {
            ti = addTreeNode(layer, missionsGroup);
            getLayeredViewMap().addLayer(layer, missionsGroup);
        } else
            getLayeredViewMap().addLayer(layer, generalGroup);

        ti.selectedProperty().addListener((obs, oldVal, newVal) -> {
            LOGGER.info("Selection state: {} ", newVal);
        });
        addSelectionHandler(ti);
    }

    public void removeLayer(Layer layer) {
        removeFromTreeGroup(layer);
        getLayeredViewMap().removeLayer(layer);
    }

    @Override
    public void updateTreeItemName(String fromText, TreeItem<Layer> treeItem) {
        if (treeItem.getValue() instanceof LayerMission) {
            LayerMission layerMission = (LayerMission) treeItem.getValue();
            LOGGER.info("Found mission to update it name");
            String newName = layerMission.getName(true);
            Mission mission = layerMission.getMission();
            try {
                MissionEditor missionEditor = missionsManager.openMissionEditor(mission);
                mission.setName(newName);
                missionEditor.update(mission);
                layerMission.setMission(mission);
                eventPublisherSvc.publish(new QuadGuiEvent(PRIVATE_SESSION_STARTED));
            } catch (MissionUpdateException e) {
                layerMission.setName(fromText);
                loggerDisplayerSvc.logError("Database is out of sync, failed to update mission name , error: " + e.getMessage());
            }
            refresh();
            LOGGER.debug("Mission manager status: \n" + missionsManager.toString());
        }

        if (treeItem.getValue() instanceof LayerPolygonPerimeter) {
            LayerPolygonPerimeter layerPolygonPerimeter = ((LayerPolygonPerimeter) treeItem.getValue());
            LOGGER.info("Found polygon perimeter to update it name");
            String newName = layerPolygonPerimeter.getName(true);
            Perimeter perimeter = layerPolygonPerimeter.getPerimeter();
            try {
                PerimeterEditor perimeterEditor = perimetersManager.openPerimeterEditor(perimeter);
                perimeter.setName(newName);
                perimeterEditor.update(perimeter);
                layerPolygonPerimeter.setPerimeter(perimeter);
                eventPublisherSvc.publish(new QuadGuiEvent(PRIVATE_SESSION_STARTED));
            } catch (PerimeterUpdateException e) {
                layerPolygonPerimeter.setName(fromText);
                loggerDisplayerSvc.logError("Database is out of sync, failed to update perimeter name , error: " + e.getMessage());
            }
            refresh();
            LOGGER.debug("Perimeter manager status: \n" + perimetersManager.toString());
        }

        if (treeItem.getValue() instanceof LayerCircledPerimeter) {
            throw new RuntimeException("Yet to implemets");
        }
    }

    @Override
    public void handleRemoveTreeItem(TreeItem<Layer> treeItem) {
        try {
            if (treeItem.getValue() instanceof LayerMission) {
                LOGGER.info("Found mission to remove");
                missionsManager.delete(((LayerMission) treeItem.getValue()).getMission());
            }
            if (treeItem.getValue() instanceof LayerPolygonPerimeter) {
                LOGGER.info("Found perimeter to remove");
                perimetersManager.delete(((LayerPolygonPerimeter) treeItem.getValue()).getPolygonPerimeter());
            }

            if (treeItem.getValue() instanceof LayerCircledPerimeter) {
                LOGGER.info("Found circle to remove");
                perimetersManager.delete(((LayerCircledPerimeter) treeItem.getValue()).getCirclePerimeter());
            }
            super.handleRemoveTreeItem(treeItem);

            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PRIVATE_SESSION_STARTED));
        } catch (PerimeterUpdateException e) {
            loggerDisplayerSvc.logError(e.getMessage());
        }
    }

    @Override
    public ContextMenu getPopupMenu(TreeItem<Layer> treeItem) {
        ContextMenu popup = super.getPopupMenu(treeItem);
        if (!(treeItem.getValue() instanceof Layer))
            return popup;

//		// Check if we are in editing mode
        if (treeItem.equals(modifiedItem)) {
            LOGGER.debug("Same layer , limit popups");
            return popup;
        }

        ContextMenu operationalPopup = new ContextMenu();

        Layer layer = treeItem.getValue();

        MenuItem menuItemUploadMission = new MenuItem("Upload DroneMission");
        MenuItem menuItemEditMission = new MenuItem("Edit DroneMission");
        MenuItem menuItemUploadPerimeter = new MenuItem("Upload Perimeter");

        menuItemUploadMission.setOnAction(e -> {
            if (layer instanceof LayerMission) {
                uploadedLayerMissionCandidate = (LayerMission) layer;
                if (uploadedLayerMissionCandidate.getMission() != null) {
                    loggerDisplayerSvc.logOutgoing("Uploading DroneMission To APM");
                    LOGGER.debug("Uploading DroneMission To APM");
                    DroneMission droneMission = missionCompilerSvc.compile(uploadedLayerMissionCandidate.getMission());
                    droneMission.sendMissionToAPM();
                    textNotificationPublisherSvc.publish("Uploading DroneMission");
                }
            }
        });

        menuItemEditMission.setOnAction(e -> {
            if (layer instanceof LayerMission) {
                ((CheckBoxTreeItem) treeItem).selectedProperty().setValue(true);
                modifiedItem = treeItem;
                LayerMission layerMission = (LayerMission) layer;
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
			if (modifiedItem == null)
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
        if (layer instanceof LayerPerimeter) {
            if (perimetersManager.getPerimeterEditor(((LayerPerimeter) layer).getPerimeter()) == null)
                eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PERIMETER_VIEW_ONLY_STARTED, layer));
            else
                eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PERIMETER_EDITING_STARTED, layer));
        }
//		else
//			eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.MISSION_VIEW_ONLY_FINISHED, layer));
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
                    LayerMission lm = getLayerMissionWithSimilarPropertiesToMission(mission);
                    if (lm == null) {
                        LOGGER.debug("Creating layer for the newly created mission");
                        lm = new LayerMission(mission, getLayeredViewMap(), true);
                        lm.setApplicationContext(applicationContext);
                        addLayer(lm);
                    }
                    else {
                        LOGGER.debug("Found layer with the same mission named '{}'", lm.getName());
                        LOGGER.debug("Clearing autogenerated mission");
                        MissionEditor missionEditor = missionsManager.getMissionEditor(mission);
                        missionEditor.delete();
                    }

                    uploadedLayerMission = (LayerMission) switchCurrentLayer(missionsGroup, uploadedLayerMission, lm);

                    loggerDisplayerSvc.logGeneral("DroneMission was updated in droneMission tree");
                    textNotificationPublisherSvc.publish("DroneMission successfully downloaded");
				}
				catch (MissionCompilationException e) {
					loggerDisplayerSvc.logError("Failed to decompile mission");
					return;
				}
				catch (MissionUpdateException e) {
                    loggerDisplayerSvc.logError("Failed to decompile mission");
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

	public void regenerateTree() {
		System.err.println("Regenerate Tree");

		Platform.runLater(() -> {
			System.err.println("Mission group " + missionsGroup.getChildens());
			for (Layer layer : missionsGroup.getChildens()) {
				if (layer instanceof LayerMission) {
					System.err.println("Name " + layer.getName() + " " + ((LayerMission) layer).isEdited());
					LayerMission layerMission = (LayerMission) layer;
					if (!layerMission.isEdited())
						continue;

					CheckBoxTreeItem checkBoxTreeItem = findCheckBoxTreeItemByLayer(layerMission);
					CheckBoxTreeItem parent = (CheckBoxTreeItem) checkBoxTreeItem.getParent();
					parent.getChildren().remove(parent);
                }
            }

            System.err.println("Perimeters group " + perimetersGroup.getChildens());
            for (Layer layer : perimetersGroup.getChildens()) {
                if (layer instanceof LayerPerimeter) {
                    System.err.println("Name " + layer.getName() + " " + ((LayerPerimeter) layer).isEdited());
                    LayerPerimeter layerPerimeter = (LayerPerimeter) layer;
                    if (!layerPerimeter.isEdited())
                        continue;

                    CheckBoxTreeItem checkBoxTreeItem = findCheckBoxTreeItemByLayer(layerPerimeter);
                    CheckBoxTreeItem parent = (CheckBoxTreeItem) checkBoxTreeItem.getParent();
                    parent.getChildren().remove(parent);
                }
            }

            refresh();
        });
    }

    @Override
    public CheckBoxViewTree getTree() {
        return this;
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
                case MISSION_EDITING_FINISHED:
                    LayerMission layerMission = (LayerMission) command.getSource();
                    if (layerMission.getMission() == null) {
                        //Means we were left without a mission, we need to clear the item
                        removeLayer(layerMission);
                    } else {
                        String missionName = layerMission.getMission().getName();
                        layerMission.setName(missionName);
                    }
                    modifiedItem = null;
                    refresh();
                    break;
                case PERIMETER_EDITING_FINISHED:
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
                case EDITMODE_EXISTING_LAYER_START: {
                    EditedLayer editedLayer = (EditedLayer) command.getSource();
                    editedLayer.startEditing();
                    refresh();
                    break;
                }
                case EDITMODE_EXISTING_LAYER_CANCELED: {
                    EditedLayer editedLayer = (EditedLayer) command.getSource();
                    editedLayer.stopEditing();
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

    public void removeItemByName(String name) {
        Layer foundLayer = getLayerByName(name);
        if (foundLayer != null)
            removeLayer(foundLayer);
    }

    //TODO: Make it search deep
    @Override
    public Layer getLayerByName(String name) {
        Layer foundLayer = null;
        for (Layer layer : missionsGroup.getChildens()) {
            LayerMission layerMission = (LayerMission) layer;
            if (layer.getName().equals(name))
                foundLayer = layerMission;
        }

        for (Layer layer : perimetersGroup.getChildens()) {
            LayerPerimeter layerPerimeter = (LayerPerimeter) layer;
            if (layerPerimeter.getName().equals(name))
                foundLayer = layerPerimeter;
        }

        if (foundLayer == null)
            return null;

        return foundLayer;
    }

    //TODO: Make it search deep
    @Override
    public Layer getLayerByValue(BaseObject object) {
        LOGGER.debug("Searching for '" + object + "'");
        Layer foundLayer = null;
        for (Layer layer : missionsGroup.getChildens()) {
            LayerMission layerMission = (LayerMission) layer;
            LOGGER.debug("Candidate -" + layerMission.getName() + " " + layerMission.getMission().getName() + " mission: " +
                    layerMission.getMission() +
                    " " + layerMission.getMission().getKeyId().getObjId() +
                    " " + layerMission.getMission().getName() +
                    " " + layerMission.getMission().getMissionItemsUids() +
                    " " + layerMission.getMission().getDefaultAlt());
            if (((LayerMission) layer).getMission().getKeyId().getObjId().equals(object.getKeyId().getObjId()))
                foundLayer = layerMission;
        }

        if (foundLayer == null)
            return null;

        return foundLayer;
    }

    /**
     * Dedicated function to find a mission layer with a mission related to the one on the drone
     * the mission will not be exacly the same:
     * 1) mission on drone doesn't have a name at this point.
     * 2) we don't have identifier except coordinates, item amount and types
     */
    public LayerMission getLayerMissionWithSimilarPropertiesToMission(Mission missionFromDrone) {
        LOGGER.debug("Searching for '{}'", missionFromDrone.getName());
        DownloadedMissionComparator downloadedMissionComparator = applicationContext.getBean(DownloadedMissionComparator.class);
        Layer foundLayer = null;
        for (Layer layer : missionsGroup.getChildens()) {
            LayerMission layerMission = (LayerMission) layer;
            Mission mission = layerMission.getMission();
            LOGGER.debug("Checking equals to '{}", mission.getName());
            if (downloadedMissionComparator.isEqual(mission, missionFromDrone)) {
                LOGGER.debug("Found identical mission named '{}'", mission.getName());
                return layerMission;
            }
        }
        LOGGER.debug("Layer with mission '{}' wasn't found", missionFromDrone.getName());
        return null;
    }
}
