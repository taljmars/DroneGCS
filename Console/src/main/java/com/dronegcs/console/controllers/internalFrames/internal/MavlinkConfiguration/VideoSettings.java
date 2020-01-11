package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console_plugin.ActiveUserProfile;
import com.dronegcs.console_plugin.services.internal.logevents.DroneGuiEvent;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class VideoSettings extends Pane implements Initializable {

    @Autowired
    @NotNull(message = "Internal Error: Failed to get GUI event publisher")
    protected ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private DialogManagerSvc dialogManagerSvc;

    @Autowired
    private ActiveUserProfile activeUserProfile;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @NotNull @FXML private Button btnUpdateDevice;
    @NotNull @FXML private TextField txtDeviceId;

    private static int called = 0;
    @PostConstruct
    private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        String deviceId = activeUserProfile.getDefinition("deviceId", "0");
        txtDeviceId.setText(deviceId);

        btnUpdateDevice.setOnAction( e -> {
            if (!txtDeviceId.getText().matches("[0-9]*")) {
                dialogManagerSvc.showErrorMessageDialog("Value '{}' is illegal, must be numeric", new NumberFormatException());
                return;
            }
            activeUserProfile.setDefinition("deviceId", txtDeviceId.getText());
            applicationEventPublisher.publishEvent(new DroneGuiEvent(DroneGuiEvent.DRONE_GUI_COMMAND.CAMERA_DEVICEID, Integer.parseInt(txtDeviceId.getText())));
        });
    }
}
