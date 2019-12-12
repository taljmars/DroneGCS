package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console.controllers.GuiAppConfig;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.dronegcs.mavlink.is.drone.variables.Calibration;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class AccelerometerCalibration extends Pane implements Initializable, DroneInterfaces.OnDroneListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccelerometerCalibration.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get application context")
    public ApplicationContext applicationContext;

    @Autowired @NotNull(message = "Internal Error: Failed to get application context")
    public GuiAppConfig guiAppConfig;

    @FXML
    public Label lblGyroStatus;

    @FXML
    public Pane root;

    @FXML
    public Label lblLevelStatus;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @Autowired
    private Drone drone;

    @Autowired
    private Calibration calibration;

    @Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
    private LoggerDisplayerSvc loggerDisplayerSvc;

    private int step = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        root.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.SPACE && calibration.isCalibrating()) {
                calibration.sendAck(step);
            }
        });
    }

    private static int called;
    @PostConstruct
    private void init() throws URISyntaxException {
        Assert.isTrue(++called == 1, "Not a Singleton");
    }


    @FXML
    public void handleCalibrationGyro(ActionEvent actionEvent) {
        if (calibration.isCalibrating()) {
            if (step == 0) loggerDisplayerSvc.logError("Calibration already started");
            return;
        }
        step = 0;
        drone.addDroneListener(this);
        lblGyroStatus.setText("");
        calibration.startAccelerometerCalibration();

    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType droneEventsType, Drone drone) {
        switch (droneEventsType) {
            case CALIBRATION_IMU:
                String message = calibration.getMessage();
                if (!calibration.isCalibrating())
                    this.drone.removeDroneListener(this);

                Platform.runLater(() -> lblGyroStatus.setText(message));
                step++;
                break;
        }
    }

    public void handleCalibrationLevel(ActionEvent actionEvent) {
        if (calibration.startLevelCalibration())
            lblLevelStatus.setText("Completed");
        else
            lblLevelStatus.setText("Failed");
    }
}
