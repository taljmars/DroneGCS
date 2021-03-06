package com.dronegcs.console.controllers.internalFrames;

import com.dronegcs.console.controllers.GuiAppConfig;
import com.dronegcs.console.controllers.dashboard.Dashboard;
import com.dronegcs.console.controllers.droneEye.DroneEye;
import com.dronegcs.console.controllers.internalFrames.internal.HUD;
import com.dronegcs.console_plugin.ActiveUserProfile;
import com.dronegcs.console_plugin.services.GlobalStatusSvc;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.internal.logevents.DroneGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.objects_detector.detector.Detector;
import com.objects_detector.ObjectDetectorListener;
import com.objects_detector.detector.StreamDetector;
import com.objects_detector.trackers.ColorTracker.ColorTracker;
import com.objects_detector.trackers.ColorTrackerLockSingleObject.ColorTrackerLockSingleObject;
import com.objects_detector.trackers.FakeTracker.FakeTracker;
import com.objects_detector.trackers.MovementTracker.MovmentTracker;
import com.objects_detector.trackers.TrackersEnum;
import com.objects_detector.utilities.DetectionResults;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
import java.util.ResourceBundle;

@Component
public class InternalFrameVideo extends Pane implements OnDroneListener, ObjectDetectorListener, Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(InternalFrameVideo.class);

    @FXML
    public BorderPane content;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
    private LoggerDisplayerSvc loggerDisplayerSvc;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get drone eye")
    private DroneEye externalFrameVideo;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get application context")
    private ApplicationContext applicationContext;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get event publisher")
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get global status")
    private GlobalStatusSvc globalStatusSvc;

    @Autowired
    private ActiveUserProfile activeUserProfile;

    @Autowired
    private GuiAppConfig guiAppConfig;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @Autowired
    private HUD hud;

    @NotNull
    @FXML
    private Pane root;

    @NotNull
    @FXML
    private HBox videoToolbar;

    @NotNull
    @FXML
    private ImageView imageViewer;

    @NotNull
    @FXML
    private RadioButton hud_all;

    @NotNull
    @FXML
    private RadioButton hud_dataonly;

    @NotNull
    @FXML
    private RadioButton hud_hide;

    @NotNull
    @FXML
    private Label redirectionLabel;

    @NotNull
    @FXML
    private Button opCamera;

    @NotNull
    @FXML
    private ComboBox<TrackersEnum> cbTrackerSelect;

    private StreamDetector detector;
    private InternalFrameVideo myself;

    private double originalVideoWidth = 0;
    private double originalVideoHeight = 0;

    private static int called;

    private boolean isOnMainScreen() {
        return String.valueOf(Dashboard.DisplayMode.HUD_MODE).equals(activeUserProfile.getDefinition(String.valueOf(Dashboard.DisplayMode.DisplayMode)));
    }

    @PostConstruct
    private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");

        drone.addDroneListener(this);
        try {
            int devID = Integer.parseInt(activeUserProfile.getDefinition("deviceId", "0"));
            detector = new StreamDetector(devID);
            detector.setTracker(null);
            detector.addListener(this);
            globalStatusSvc.setComponentStatus(GlobalStatusSvc.Component.DETECTOR, true);
        } catch (Throwable e) {
            LOGGER.warn("Failed to initialize detector", e);
            LOGGER.warn("Flight detector and camera will not be accessible");
            loggerDisplayerSvc.logError("Failed to initialize detector: " + e.getMessage());
            applicationEventPublisher.publishEvent(new DroneGuiEvent(DroneGuiEvent.DRONE_GUI_COMMAND.DETECTOR_LOAD_FAILURE));
        }

        myself = this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbTrackerSelect.getItems().addAll(FXCollections.observableArrayList(TrackersEnum.values()));
        cbTrackerSelect.setValue(TrackersEnum.VIDEO_ONLY);

        // Setting the initial value for the detector
        handleTrackerSelectOnAction(null);

        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

//        handleOpHudOnAction(null);

        ToggleGroup radioGroup = new ToggleGroup();
        hud_all.setToggleGroup(radioGroup);
        hud_dataonly.setToggleGroup(radioGroup);
        hud_hide.setToggleGroup(radioGroup);

        hud.setHideBackground(false);

        if (isOnMainScreen()) {
            content.setTop(null);
            hud.setItemsLevel(HUD.ViewLevel.ANGLE_ONLY);
            handleOpCameraOnAction(null);
        }
    }

    @FXML
    public void handleVideoMouseClick(MouseEvent mouseEvent) {
        if (isOnMainScreen()) {
            return;
        }
        if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED && mouseEvent.getClickCount() >= 2) {
            Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
            double ratio = (primaryScreenBounds.getHeight() - 50) / originalVideoHeight;
            double height = primaryScreenBounds.getHeight();
            double width = originalVideoWidth * ratio;
            Parent droneEyeView = (Parent) guiAppConfig.loadFrame("/com/dronegcs/console/views/DroneEyeView.fxml", width, height);
            loggerDisplayerSvc.logGeneral("add drone listening to drone eye view");
            drone.addDroneListener(externalFrameVideo);
            detector.addListener(externalFrameVideo);
            detector.removeListener(myself);
            redirectionLabel.setVisible(true);
            imageViewer.setVisible(false);
            Stage stage = new Stage();
            stage.setTitle("Drone Eye");
            stage.setResizable(false);
            stage.setScene(new Scene(droneEyeView, width, height));
            stage.setOnCloseRequest(windowEvent -> {
                if (windowEvent.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
                    loggerDisplayerSvc.logGeneral("Closing maximized video");
                    detector.removeListener(externalFrameVideo);
                    redirectionLabel.setVisible(false);
                    imageViewer.setVisible(true);
                    detector.addListener(myself);
                    loggerDisplayerSvc.logGeneral("Removing drone listening from drone eye view");
                    drone.removeDroneListener(externalFrameVideo);
                }
            });
            stage.show();
        }
    }


    @SuppressWarnings("incomplete-switch")
    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch (event) {
        }
    }

    @SuppressWarnings("incomplete-switch")
    @EventListener
    public void onApplicationEvent(DroneGuiEvent command) {
        switch (command.getCommand()) {
            case EXIT:
                if (detector != null)
                    detector.stop();
                break;
            case CAMERA_DEVICEID:
                loggerDisplayerSvc.logGeneral("Changing device id");
                if (detector != null && detector.isActive()) {
                    loggerDisplayerSvc.logGeneral("Device is running  stopping current before switching id");
                    detector.stop();
                    String deviceId = activeUserProfile.getDefinition("deviceId");
                    if (deviceId == null) {
                        loggerDisplayerSvc.logError("Failed to identify device ID");
                        return;
                    }
                    detector.setDeviceId(Integer.parseInt(deviceId));
                    detector.start();
                }
                break;
        }
    }

    @Override
    public void handleImageProcessResults(DetectionResults frameProcessResult) {
        Image img = frameProcessResult.getFinalImage();
        originalVideoWidth = img.getWidth();
        originalVideoHeight = img.getHeight();
//        imageViewer.setFitWidth(root.getPrefWidth());
//        imageViewer.setPreserveRatio(true);
        imageViewer.setImage(img);
//        imageViewer.setLayoutX((imageViewer.getImage().getWidth() - root.getPrefWidth())/2);
//        imageViewer.setLayoutX(100);
    }


    @FXML
    public void handleOpCameraOnAction(ActionEvent actionEvent) {
        if (detector.isActive()) {
            opCamera.setText("Start Camera");
            detector.stop();
            imageViewer.setVisible(false);
            hud.setHideBackground(false);
        } else {
            opCamera.setText("Stop Camera");
            detector.start();
            imageViewer.setVisible(true);
            hud.setHideBackground(true);
        }
    }

    @FXML
    public void handleTrackerSelectOnAction(ActionEvent actionEvent) {
        TrackersEnum value = cbTrackerSelect.getValue();
        if (detector == null)
            return;

        switch (value) {
            case MOVEMENT_TRACKER:
                detector.setTracker(new MovmentTracker(10, 24));
                break;
            case COLOR_TRACKER:
                detector.setTracker(new ColorTracker(156, 180, 171, 232, 80, 185, 10, 24, 30));
                break;
            case COLOR_TRACKER_SINGLE_OBJECT:
                detector.setTracker(new ColorTrackerLockSingleObject(156, 180, 171, 232, 80, 185, 10, 24, 30));
                break;
            case VIDEO_ONLY:
                detector.setTracker(new FakeTracker());
                break;
            default:
                break;
        }
    }

    public void setHudViewAll(ActionEvent actionEvent) {
        hud.setItemsLevel(HUD.ViewLevel.ALL_ITEMS);
    }

    public void setHudViewDataOnly(ActionEvent actionEvent) {
        hud.setItemsLevel(HUD.ViewLevel.DATA_ONLY);
    }

    public void setHudViewHide(ActionEvent actionEvent) {
        hud.setItemsLevel(HUD.ViewLevel.NONE);
    }
}
