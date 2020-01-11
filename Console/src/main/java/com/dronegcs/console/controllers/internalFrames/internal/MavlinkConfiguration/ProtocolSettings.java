package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console.DialogManagerSvc;
import com.dronegcs.console_plugin.ActiveUserProfile;
import com.dronegcs.mavlink.core.gcs.GCSHeartbeat;
import com.dronegcs.mavlink.is.drone.Drone;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ProtocolSettings extends Pane implements Initializable {

    @Autowired @NotNull(message = "Internal Error: Failed to get drone")
    private Drone drone;

    @Autowired
    private GCSHeartbeat gcsHeartbeat;

    @Autowired @NotNull(message = "Internal Error: Missing application Context")
    private ApplicationContext applicationContext;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @Autowired
    private DialogManagerSvc dialogManagerSvc;

    @Autowired
    private ActiveUserProfile activeUserProfile;

    @FXML
    public CheckBox cbFetchOnConnect;

    @FXML
    public TextField gcsHbInterval;

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

        cbFetchOnConnect.setSelected(drone.getParameters().isFetchOnConnect());
        gcsHbInterval.setText(gcsHeartbeat.getFrequency() + "");
    }

    @FXML
    public void onFetchParameters(ActionEvent actionEvent) {
        if (cbFetchOnConnect.isSelected()) {
            drone.getParameters().setAutoFetch(true);
        }
        else {
            drone.getParameters().setAutoFetch(false);
        }
        activeUserProfile.setDefinition(ActiveUserProfile.DEFS.ParamAutoFetch.name(), String.valueOf(cbFetchOnConnect.isSelected()));
    }

    @FXML
    public void updateGCSHeartbeatInterval(ActionEvent actionEvent) {
        try {
            int interval = Integer.parseInt(gcsHbInterval.getText());
            if (interval <= 0) {
                dialogManagerSvc.showErrorMessageDialog("Interval must be greater than zero", null);
            }
            gcsHeartbeat.setFrequency(interval);
            return;
        }
        catch (NumberFormatException e) {
            dialogManagerSvc.showErrorMessageDialog("Interval must be native and complete number", e);
        }
        catch (Exception e) {
            dialogManagerSvc.showErrorMessageDialog("Interval must be greater than zero", e);
        }
        gcsHbInterval.setText(gcsHeartbeat.getFrequency() + "");
        activeUserProfile.setDefinition(ActiveUserProfile.DEFS.HeartBeatFreq.name(), gcsHbInterval.getText());
    }
}
