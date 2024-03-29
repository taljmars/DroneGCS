package com.dronegcs.console.controllers.droneEye;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.geo_tools.GeoTools;
import com.objects_detector.ObjectDetectorListener;
import com.objects_detector.utilities.DetectionResults;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class DroneEye extends StackPane implements ObjectDetectorListener, OnDroneListener, Initializable {
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DroneEye.class);

    @NotNull @FXML private StackPane root;
    @NotNull @FXML private ImageView imageViewer;
    @NotNull @FXML private Label lblAlt;
    @NotNull @FXML private Label lblMode;
    @NotNull @FXML private Label lblSignal;
    @NotNull @FXML private Label lblDistToLaunch;
    @NotNull @FXML private Label lblBattery;
    @NotNull @FXML private Label lblPointerToHome;
    @NotNull @FXML private Label lblPointerToHomeBorder;
    @NotNull @FXML private Label lblCompass;
    @NotNull @FXML private Label lblCompassBorder;
    @NotNull @FXML private Label lblFlightTime;
    @NotNull @FXML private Label lblFlightDist;

    @NotNull @Autowired
    private Logger logger;

    @Autowired
    private RuntimeValidator runtimeValidator;

    private static int called;

    @PostConstruct
    private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        LOGGER.info("Validation Succeeded for instance of {}", getClass());

        lblCompassBorder.setStyle("-fx-border-color: #52ee27;");
        lblPointerToHomeBorder.setStyle("-fx-border-color: #52ee27;");
        logger.LogGeneralMessege("Drone Eye initialized");
    }

    private void SetFlightModeLabel(String name) {
        lblMode.setText("MODE:" + name);
    }

    private void SetDistToLaunch(double dist) {
        lblDistToLaunch.setText(String.format("HOME:%.1f", dist) + "m");
    }

    private void SetLblAlt(double ht) {
        lblAlt.setText(String.format("ALT:%.1f", ht) + "m");
    }

    private void SetLblSignal(int signalStrength) {
        lblSignal.setText("SIG:" + signalStrength + "%");
    }

    private void SetLblBattery(double bat) {
        lblBattery.setText("BAT:" + (bat < 0 ? 0 : bat) + "%");

        if (bat == 50 || bat == 49) {
            return;
        }

        if (bat == 25 || bat == 24) {
            lblBattery.setStyle("-fx-background-color: #red;");
            return;
        }

        if (bat == 10 || bat == 9) {
            lblBattery.setStyle("-fx-background-color: #red;");
            return;
        }

        if (bat < 10) {
            return;
        }
    }

    private void SetPointerToHome(double direction) {
        lblPointerToHome.setRotate(direction);
    }

    private void SetCompass(double direction) {
        lblCompass.setRotate(direction);
    }

    @Override
    public void handleImageProcessResults(DetectionResults frameProcessResult) {
        Image img = frameProcessResult.getFinalImage();
        imageViewer.setFitWidth(root.getPrefWidth());
        imageViewer.setFitHeight(root.getPrefHeight());
        imageViewer.setPreserveRatio(true);
        imageViewer.setImage(img);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        Platform.runLater(() -> {
            switch (event) {
                case ORIENTATION:
                    SetLblAlt(drone.getAltitude().getAltitude());
                    return;
                case NAVIGATION:
                    SetCompass(drone.getNavigation().getNavBearing());
                    return;
                case DISCONNECTED:
                case HEARTBEAT_TIMEOUT:
                    SetLblAlt(0);
                    SetLblSignal(0);
                    SetLblBattery(0);
                    SetFlightModeLabel("-");
                    return;
                case RADIO:
                    SetLblSignal(drone.getRadio().getSignalStrength());
                    return;
                case BATTERY:
                    SetLblBattery(drone.getBattery().getBattRemain());
                case MODE:
                    SetFlightModeLabel(drone.getState().getMode().getName());
                    return;
                case GPS:
                    SetDistToLaunch(drone.getHome().getDroneDistanceToHome());
                    double directionToHome = GeoTools.getHeadingFromCoordinates(drone.getGps().getPosition(), drone.getHome().getCoord());
                    SetPointerToHome(directionToHome);
                    return;
            }
        });
    }
}	