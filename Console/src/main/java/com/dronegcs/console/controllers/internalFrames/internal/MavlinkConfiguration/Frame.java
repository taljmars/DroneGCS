package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console.controllers.GuiAppConfig;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces;
import com.dronegcs.mavlink.is.drone.parameters.Parameter;
import com.dronegcs.mavlink.is.drone.variables.Calibration;
import com.dronegcs.mavlink.is.protocol.msg_metadata.ApmFrameTypes;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class Frame extends Pane implements Initializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(Frame.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get application context")
    public ApplicationContext applicationContext;

    @FXML
    public Pane root;

    @FXML
    public Label lblFrame;

    @FXML
    public HBox rbFrames;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @Autowired
    private Drone drone;

    @Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
    private LoggerDisplayerSvc loggerDisplayerSvc;

    private ToggleGroup group = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        List<RadioButton> lst = new ArrayList<>();
        group = new ToggleGroup();
        for (ApmFrameTypes type : ApmFrameTypes.getFrameList(drone.getFirmwareType(), drone.getType().getDroneType())) {
            RadioButton rb = new RadioButton(type.getName());
            if (drone != null) {
                Parameter parameter = drone.getParameters().getParameter("FRAME");
                if (parameter != null && parameter.getValue().intValue() == type.getFrameType().getIndex())
                    rb.setSelected(true);
                else if (type == ApmFrameTypes.FIXED_WING)
                    rb.setSelected(true);
            }
            rb.setToggleGroup(group);
            lst.add(rb);
        }
        rbFrames.getChildren().addAll(lst);
    }

    private static int called;
    @PostConstruct
    private void init() throws URISyntaxException {
        Assert.isTrue(++called == 1, "Not a Singleton");
    }

    public void handleUpdate(ActionEvent actionEvent) {
        if (group == null)
            return;

        if (drone == null || drone.getParameters() == null) {
            loggerDisplayerSvc.logError("Drone isn't connected / synced");
            return;
        }

        if (drone.getType().isPlane()) {
            return;
        }
        Parameter frameParam = drone.getParameters().getParameter("FRAME");
        if (frameParam == null) {
            loggerDisplayerSvc.logError("Failed to find parameter name \"Frame\"");
            return;
        }
        RadioButton chk = (RadioButton)group.getSelectedToggle(); // Cast object to radio button
        frameParam.setValue(ApmFrameTypes.valueOf(chk.getText()).getFrameType().getIndex());
        drone.getParameters().sendParameter(frameParam);
        loggerDisplayerSvc.logGeneral("Frame Type changed: " + ApmFrameTypes.valueOf(chk.getText()));
    }
}
