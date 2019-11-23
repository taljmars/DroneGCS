package com.dronegcs.console.controllers.internalFrames;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class InternalFrameQuickData extends Pane implements Initializable, DroneInterfaces.OnDroneListener {

    @NotNull @FXML private Pane root;

    @NotNull @FXML private Label lblAltValue;
    @NotNull @FXML private Label lblSpeedValue;
    @NotNull @FXML private Label lblDistValue;

    @NotNull @Autowired
    private Drone drone;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());
    }

    private static int called;
    @PostConstruct
    private void init() throws URISyntaxException {
        Assert.isTrue(++called == 1, "Not a Singleton");

        drone.addDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType droneEventsType, Drone drone) {
        Platform.runLater( () -> {
            switch (droneEventsType) {
                case SPEED:
                    lblSpeedValue.setText(String.format("%.1f", drone.getSpeed().getAirSpeed().valueInMetersPerSecond()) + " m/s");
                    break;
                case ATTITUDE:
                    lblAltValue.setText(String.format("%.1f", drone.getAltitude().getAltitude()) + " m");
                    break;
                case GPS:
                    lblDistValue.setText(String.format("%.1f", drone.getHome().getDroneDistanceToHome()) + " m");
                    break;
                case DISCONNECTED:
                    lblSpeedValue.setText("0 m/s");
                    lblAltValue.setText("0 m");
                    lblDistValue.setText("0 m");
                    break;
                default:
                    break;
            }
        });
    }
}
