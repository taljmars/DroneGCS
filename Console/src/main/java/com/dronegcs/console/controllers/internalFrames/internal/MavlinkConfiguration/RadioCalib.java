package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by taljmars on 6/28/2017.
 */
@Component
public class RadioCalib implements DroneInterfaces.OnDroneListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(RadioCalib.class);

    private static final Integer MAX_PWM = 2200;
    private static final Integer MIN_PWM = 800;

    @Autowired
    @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @FXML @NotNull private ScrollBar pitchBar;
    @FXML @NotNull private ScrollBar yawBar;
    @FXML @NotNull private ScrollBar throttleBar;
    @FXML @NotNull private ScrollBar rollBar;

    @FXML @NotNull private Label lblPitch;
    @FXML @NotNull private Label lblYaw;
    @FXML @NotNull private Label lblThrottle;
    @FXML @NotNull private Label lblRoll;

    @FXML @NotNull private ScrollBar ch5Bar;
    @FXML @NotNull private ScrollBar ch6Bar;
    @FXML @NotNull private ScrollBar ch7Bar;
    @FXML @NotNull private ScrollBar ch8Bar;

    @FXML @NotNull private Label lblCh5;
    @FXML @NotNull private Label lblCh6;
    @FXML @NotNull private Label lblCh7;
    @FXML @NotNull private Label lblCh8;

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType droneEventsType, Drone drone) {
        Platform.runLater(() -> {
            switch (droneEventsType) {
                case RC_IN:
                    update( drone.getRC().in[0], drone.getRC().in[1], drone.getRC().in[2], drone.getRC().in[3],
                            drone.getRC().in[4], drone.getRC().in[5], drone.getRC().in[6], drone.getRC().in[7]);
                    break;
            }
        });
    }

    private void update(int roll, int pitch, int thrust, int yaw,
                        int ch5, int ch6, int ch7, int ch8) {
        lblRoll.setText("Roll: " + roll);
        lblPitch.setText("Pitch: " + pitch);
        lblThrottle.setText("Throttle: " + thrust);
        lblYaw.setText("Yaw: " + yaw);

        rollBar.setValue(roll - MIN_PWM);
        pitchBar.setValue(MAX_PWM - pitch);
        throttleBar.setValue(MAX_PWM - thrust);
        yawBar.setValue(yaw - MIN_PWM);

        lblCh5.setText("Channel 5: " + ch5);
        lblCh6.setText("Channel 6: " + ch6);
        lblCh7.setText("Channel 7: " + ch7);
        lblCh8.setText("Channel 8: " + ch8);

        ch5Bar.setValue(ch5 - MIN_PWM);
        ch6Bar.setValue(ch6 - MIN_PWM);
        ch7Bar.setValue(ch7 - MIN_PWM);
        ch8Bar.setValue(ch8 - MIN_PWM);

    }

    @PostConstruct
    private void init() {
        drone.addDroneListener(this);
    }
}
