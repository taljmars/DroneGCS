package com.dronegcs.console.controllers.internalPanels;

import com.db.gui.persistence.scheme.Layer;
import com.db.gui.persistence.scheme.LayersGroup;
import com.db.persistence.scheme.BaseObject;
import com.dronedb.persistence.scheme.Perimeter;
import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console.controllers.dashboard.Dashboard;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console.controllers.internalFrames.internal.view_tree_layers.EditedLayer;
import com.dronegcs.console_plugin.ClosingPair;
import com.dronegcs.console_plugin.draw_editor.DrawManager;
import com.dronegcs.console_plugin.layergroup_editor.LayersGroupsManager;
import com.dronegcs.console_plugin.mission_editor.MissionsManager;
import com.dronegcs.console_plugin.perimeter_editor.PerimetersManager;
import com.dronegcs.console_plugin.remote_services_wrappers.SessionsSvcRemoteWrapper;
import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.gui.core.layers.AbstractLayer;
import com.gui.core.layers.LayerManager;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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

    @NotNull @FXML
    private Button btnPublish;

    @NotNull @FXML
    private Button btnDiscard;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get dialog manager")
    private DialogManagerSvc dialogManagerSvc;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get session service")
    private SessionsSvcRemoteWrapper sessionsSvcRemote;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get draw manager")
    private DrawManager drawManager;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get mission manager")
    private MissionsManager missionsManager;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get perimeters manager")
    private PerimetersManager perimetersManager;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get layers manager")
    private LayersGroupsManager layersGroupsManager;

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
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get logger")
    private LoggerDisplayerSvc loggerDisplayerSvc;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LayerManager layerManager;

    private boolean inPrivateSession = false;

    private static int called;

    @PostConstruct
    private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");
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
            applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PRIVATE_SESSION_STARTED));
        }
    }

    private void updateFrameMapPath() {
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

        // Make the framebar visible since we need it
        Dashboard dashboard = applicationContext.getBean(Dashboard.class);
    }

    @FXML
    public void ToolbarOnClickEvent(MouseEvent event) {
        Button button = (Button) event.getSource();

        if (event.getClickCount() == 1) {
            Dashboard dashboard = applicationContext.getBean(Dashboard.class);
        }

        if (event.getClickCount() >= 2) {
            String springInstansiation = (String) button.getUserData();
            Dashboard dashboard = applicationContext.getBean(Dashboard.class);
            dashboard.loadBigScreenContainter(springInstansiation);
        }
    }

    @FXML
    private void publish() {
        LOGGER.error("publishing");
        if (operationalViewMap.isEditingLayer()) {
            LOGGER.debug("Editor is currently open, must be closed first");
            dialogManagerSvc.showAlertMessageDialog("Editor is currently open, must be closed first");
            return;
        }

        LOGGER.debug("Start handling published layers");

        Collection<ClosingPair<Layer>> draws = drawManager.flushAllItems(true);
        LOGGER.debug("Handling published draws (amount of modified draws={})", draws.size());
        handleClosingPair(draws);

        Collection<ClosingPair<BaseObject>> missionItems = missionsManager.flushAllItems(true);
        LOGGER.debug("Handling published missions (amount of modified mission items={})", missionItems.size());
        handleClosingPair(missionItems);

        Collection<ClosingPair<Perimeter>> perimeters = perimetersManager.flushAllItems(true);
        LOGGER.debug("Handling published perimeters (amount of modified perimeters={})", perimeters.size());
        handleClosingPair(perimeters);

        Collection<ClosingPair<LayersGroup>> layersGroups = layersGroupsManager.flushAllItems(true);
        LOGGER.debug("Handling published layer groups (amount of modified layers group={})", layersGroups.size());
        handleClosingPair(layersGroups);

        sessionsSvcRemote.publish();

        int objectsAmount = draws.size() + missionItems.size() + layersGroups.size() + perimeters.size();
        loggerDisplayerSvc.logGeneral(String.format("%d objects saved successfully", objectsAmount));

        operationalViewTree.regenerateTree();
        // TODO: find better solution than reload - it too aggressive - also fix the icons
        operationalViewTree.reloadData();
        applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.PUBLISH));
    }

    public <T extends BaseObject> void handleClosingPair(Collection<ClosingPair<T>> closingPairs) {
        closingPairs.forEach((ClosingPair<T> closingPair) -> {
            LOGGER.debug("Handling Object {} , isDeleted: {}", closingPair.getObject(), closingPair.isDeleted());
            T object = closingPair.getObject();

            AbstractLayer guiLayer = layerManager.getLayerByValue(object, (guiLayer1, l) -> {
                BaseObject treeObj = (BaseObject) ((AbstractLayer) guiLayer1).getPayload();
                BaseObject searchObject = (BaseObject) l;
                if (treeObj.getKeyId().getObjId().equals(searchObject.getKeyId().getObjId()))
                    return 0;
                return -1;
            });

            if (guiLayer != null) {
                LOGGER.debug("Found layer {}", object);
                // Layer will not be exist in case of deletion
                ((EditedLayer) guiLayer).stopEditing();
                guiLayer.setWasEdited(false);
            }
            else {
                LOGGER.error("Failed to find layer {}", object);
            }
        });
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

        drawManager.flushAllItems(false);
        missionsManager.flushAllItems(false);
        perimetersManager.flushAllItems(false);
        layersGroupsManager.flushAllItems(false);

        operationalViewTree.reloadData();

        operationalViewTree.regenerateTree();
        applicationEventPublisher.publishEvent(new QuadGuiEvent(QuadGuiEvent.QUAD_GUI_COMMAND.DISCARD));
    }

    private void setImageButton(Button button, URL resource) {
        Image img = new Image(resource.toString());
        ImageView iView = new ImageView(img);
        iView.setFitHeight(40);
        iView.setFitWidth(40);
        button.setGraphic(iView);
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
