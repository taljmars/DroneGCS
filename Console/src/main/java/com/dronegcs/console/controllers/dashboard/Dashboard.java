package com.dronegcs.console.controllers.dashboard;

import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console.controllers.ActiveUserProfile;
import com.dronegcs.console.controllers.GUISettings;
import com.dronegcs.console.controllers.GuiAppConfig;
import com.dronegcs.console.controllers.internalFrames.InternalFrameMap;
import com.dronegcs.console.operations.OpGCSTerminationHandler;
import com.dronegcs.console_plugin.plugin_event.ClientPluginEvent;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.services.TextNotificationPublisherSvc;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnParameterManagerListener;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnWaypointManagerListener;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmModes;
import com.dronegcs.mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static com.dronegcs.mavlink.is.drone.profiles.Parameters.UNINDEX_PARAM;

@Component
public class Dashboard extends StackPane implements OnDroneListener, OnWaypointManagerListener, OnParameterManagerListener, EventHandler<WindowEvent>, Initializable {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Dashboard.class);
    public static final String APP_TITLE = "Drone Ground Station";

    private String app_ver = "unknown";

    @Autowired @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @Autowired
    private DialogManagerSvc dialogManagerSvc;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired @NotNull(message = "Internal Error: Failed to get text publisher")
    private TextNotificationPublisherSvc textNotificationPublisherSvc;

    @Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
    private LoggerDisplayerSvc loggerDisplayerSvc;

    @Autowired @NotNull(message = "Internal Error: Failed to get GCS terminator handler")
    private OpGCSTerminationHandler opGCSTerminationHandler;

    @Autowired @NotNull(message = "Internal Error: Failed to get map frame")
    private InternalFrameMap internalFrameMap;

    @Autowired @NotNull(message = "Internal Error: Failed to get gui configuration")
    private GuiAppConfig guiAppConfig;

    @Autowired @NotNull(message = "Internal Error: Failed to get floatingNodeManager")
    private FloatingNodeManager floatingNodeManager;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @Autowired
    private ActiveUserProfile activeUserProfile;

    @FXML private StackPane dashboardView;

    private static final double BOTTOM_PANEL_RATIO_H = 0.15;
    private static final double BOTTOM_PANEL_RATIO_W = 0.65;
    private static final double BOTTOM_PANEL_RATIO_BOTTOM_MARGIN_H = 0.04;
    @FXML private Pane bottomPanel;
    @FXML private TabPane bottomPanelTab;

    @FXML
    private ProgressIndicator progressIndicator;

    private static final double BIG_SCREEN_CONTAINER_RATIO_H = 0.55;
    private static final double BIG_SCREEN_CONTAINER_RATIO_W = 0.6;
    @FXML private Pane bigScreenContainer;

    @FXML private CheckBox widgetDragging;

    private Stage viewManager;

    private static int called;

    @PostConstruct
    private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");

        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        initializeDefinitions();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InputStream inputStream = null;
        try {
            inputStream = Dashboard.class.getClassLoader().getResourceAsStream("version");
            byte[] versionBuffer = new byte[32];
            inputStream.read(versionBuffer);
            app_ver = new String(versionBuffer);
            app_ver = "v1." + app_ver.trim();
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        initializeGui();
    }

    private void initializeDefinitions() {
        LOGGER.info("Sign Dashboard as drone listener");
        drone.addDroneListener(this);
        drone.getWaypointManager().addWaypointManagerListener(this);
        drone.getParameters().addParameterListener(this);

        if (drone.isConnectionAlive()) {
//			tbTelemtry.SetHeartBeat(true);
            drone.notifyDroneEvent(DroneEventsType.MODE);
        }
    }

    private void initializeGui() {
        LOGGER.info("Initialize GUI");
        setViewManager(guiAppConfig.getRootStage());

        floatingNodeManager.bind(widgetDragging.selectedProperty());
        setDragPlane(dashboardView);

        internalFrameMap.reloadData();

        GUISettings._WIDTH.addListener(val -> {
            int intVal = ((IntegerProperty) val).getValue();
            double newBottomWidth = intVal * BOTTOM_PANEL_RATIO_W;
            bottomPanel.setPrefWidth(newBottomWidth);
            double padX = (intVal - newBottomWidth) / 2;
            Insets currentInsets = StackPane.getMargin(bottomPanel);
            Insets insets = new Insets(currentInsets.getTop(), padX, currentInsets.getBottom(), padX);
            StackPane.setMargin(bottomPanel, insets);
        });
        GUISettings._HEIGHT.addListener(val -> {
            int intVal = ((IntegerProperty) val).getValue();
            double newBottomHeight = intVal * BOTTOM_PANEL_RATIO_H;
            bottomPanel.setPrefHeight(newBottomHeight);
            double padY = intVal * BOTTOM_PANEL_RATIO_BOTTOM_MARGIN_H;
            Insets currentInsets = StackPane.getMargin(bottomPanel);
            Insets insets = new Insets(intVal - newBottomHeight - padY, currentInsets.getRight(), padY, currentInsets.getLeft());
            StackPane.setMargin(bottomPanel, insets);
        });

        GUISettings._WIDTH.addListener(val -> bottomPanelTab.setPrefWidth(((IntegerProperty) val).getValue() * BOTTOM_PANEL_RATIO_W));
        GUISettings._HEIGHT.addListener(val -> bottomPanelTab.setPrefHeight(((IntegerProperty) val).getValue() * BOTTOM_PANEL_RATIO_H));
    }

    private void setDragPlane(Pane plane) {
        plane.setOnDragOver((event) -> {
            /* data is dragged over the target */
            /* accept it only if it is not dragged from the same node
             * and if it has a string data */
            if (event.getGestureSource() != plane && event.getDragboard().hasString()) {
                /* allow for both copying and moving, whatever user chooses */
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            event.consume();
        });

        plane.setOnDragDropped((event) -> {
            /* data dropped */
            /* if there is a string data on dragboard, read it and use it */
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
//                loggerDisplayerSvc.logGeneral(db.getString());
                LOGGER.debug("Generating drop plugin: " + db.getString());
                success = true;
            }

            if (event.getGestureSource() instanceof Button) {
                Button button = (Button) event.getGestureSource();
                handleFrameContainerRequest((String) button.getUserData());
                /* let the source know whether the string was successfully transferred and used */
                event.setDropCompleted(success);
            } else {
                event.setDropCompleted(false);
            }

            event.consume();
        });

        plane.setOnDragEntered((event) -> event.consume());
    }

    private void SetDistanceToWaypoint(double d) {
        if (drone.getState().getMode().equals(ApmModes.ROTOR_GUIDED)) {
            // if (drone.getGuidedPoint().isIdle()) {
            if (d == 0) {
                textNotificationPublisherSvc.publish("In Position");
                loggerDisplayerSvc.logGeneral("Guided: In Position");
            } else {
                textNotificationPublisherSvc.publish("Flying to destination");
                loggerDisplayerSvc.logGeneral("Guided: Fly to destination");
            }
        }
    }

    @Override
    public void onBeginWaypointEvent(WaypointEvent_Type wpEvent) {
        initProgressBar();
        if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD) || wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
            return;
        }
        finishProgressBar();
    }

    @Override
    public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
        if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD) || wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
            setProgressBar((double) index / count);
            return;
        }
        finishProgressBar();
    }

    @Override
    public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
        finishProgressBar();
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch (event) {
            case LEFT_PERIMETER:
                textNotificationPublisherSvc.publish("Outside Perimeter");
                loggerDisplayerSvc.logError("Drone left the perimeter");
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            case ENFORCING_PERIMETER:
                textNotificationPublisherSvc.publish("Enforcing Perimeter");
                loggerDisplayerSvc.logError("Enforcing Perimeter");
                return;
            case ORIENTATION:
                SetDistanceToWaypoint(drone.getMissionStats().getDistanceToWP());
                return;
            case MODE:
                String prefix = "";
                if (activeUserProfile.getMode() == ActiveUserProfile.Mode.OFFLINE)
                    prefix = "[OFFLINE] ";
                String finalPrefix = prefix;
                Platform.runLater(() -> viewManager.setTitle(finalPrefix + APP_TITLE + " [" + app_ver + "] (" + drone.getState().getMode().getName() + ")"));
                return;
            case TEXT_MESSEGE:
                loggerDisplayerSvc.logIncoming(drone.getMessegeQueue().pop());
                return;
            case WARNING_SIGNAL_WEAK:
                loggerDisplayerSvc.logWarning("Warning: Weak signal");
                loggerDisplayerSvc.logWarning("Warning: Weak signal");
                loggerDisplayerSvc.logWarning("Warning: Weak signal");
                java.awt.Toolkit.getDefaultToolkit().beep();
                java.awt.Toolkit.getDefaultToolkit().beep();
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            case FOLLOW_START:
                loggerDisplayerSvc.logGeneral("Follow Me Started");
                return;
            case FOLLOW_UPDATE:
                loggerDisplayerSvc.logGeneral("Follow Me Updated");
                return;
            case FOLLOW_STOP:
                loggerDisplayerSvc.logGeneral("Follow Me Ended");
                return;
            case FIRMWARE:
                loggerDisplayerSvc.logGeneral("Firmware Identified: " + drone.getFirmwareType());
                return;
            case TYPE:
                loggerDisplayerSvc.logGeneral("Drone Type: " + drone.getType().getDroneType());
                return;
        }
    }

    private synchronized void initProgressBar() {
        LOGGER.info("Init progress bar");
        progressIndicator.setVisible(true);
        progressIndicator.setProgress(0);
    }

    private synchronized void setProgressBar(double val) {
        progressIndicator.setProgress(val);
        if (progressIndicator.getProgress() == 1.0) {
            finishProgressBar();
        }
    }

    private synchronized void finishProgressBar() {
        LOGGER.info("Finish progress bar");
        progressIndicator.setVisible(false);
    }

    @Override
    public void onBeginReceivingParameters() {
        LOGGER.debug("Start Receiving parameters");
        initProgressBar();
        drone.getStreamRates().prepareStreamRates();
    }

    @Override
    public void onParameterReceived(Parameter parameter, int index, int count) {
        if (index == UNINDEX_PARAM) {
            return;
        }
        LOGGER.debug("Received parameter {}/{}: {}", index, count, parameter);
        int prc = drone.getParameters().getPercentageComplete();
        if (prc > 95) {
            setProgressBar(1);
            drone.getStreamRates().setupStreamRatesFromPref();
            if (drone.isConnectionAlive())
                drone.notifyDroneEvent(DroneEventsType.MODE);
        } else {
            setProgressBar(((double) prc) / 100.0);
        }
    }

    @Override
    public void onEndReceivingParameters(List<Parameter> parameter) {
        LOGGER.debug("Finish receiving parameters");
        finishProgressBar();

        drone.getStreamRates().prepareStreamRates();
        drone.getStreamRates().setupStreamRatesFromPref();
        if (drone.isConnectionAlive())
            drone.notifyDroneEvent(DroneEventsType.MODE);
    }

    public void setViewManager(Stage stage) {
        this.viewManager = stage;
        String prefix = "";
        if (activeUserProfile.getMode() == ActiveUserProfile.Mode.OFFLINE)
            prefix = "[OFFLINE] ";
        viewManager.setTitle(prefix + APP_TITLE + " [" + app_ver + "]");
        viewManager.setOnCloseRequest(this);
    }

    @Override
    public void handle(WindowEvent event) {
        if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
            try {
                opGCSTerminationHandler.go();
                event.consume();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFrameContainerRequest(String springInstantiation) {
        if (springInstantiation.isEmpty())
            return;

        Node selectedPane = guiAppConfig.loadInternalFrame(springInstantiation, GUISettings._WIDTH.get() * 0.3, GUISettings._HEIGHT.get() * 0.3);
        ((Pane)selectedPane).setMaxSize(GUISettings._WIDTH.get() * 0.3, GUISettings._HEIGHT.get() * 0.3);
        selectedPane.setUserData(springInstantiation);
        selectedPane = floatingNodeManager.makeDraggable(dashboardView, selectedPane, GUISettings._WIDTH.get() * 0.3, GUISettings._HEIGHT.get() * 0.3);
        Button b = new Button();
        b.setText("X");
//        b.setCancelButton(true);
        b.setLayoutX(-10);
        b.setLayoutY(-10);
        ((Pane) selectedPane).getChildren().add(b);
        b.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            b.getParent().setVisible(false);
        });
        dashboardView.getChildren().add(selectedPane);
    }

    public void loadBigScreenContainer(String springInstantiation) {
        Pane node = (Pane) guiAppConfig.loadInternalFrame(springInstantiation, GUISettings._WIDTH.get() * BIG_SCREEN_CONTAINER_RATIO_W, GUISettings._HEIGHT.get() * BIG_SCREEN_CONTAINER_RATIO_H);
        bigScreenContainer.getChildren().clear();
        bigScreenContainer.getChildren().addAll(node);

        bigScreenContainer.setVisible(true);
        bigScreenContainer.setOnMouseClicked(event -> {
            if (event.isPopupTrigger())
                bigScreenContainer.setVisible(false);
        });

        double leftrightPadding = (GUISettings._WIDTH.get() * (1 - BIG_SCREEN_CONTAINER_RATIO_W)) / 2;
        double topbuttomPadding = (GUISettings._HEIGHT.get() * (1 - BIG_SCREEN_CONTAINER_RATIO_H)) / 2;
        Insets insets = new Insets(topbuttomPadding, leftrightPadding, topbuttomPadding, leftrightPadding);
        StackPane.setMargin(bigScreenContainer, insets);

        Button b = new Button();
        b.setText("X");
//        b.setCancelButton(true);
        b.setTranslateX(-10 - bigScreenContainer.widthProperty().get() / 2);
        b.setTranslateY(-10 - bigScreenContainer.heightProperty().get() / 2);
//        b.setLayoutX(leftrightPadding);
//        b.setLayoutY(topbuttomPadding);
        bigScreenContainer.getChildren().add(b);
        b.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            b.getParent().setVisible(false);
        });
    }

    @SuppressWarnings("incomplete-switch")
    @EventListener
    public void onApplicationEvent(ClientPluginEvent event) {
        switch (event.getType()) {
            case SERVER_LOST:
                Platform.runLater(() -> {
                    loggerDisplayerSvc.logError("Connectivity lost to server");
                    dialogManagerSvc.showAlertMessageDialog("Connectivity Lost to server, restart the application " +
                        "once connectivity restored");
                    opGCSTerminationHandler.terminateNow();
                });
                break;
            case SERVER_CLOCK:
                LOGGER.debug("Current server time: " + event.getPayload().get(0));
                break;
        }
    }
}
