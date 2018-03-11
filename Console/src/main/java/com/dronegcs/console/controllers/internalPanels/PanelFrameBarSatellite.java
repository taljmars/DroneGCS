package com.dronegcs.console.controllers.internalPanels;

import com.dronedb.persistence.scheme.CirclePerimeter;
import com.dronedb.persistence.scheme.Mission;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronedb.persistence.scheme.PolygonPerimeter;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerCircledPerimeter;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPerimeter;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerPolygonPerimeter;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.perimeter_editor.PerimeterUpdateException;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.remote_services_wrappers.SessionsSvcRemoteWrapper;
import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console_plugin.services.EventPublisherSvc;
import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.gui.core.mapTreeObjects.Layer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

@Component
public class PanelFrameBarSatellite extends FlowPane implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(PanelFrameBarSatellite.class);
    private static String INTERNAL_FRAME_PATH = "/com/dronegcs/console/views/internalFrames/";

    @NotNull
    @FXML
    private Button btnMap;
    private static String MAP_VIEW = "InternalFrameMapAndTreeView.fxml";

    @NotNull
    @FXML
    private Button btnActualPWM;
    private static String ACTUAL_PWM_VIEW = "InternalFrameActualPWMView.fxml";

    @NotNull
    @FXML
    private Button btnBattery;
    private static String BATTERY_VIEW = "InternalFrameBatteryView.fxml";

    @NotNull
    @FXML
    private Button btnSignal;
    private static String SIGNALS_VIEW = "InternalFrameSignalsView.fxml";

    @NotNull
    @FXML
    private Button btnHeightAndSpeed;
    private static String HEIGHT_SPEED_VIEW = "InternalFrameHeightAndSpeedView.fxml";

    @NotNull
    @FXML
    private Button btnCamera;
    private static String CAMERA_VIEW = "InternalFrameVideoView.fxml";

    @NotNull
    @FXML
    private Button btnMavlinkParams;
    private static String MAVLINKPARAM_VIEW = "InternalFrameMavlinkParamsView.fxml";

    @NotNull
    @FXML
    private Button btnMavlinkConfiguration;
    private static String MAVLINKCONFIGURATION_VIEW = "InternalFrameMavlinkConfigurationView.fxml";

    @NotNull
    @FXML
    private Button btnEventLog;
    private static String EVENTLOG_VIEW = "InternalFrameEventLogView.fxml";

    @NotNull
    @FXML
    private Button btnPublish;
    @NotNull
    @FXML
    private Button btnDiscard;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get dialog manager")
    private DialogManagerSvc dialogManagerSvc;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get session service")
    private SessionsSvcRemoteWrapper sessionsSvcRemote;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get perimeters manager")
    private PerimetersManager perimetersManager;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get tree view")
    private OperationalViewTree operationalViewTree;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get map view")
    private OperationalViewMap operationalViewMap;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get video frame")
    private GlobalStatusSvc globalStatusSvc;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get event publisher")
    private EventPublisherSvc eventPublisherSvc;

    @Autowired
    private RuntimeValidator runtimeValidator;

    private boolean inPrivateSession = false;

    private static int called;

    @PostConstruct
    private void init() {
        if (called++ > 1)
            throw new RuntimeException("Not a Singleton");
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        updateFrameMapPath();

        if (!globalStatusSvc.isComponentOn(GlobalStatusSvc.Component.DETECTOR)) {
            LOGGER.warn("Detector in not loaded, setting button off");
            btnCamera.setOnDragDetected(mouseEvent -> dialogManagerSvc.showAlertMessageDialog("Camera detector was not loaded, feature is off"));
        }

        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        if (operationalViewTree.hasPrivateSession()) {
            LOGGER.debug("Private session was found");
            eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PRIVATE_SESSION_STARTED));
        }
    }

    private void updateFrameMapPath() {
        btnMap.setUserData(INTERNAL_FRAME_PATH + MAP_VIEW);
        btnActualPWM.setUserData(INTERNAL_FRAME_PATH + ACTUAL_PWM_VIEW);
        btnSignal.setUserData(INTERNAL_FRAME_PATH + SIGNALS_VIEW);
        btnHeightAndSpeed.setUserData(INTERNAL_FRAME_PATH + HEIGHT_SPEED_VIEW);
        btnBattery.setUserData(INTERNAL_FRAME_PATH + BATTERY_VIEW);
        btnCamera.setUserData(INTERNAL_FRAME_PATH + CAMERA_VIEW);
        btnMavlinkParams.setUserData(INTERNAL_FRAME_PATH + MAVLINKPARAM_VIEW);
        btnMavlinkConfiguration.setUserData(INTERNAL_FRAME_PATH + MAVLINKCONFIGURATION_VIEW);
        btnEventLog.setUserData(INTERNAL_FRAME_PATH + EVENTLOG_VIEW);
    }

    @FXML
    private void ToolbarOnDragDone(DragEvent event) {
        Button button = (Button) event.getSource();
        ColorAdjust blackout = new ColorAdjust();
        blackout.setContrast(0);
        button.setEffect(blackout);
    }

    @FXML
    private void ToolbarOnDragEvent(MouseEvent event) {
        Button button = (Button) event.getSource();

    	/* drag was detected, start a drag-and-drop gesture*/
    	/* allow any transfer mode */
        Dragboard db = button.startDragAndDrop(TransferMode.ANY);

        ColorAdjust blackout = new ColorAdjust();
        blackout.setSaturation(0.5);
        button.setEffect(blackout);
    	        
    	/* Put a string on a dragboard */
        ClipboardContent content = new ClipboardContent();
        content.putString((String) button.getUserData());
        db.setContent(content);
        event.consume();
    }

    @FXML
    private void publish() {
        LOGGER.error("publishing");
        if (operationalViewMap.isEditingLayer()) {
            LOGGER.debug("Editor is currently open, must be closed first");
            dialogManagerSvc.showAlertMessageDialog("Editor is currently open, must be closed first");
            return;
        }
        sessionsSvcRemote.publish();

        Collection<ClosingPair<Mission>> missions = missionsManager.closeAllMissionEditors(true);
        missions.forEach((ClosingPair<Mission> missionClosingPair) -> {
            Mission mission = missionClosingPair.getObject();
            LOGGER.debug("publishing mission {} {} {} {} {}", mission, mission.getKeyId().getObjId(), mission.getName(), mission.getMissionItemsUids(), mission.getDefaultAlt());
            Layer layer = operationalViewTree.getLayerByValue(mission);
            LOGGER.debug("Found layer {}", layer);
            if (layer != null) {
                // Layer will not be exist in case of deletion
                ((LayerMission) layer).setMission(mission);
                ((LayerMission) layer).stopEditing();
            }
        });

        try {
            Collection<ClosingPair<Perimeter>> perimeters  = perimetersManager.closeAllPerimeterEditors(true);
            perimeters.forEach((ClosingPair<Perimeter> perimeterClosingPair) -> {
                Perimeter perimeter = perimeterClosingPair.getObject();
                LOGGER.debug("publishing perimeter {} {} {} ", perimeter, perimeter.getKeyId().getObjId(), perimeter.getName());
                Layer layer = operationalViewTree.getLayerByValue(perimeter);
                LOGGER.debug("Found layer " + layer);
                if (layer != null) {
                    // Layer will not be exist in case of deletion
                    ((LayerPerimeter) layer).setPerimeter(perimeter);
                    ((LayerPerimeter) layer).stopEditing();
                }
            });
        } catch (PerimeterUpdateException e) {
            LOGGER.error("Critical Error: Failed to publish action", e);
        }

        operationalViewTree.regenerateTree();
        eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PUBLISH));
    }

    @FXML
    private void discard() {
        LOGGER.debug("Discarding");
        if (operationalViewMap.isEditingLayer()) {
            LOGGER.debug("Editor is currently open, must be closed first");
            dialogManagerSvc.showAlertMessageDialog("Editor is currently open, must be closed first");
            return;
        }
        sessionsSvcRemote.discard();
        // Handle mission being editing
        Collection<ClosingPair<Mission>> missions = missionsManager.closeAllMissionEditors(false);
        LOGGER.debug("Cleaning mission: " + missions);
        missions.forEach((ClosingPair<Mission> missionClosingPair) -> {
            if (missionClosingPair.isDeleted()) {
                // means we discarded a newly created layer
                LOGGER.debug("Discard / found deleted mission");
                operationalViewTree.removeItemByName(missionClosingPair.getObject().getName());
            } else {
                LOGGER.debug("Discard / reverting mission");
                Layer layer = operationalViewTree.getLayerByValue(missionClosingPair.getObject());
                if (layer == null) {
                    // Means an existing layer was deleted
                    LayerMission layerMission = new LayerMission(missionClosingPair.getObject(), operationalViewMap);
                    layerMission.stopEditing();
                    operationalViewTree.addLayer(layerMission);
                }
                else {
                    // Means we are reverting and existing layer to it previous values
                    ((LayerMission) layer).setMission(missionClosingPair.getObject());
                    layer.setName(missionClosingPair.getObject().getName());
                    ((LayerMission) layer).stopEditing();
                }
            }
        });

        // Handle perimeters being editing
        try {
            Collection<ClosingPair<Perimeter>> perimeters  = perimetersManager.closeAllPerimeterEditors(false);
            LOGGER.debug("Cleaning perimeter: " + perimeters);
            perimeters.forEach((ClosingPair<Perimeter> perimeterClosingPair) -> {
                if (perimeterClosingPair.isDeleted()) {
                    // means we discarded a newly created layer
                    LOGGER.debug("Discard / found deleted perimeter");
                    operationalViewTree.removeItemByName(perimeterClosingPair.getObject().getName());
                } else {
                    LOGGER.debug("Discard / reverting perimeter");
                    Layer layer = operationalViewTree.getLayerByValue(perimeterClosingPair.getObject());
                    if (layer == null) {
                        // Means an existing layer was deleted
                        LayerPerimeter layerPerimeter = null;
                        if (perimeterClosingPair.getObject() instanceof PolygonPerimeter)
                            layerPerimeter = new LayerPolygonPerimeter((PolygonPerimeter) perimeterClosingPair.getObject(), operationalViewMap);
                        else
                            layerPerimeter = new LayerCircledPerimeter((CirclePerimeter) perimeterClosingPair.getObject(), operationalViewMap);
                        layerPerimeter.stopEditing();
                        operationalViewTree.addLayer(layerPerimeter);
                    }
                    else {
                        // Means we are reverting and existing layer to it previous values
                        ((LayerPerimeter) layer).setPerimeter(perimeterClosingPair.getObject());
                        layer.setName(perimeterClosingPair.getObject().getName());
                        ((LayerPerimeter) layer).stopEditing();
                    }
                }
            });
        } catch (PerimeterUpdateException e) {
            LOGGER.error("Critical error: failed to discard", e);
        }

        operationalViewTree.regenerateTree();
        eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.DISCARD));
    }

    private void setImageButton(Button button, URL resource) {
        Image img = new Image(resource.toString());
        ImageView iview = new ImageView(img);
        iview.setFitHeight(40);
        iview.setFitWidth(40);
        button.setGraphic(iview);
    }

    @SuppressWarnings("incomplete-switch")
    @EventListener
    public void onApplicationEvent(QuadGuiEvent command) {
        switch (command.getCommand()) {
            case MISSION_EDITING_STARTED:
            case MISSION_UPDATED_BY_MAP:
            case MISSION_UPDATED_BY_TABLE:
            case PERIMETER_EDITING_STARTED:
            case PERIMETER_UPDATED_BY_MAP:
            case PERIMETER_UPDATED_BY_TABLE:
            case EDITMODE_EXISTING_LAYER_START:
            case PRIVATE_SESSION_STARTED:
                LOGGER.debug("Private session related event");
                if (inPrivateSession)
                    break;
                LOGGER.debug("Changing icons for private session");
                setImageButton(btnPublish, this.getClass().getResource("/com/dronegcs/console/guiImages/Save.png"));
                setImageButton(btnDiscard, this.getClass().getResource("/com/dronegcs/console/guiImages/Discard.png"));
                inPrivateSession = true;
                break;
            case PUBLISH:
            case DISCARD:
                if (!inPrivateSession)
                    break;
                LOGGER.debug("Changing icons for public session");
                setImageButton(btnPublish, this.getClass().getResource("/com/dronegcs/console/guiImages/SaveOff.png"));
                setImageButton(btnDiscard, this.getClass().getResource("/com/dronegcs/console/guiImages/DiscardOff.png"));
                inPrivateSession = false;
                break;
            case DETECTOR_LOAD_FAILURE:
                LOGGER.warn("Detector load failure, canceling button");
                setImageButton(btnCamera, this.getClass().getResource("/com/dronegcs/console/guiImages/SaveOff.png"));
                break;
            case EXIT:
                break;
        }
    }
}
