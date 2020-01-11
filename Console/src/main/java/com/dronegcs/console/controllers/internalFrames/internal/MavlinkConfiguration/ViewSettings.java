package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console.controllers.dashboard.Dashboard;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewMap;
import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.dronegcs.console_plugin.ActiveUserProfile;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.mapviewer.gui.core.mapViewer.internal.MapViewerSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

import static com.dronegcs.console.controllers.dashboard.Dashboard.DisplayMode.MAP_MODE;

@Component
public class ViewSettings extends Pane implements Initializable {


    @Autowired @NotNull(message = "Internal Error: Missing application Context")
    private ApplicationContext applicationContext;

    @Autowired
    private RuntimeValidator runtimeValidator;

//    @Autowired
    private ActiveUserProfile activeUserProfile;

    @Autowired
    private OperationalViewMap operationalViewMap;

    @NotNull @FXML private ComboBox<String> cmbMapIconFontSize;
    @NotNull @FXML private CheckBox cbLockPosition;
    @NotNull @FXML private CheckBox cbTrail;

    @NotNull @FXML private RadioButton rbMap;
    @NotNull @FXML private RadioButton rbHud;

    private static int called = 0;
    @PostConstruct
    private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");
    }

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        activeUserProfile = applicationContext.getBean(ActiveUserProfile.class);

        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        cmbMapIconFontSize.setValue(MapViewerSettings.getMarkersFontSize());
        cmbMapIconFontSize.setOnAction( e -> {
            MapViewerSettings.setMarkersFontSize(cmbMapIconFontSize.getValue());
            OperationalViewTree operationalViewTree = applicationContext.getBean(OperationalViewTree.class);
            operationalViewTree.regenerateTree();
        });

        cbLockPosition.setSelected(operationalViewMap.getLockOnMyPosition());
        cbLockPosition.setOnAction( e -> {
            operationalViewMap.setLockOnMyPosition(cbLockPosition.isSelected());
        });

        cbTrail.setSelected(operationalViewMap.getLeaveTrail());
        cbTrail.setOnAction( e -> {
            operationalViewMap.setLeaveTrail(cbTrail.isSelected());
        });

        ToggleGroup radioGroup = new ToggleGroup();
        String mode = activeUserProfile.getDefinition(String.valueOf(Dashboard.DisplayMode.DisplayMode));
        if (String.valueOf(Dashboard.DisplayMode.HUD_MODE).equals(mode)) {
            rbHud.setSelected(true);
        } else {
            rbMap.setSelected(true);
        }
        rbHud.setToggleGroup(radioGroup);
        rbMap.setToggleGroup(radioGroup);
    }

    public void handleFlushTrail(ActionEvent actionEvent) {
        operationalViewMap.flushTrail();
    }

    public void onMainScreenViewSelect(ActionEvent actionEvent) {
        if (rbHud.isSelected()) {
            activeUserProfile.setDefinition(String.valueOf(Dashboard.DisplayMode.DisplayMode), String.valueOf(Dashboard.DisplayMode.HUD_MODE));
        }
        else {
            activeUserProfile.setDefinition(String.valueOf(Dashboard.DisplayMode.DisplayMode), String.valueOf(MAP_MODE));
        }
    }
}
