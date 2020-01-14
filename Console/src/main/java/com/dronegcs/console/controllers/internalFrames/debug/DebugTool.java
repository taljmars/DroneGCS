package com.dronegcs.console.controllers.internalFrames.debug;

import com.dronegcs.console.controllers.dashboard.Dashboard;
import com.dronegcs.console.flightControllers.KeyBoardController;
import com.dronegcs.console_plugin.services.LoggerDisplayerSvc;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.protocol.msg_metadata.enums.MAV_SEVERITY;
import com.dronegcs.mavlink.is.protocol.msgbuilder.MavLinkSendText;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class DebugTool extends Pane implements Initializable, EventHandler<KeyEvent> {

    @FXML
    public TextField txtMessage;

    @Autowired
    private KeyBoardController keyBoardController;

    @Autowired
    private LoggerDisplayerSvc loggerDisplayerSvc;

    @Autowired
    private Dashboard dashboard;

    @Autowired
    private Drone drone;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void handle(KeyEvent event) {
        if (event.isControlDown() && event.getCode() == KeyCode.D) {
            keyBoardController.HoldIfNeeded();
            dashboard.loadBigScreenContainer("/com/dronegcs/console/views/internalFrames/InternalFrameDebug.fxml");
            keyBoardController.ReleaseIfNeeded();
        }
    }

    @FXML
    public void handleSendMessage(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            loggerDisplayerSvc.logOutgoing("Sending message '" + txtMessage.getText() + "'");
            MavLinkSendText.sendArmMessage(drone, MAV_SEVERITY.MAV_SEVERITY_EMERGENCY, txtMessage.getText());
            txtMessage.setText("");
        }
    }
}

