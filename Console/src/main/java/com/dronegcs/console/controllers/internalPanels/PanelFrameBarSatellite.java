package com.dronegcs.console.controllers.internalPanels;

import com.dronedb.persistence.ws.internal.SessionsSvcRemote;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.LayerMission;
import com.dronegcs.console_plugin.mission_editor.MissionClosingPair;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.services.DialogManagerSvc;
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
    private Button btnPublish;
    @NotNull
    @FXML
    private Button btnDiscard;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get dialog manager")
    private DialogManagerSvc dialogManagerSvc;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get session service")
    private SessionsSvcRemote sessionsSvcRemote;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

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
            LOGGER.error("Detector in not loaded, setting button off");
            btnCamera.setOnDragDetected(mouseEvent -> dialogManagerSvc.showAlertMessageDialog("Camera detector was not loaded, feature is off"));
        }

        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        inPrivateSession = operationalViewTree.hasPrivateSession();
        if (inPrivateSession) {
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
        sessionsSvcRemote.publish();
        Collection<MissionClosingPair> missions = missionsManager.closeAllMissionEditors(true);
        missions.forEach((MissionClosingPair missionClosingPair) -> {
            LOGGER.error("publishing mission " +
                    " " + missionClosingPair.getMission() +
                    " " + missionClosingPair.getMission().getKeyId().getObjId() +
                    " " + missionClosingPair.getMission().getName() +
                    " " + missionClosingPair.getMission().getMissionItemsUids() +
                    " " + missionClosingPair.getMission().getDefaultAlt());
            Layer layer = operationalViewTree.getLayerByValue(missionClosingPair.getMission());
            LOGGER.error("Found layer " + layer);
            if (layer instanceof LayerMission) {
                ((LayerMission) layer).setMission(missionClosingPair.getMission());
                ((LayerMission) layer).stopEditing();
            }
        });
        operationalViewTree.regenerateTree();
        eventPublisherSvc.publish(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PUBLISH));
    }

    @FXML
    private void discard() {
        LOGGER.error("Discarding");
        sessionsSvcRemote.discard();
        // Handle mission being editing
        Collection<MissionClosingPair> missions = missionsManager.closeAllMissionEditors(false);
        LOGGER.error("Cleaning mission: " + missions);
        missions.forEach((MissionClosingPair missionClosingPair) -> {
            if (missionClosingPair.isDeleted()) {
                LOGGER.error("Discard / found deleted mission");
                operationalViewTree.removeItemByName(missionClosingPair.getMission().getName());
            } else {
                LOGGER.error("Discard / reverting mission");
                Layer layer = operationalViewTree.getLayerByValue(missionClosingPair.getMission());
                if (layer == null) {
                    LayerMission layerMission = new LayerMission(missionClosingPair.getMission(), operationalViewMap);
                    layerMission.stopEditing();
                    operationalViewTree.addLayer(layerMission);
                } else if (layer instanceof LayerMission) {
                    ((LayerMission) layer).setMission(missionClosingPair.getMission());
                    layer.setName(missionClosingPair.getMission().getName());
                    ((LayerMission) layer).stopEditing();
                }
            }
        });
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
            case EDITMODE_EXISTING_LAYER_START:
            case PRIVATE_SESSION_STARTED:
                if (inPrivateSession)
                    break;
                System.err.print("Changing icons for private session");
                setImageButton(btnPublish, this.getClass().getResource("/com/dronegcs/console/guiImages/Save.png"));
                setImageButton(btnDiscard, this.getClass().getResource("/com/dronegcs/console/guiImages/Discard.png"));
                inPrivateSession = true;
                break;
            case PUBLISH:
            case DISCARD:
                if (!inPrivateSession)
                    break;
                System.err.print("Changing icons for public session");
                setImageButton(btnPublish, this.getClass().getResource("/com/dronegcs/console/guiImages/SaveOff.png"));
                setImageButton(btnDiscard, this.getClass().getResource("/com/dronegcs/console/guiImages/DiscardOff.png"));
                inPrivateSession = false;
                break;
            case DETECTOR_LOAD_FAILURE:
                System.err.print("Detector load failure, canceling button");
                setImageButton(btnCamera, this.getClass().getResource("/com/dronegcs/console/guiImages/SaveOff.png"));
                break;
            case EXIT:
                break;
        }
    }
}
