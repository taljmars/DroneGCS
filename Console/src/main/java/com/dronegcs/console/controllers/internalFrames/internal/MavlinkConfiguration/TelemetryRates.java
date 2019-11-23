package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.core.firmware.FirmwareType;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.Preferences;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkStreamRates;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.event.ActionEvent;
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

    @Autowired @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @NotNull @FXML private ComboBox<String> cmbAlt;
    @NotNull @FXML private ComboBox<String> cmbPos;
    @NotNull @FXML private ComboBox<String> cmbModeStatus;
    @NotNull @FXML private ComboBox<String> cmbRC;
    @NotNull @FXML private ComboBox<String> cmbSensors;
    @NotNull @FXML private ComboBox<String> cmbController;

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

        Preferences.Rates rates = drone.getPreferences().getRates();

        cmbAlt.setValue(String.valueOf(rates.extra1));
        cmbAlt.setValue(String.valueOf(rates.extra2));
        cmbPos.setValue(String.valueOf(rates.position));
        cmbModeStatus.setValue(String.valueOf(rates.extendedStatus));
        cmbRC.setValue(String.valueOf(rates.rcChannels));
        cmbSensors.setValue(String.valueOf(rates.extra3));
        cmbSensors.setValue(String.valueOf(rates.rawSensors));
        cmbController.setValue(String.valueOf(rates.rawController));
    }

    public void UpdateRates(ActionEvent actionEvent) {
        Preferences.Rates rates = drone.getPreferences().getRates();
        rates.extra1 = Integer.parseInt(cmbAlt.getValue());
        rates.extra2 = Integer.parseInt(cmbAlt.getValue());
        rates.position = Integer.parseInt(cmbPos.getValue());
        rates.extendedStatus = Integer.parseInt(cmbModeStatus.getValue());
        rates.rcChannels = Integer.parseInt(cmbRC.getValue());
        rates.extra3 = Integer.parseInt(cmbSensors.getValue());
        rates.rawSensors = Integer.parseInt(cmbSensors.getValue());
        rates.rawController = Integer.parseInt(cmbController.getValue());

        drone.getPreferences().setRates(rates);
        drone.getStreamRates().prepareStreamRates();
        drone.getStreamRates().setupStreamRatesFromPref();
    }
}
