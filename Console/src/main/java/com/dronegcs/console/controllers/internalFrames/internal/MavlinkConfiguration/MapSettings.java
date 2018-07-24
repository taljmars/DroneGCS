package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console.controllers.internalFrames.internal.OperationalViewTree;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import com.gui.core.mapViewer.internal.MapViewerSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MapSettings extends Pane implements Initializable {

    @Autowired @NotNull(message = "Internal Error: Missing application Context")
    private ApplicationContext applicationContext;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @NotNull @FXML private ComboBox<String> cmbMapIconFontSize;

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

        cmbMapIconFontSize.setValue(MapViewerSettings.getMarkersFontSize());
        cmbMapIconFontSize.setOnAction( e -> {
            MapViewerSettings.setMarkersFontSize(cmbMapIconFontSize.getValue());
            OperationalViewTree operationalViewTree = applicationContext.getBean(OperationalViewTree.class);
            operationalViewTree.regenerateTree();
        });
    }
}
