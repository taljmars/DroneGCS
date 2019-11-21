package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
public class TelemetryRates extends Pane implements Initializable {

    @Autowired
    @NotNull(message = "Internal Error: Failed to get GUI event publisher")
    protected ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @NotNull @FXML private ComboBox<String> cmbAlt;
    @NotNull @FXML private ComboBox<String> cmbPos;
    @NotNull @FXML private ComboBox<String> cmbModeStatus;
    @NotNull @FXML private ComboBox<String> cmbRC;
    @NotNull @FXML private ComboBox<String> cmbSensors;

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

    }
}
