package com.dronegcs.console.controllers.internalFrames.internal;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.generic_tools.logger.Logger;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.geo_tools.GeoTools;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@Component
public class HUD extends StackPane implements DroneInterfaces.OnDroneListener {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HUD.class);
    private boolean initializedDone;
    private double hud_radius;

    public enum ViewLevel {
        ALL_ITEMS,
        DATA_ONLY,
        NONE
    }

    private ViewLevel viewLevel;

    @NotNull @FXML private StackPane root;

    @NotNull @FXML private Label lblAlt;
    @NotNull @FXML private Label lblMode;
    @NotNull @FXML private Label lblSignal;
    @NotNull @FXML private Label lblDistToLaunch;
    @NotNull @FXML private Label lblBattery;
    @NotNull @FXML private Label lblPointerToHome;
    @NotNull @FXML private Label lblPointerToHomeBorder;
    @NotNull @FXML private Label lblCompass;
    @NotNull @FXML private Label lblSpeed;
    @NotNull @FXML private Label lblCompassBorder;
    @NotNull @FXML private Label lblFlightTime;
    @NotNull @FXML private Label lblFlightDist;
    @NotNull @FXML private Label lblMode1;

    @NotNull @FXML private Sphere sphere;
    @NotNull @FXML private Arc arc;
    @NotNull @FXML private Line horizontal_bar_left;
    @NotNull @FXML private Line horizontal_bar_right;

    @NotNull @FXML private Label hud_p_45;
    @NotNull @FXML private Label hud_p_30;
    @NotNull @FXML private Label hud_p_15;
    @NotNull @FXML private Label hud_0;
    @NotNull @FXML private Label hud_m_15;
    @NotNull @FXML private Label hud_m_30;
    @NotNull @FXML private Label hud_m_45;

    @NotNull @FXML private Label tiltPointer;

    @NotNull @Autowired
    private Drone drone;

    @NotNull @Autowired
    private Logger logger;

    private List<Label> angleLabels;
    private List<Label> dataLabels;

    @Autowired
    private RuntimeValidator runtimeValidator;

    private static int called;

    @PostConstruct
    private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");
        drone.addDroneListener(this);

        viewLevel = ViewLevel.ALL_ITEMS;

        initializedDone = false;
    }

    @FXML
    public void initialize() {
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        PhongMaterial mat4 = new PhongMaterial();
        Image img = new Image(getClass().getResource("/com/dronegcs/console/guiImages/diffuse.jpg").toExternalForm());
        mat4.setDiffuseMap(img);
        sphere.setMaterial(mat4);

        // create ImagePattern
        SetRollAngle(0, true);
        SetPitchAngle(0, true);

        hud_radius = sphere.radiusProperty().get() * 0.7;

//        arc.setRadiusX(hud_radius);
//        arc.setRadiusY(hud_radius);

//        rotateHudLabel(hud_m_45,    -150.0, hud_radius);
//        rotateHudLabel(hud_m_30,    -130.0, hud_radius);
//        rotateHudLabel(hud_m_15,    -110.0, hud_radius);
//        rotateHudLabel(hud_0,       -90.0, hud_radius);
//        rotateHudLabel(hud_p_15,    -70.0, hud_radius);
//        rotateHudLabel(hud_p_30,    -50.0, hud_radius);
//        rotateHudLabel(hud_p_45,    -30.0, hud_radius);

        angleLabels = new ArrayList<>();
        angleLabels.add(hud_m_45);
        angleLabels.add(hud_m_30);
        angleLabels.add(hud_m_15);
        angleLabels.add(hud_0);
        angleLabels.add(hud_p_15);
        angleLabels.add(hud_p_30);
        angleLabels.add(hud_p_45);
        angleLabels.add(tiltPointer);

        for (Label label : angleLabels)
            setLabelStyle(label, "-fx-text-fill: chartreuse;");

        dataLabels = new ArrayList<>();
        dataLabels.add(lblMode);
        dataLabels.add(lblDistToLaunch);
        dataLabels.add(lblAlt);
        dataLabels.add(lblSignal);
        dataLabels.add(lblBattery);
        dataLabels.add(lblPointerToHome);
        dataLabels.add(lblCompass);
        dataLabels.add(lblFlightTime);
        dataLabels.add(lblFlightDist);
        dataLabels.add(lblMode1);
        dataLabels.add(lblSpeed);

        for (Label label : dataLabels)
            setLabelStyle(label, "-fx-text-fill: chartreuse;");

        arc.setFill(null);
        arc.getStrokeDashArray().addAll(2d, 21d);

        initializedDone = true;
    }

    private void setLabelStyle(Label lbl, String style) {
        lbl.setStyle(style);
    }

    private void rotateHudLabel(Label label, double angle, double radius) {
        label.setTranslateX(radius * Math.cos(Math.toRadians(angle)));
        label.setTranslateY(radius * Math.sin(Math.toRadians(angle)));
        label.setStyle("-fx-text-fill: chartreuse;");
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

    private void SetLblSpeed(double valueInMetersPerSecond) {
        lblSpeed.setText("Speed: " + valueInMetersPerSecond + "ms");
    }


    @SuppressWarnings("incomplete-switch")
    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        if (!initializedDone)
            return;

        Platform.runLater(() -> {
            switch (event) {
                case SPEED:
                    SetLblSpeed(drone.getSpeed().getAirSpeed().valueInMetersPerSecond());
                    break;
                case ORIENTATION:
                    SetLblAlt(drone.getAltitude().getAltitude());
                    break;
                case NAVIGATION:
                    SetCompass(drone.getNavigation().getNavBearing());
                    break;
                case DISCONNECTED:
                case HEARTBEAT_TIMEOUT:
                    SetLblAlt(0);
                    SetLblSignal(0);
                    SetLblBattery(0);
                    SetFlightModeLabel("-");
                    break;
                case RADIO:
                    SetLblSignal(drone.getRadio().getSignalStrength());
                    break;
                case BATTERY:
                    SetLblBattery(drone.getBattery().getBattRemain());
                case MODE:
                    SetFlightModeLabel(drone.getState().getMode().getName());
                    break;
                case GPS:
                    SetDistToLaunch(drone.getHome().getDroneDistanceToHome());
                    if (drone.getHome().getCoord() != null) {
                        double directionToHome = GeoTools.getHeadingFromCoordinates(drone.getGps().getPosition(), drone.getHome().getCoord());
                        SetPointerToHome(directionToHome);
                    }
                    break;
            }
            SetRollAngle(drone.getOrientation().getRoll() * -1, false);
            SetPitchAngle(drone.getOrientation().getPitch(), false);
        });
    }

    private void SetRollAngle(double roll, boolean isReset) {
        Rotate rotate = new Rotate();

        rotate.setAngle(roll);
        if (isReset) {
            sphere.getTransforms().add(rotate);
            tiltPointer.getTransforms().add(rotate);
        }
        else {
            sphere.getTransforms().set(0, rotate);
            tiltPointer.getTransforms().set(0, rotate);
        }

        tiltPointer.setTranslateX(sphere.radiusProperty().get() * 0.6 * Math.sin(Math.toRadians(roll)));
        tiltPointer.setTranslateY(-sphere.radiusProperty().get() * 0.6 * Math.cos(Math.toRadians(roll)));
    }

    private void SetPitchAngle(double pitch, boolean isReset) {
        Rotate rotateSphere = new Rotate(pitch, new Point3D(1,0,0));
        rotateSphere.setAngle(pitch);
        if (isReset)
            sphere.getTransforms().add(rotateSphere);
        else
            sphere.getTransforms().set(1, rotateSphere);
    }


    public void setHideBackground(boolean shouldHide) {
        if (shouldHide) {
            sphere.setVisible(false);
            root.setStyle("-fx-background-color: null;");
        }
        else {
            sphere.setVisible(true);
            root.setStyle("-fx-background-color: textColor;");
        }
    }

    public void setItemsLevel(ViewLevel viewLevel) {
        if (viewLevel.equals(this.viewLevel))
            return;

        Platform.runLater(() -> {
            switch (viewLevel) {
                case NONE:
                    root.setVisible(false);
                    this.viewLevel = ViewLevel.NONE;
                    break;
                case ALL_ITEMS:
                    root.setVisible(true);
                    arc.setVisible(true);
                    horizontal_bar_left.setVisible(true);
                    horizontal_bar_right.setVisible(true);
                    for (Label l : angleLabels) l.setVisible(true);
                    for (Label l : dataLabels) l.setVisible(true);
                    this.viewLevel = ViewLevel.ALL_ITEMS;
                    break;
                case DATA_ONLY:
                    root.setVisible(true);
                    arc.setVisible(false);
                    horizontal_bar_left.setVisible(false);
                    horizontal_bar_right.setVisible(false);
                    for (Label l : angleLabels) l.setVisible(false);
                    for (Label l : dataLabels) l.setVisible(true);
                    this.viewLevel = ViewLevel.DATA_ONLY;
                    break;
            }
        });
    }
}
