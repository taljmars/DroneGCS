package com.dronegcs.console.controllers.internalFrames.internal.MavlinkConfiguration;

import com.dronegcs.console.controllers.GuiAppConfig;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.console_plugin.simulator.DroneStreamSerializer;
import com.dronegcs.mavlink.is.drone.Drone;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class Recording extends Pane implements Initializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(Recording.class);

    @Autowired @NotNull(message = "Internal Error: Failed to get application context")
    public ApplicationContext applicationContext;

    @Autowired @NotNull(message = "Internal Error: Failed to get application context")
    public GuiAppConfig guiAppConfig;

    @Autowired
    private RuntimeValidator runtimeValidator;

    @FXML @NotNull
    private TextField filePathTxtBox;

    @FXML @NotNull
    private ToggleButton recordBtn;

    @Autowired
    private DroneStreamSerializer droneStreamWriter;

    @Autowired
    private Drone drone;

    @Autowired @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger displayer")
    private LoggerDisplayerSvc loggerDisplayerSvc;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        if (droneStreamWriter.isRunning()) {
            recordBtn.selectedProperty().setValue(true);
            loggerDisplayerSvc.logGeneral("Record is running");
        }
    }

    private static int called;
    @PostConstruct
    private void init() throws URISyntaxException {
        Assert.isTrue(++called == 1, "Not a Singleton");
    }

    public void handleRecord(MouseEvent actionEvent) {
        if (recordBtn.selectedProperty().get()) {
            loggerDisplayerSvc.logGeneral("Record started, file: " + filePathTxtBox.getText());
            droneStreamWriter.openStream(new File(filePathTxtBox.getText()));
        }
        else {
            droneStreamWriter.close();
            loggerDisplayerSvc.logGeneral("Record Done");
        }
    }

    public void handleOpenFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(guiAppConfig.getRootStage());
        if (file != null) {
            System.out.println("Win " + file.getAbsolutePath());
            try {
                System.out.println("Win " + file.getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Win " + file.getPath());
            Platform.runLater(()-> {
                filePathTxtBox.setText(file.getPath());
            });
        }


    }
}
