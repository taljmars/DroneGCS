package com.dronegcs.console.controllers.internalPanels;

import com.dronegcs.console.controllers.GUISettings;
import com.dronegcs.console_plugin.services.internal.logevents.LogAbstractDisplayerEvent;
import com.generic_tools.logger.Logger;
import com.generic_tools.logger.Logger.Type;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

@Component
public class PanelLogBox extends Pane implements Initializable {
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PanelLogBox.class);

    @Autowired
    @NotNull(message = "Internal Error: Failed to get com.generic_tools.logger")
    private Logger logger;

    @NotNull
    @FXML
    private TextFlow logTextBox;

    @Autowired
    private RuntimeValidator runtimeValidator;

    private static int called;

    @PostConstruct
    private void init() {
        Assert.isTrue(++called == 1, "Not a Singleton");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ValidatorResponse validatorResponse = runtimeValidator.validate(this);
        if (validatorResponse.isFailed())
            throw new RuntimeException(validatorResponse.toString());

        GUISettings._WIDTH.addListener(val -> {
            logTextBox.setPrefWidth(((IntegerProperty) val).getValue() * 0.65);
        });
        GUISettings._HEIGHT.addListener(val -> {
            logTextBox.setPrefHeight(((IntegerProperty) val).getValue() * 0.15);
        });

        logTextBox.setPrefWidth(GUISettings._WIDTH.get() * 0.65);
        logTextBox.setPrefHeight(GUISettings._HEIGHT.get() * 0.15);
    }

    private void addGeneralMessegeToDisplay(String cmd) {
        LOGGER.info(cmd);
        addMessegeToDisplay(cmd, Type.GENERAL);
    }

    private void addErrorMessegeToDisplay(String cmd) {
        LOGGER.error(cmd);
        addMessegeToDisplay(cmd, Type.ERROR);
    }

    private void addWarningMessegeToDisplay(String cmd) {
        LOGGER.error(cmd);
        addMessegeToDisplay(cmd, Type.WARNING);
    }

    private void addSuccessMessegeToDisplay(String cmd) {
        LOGGER.info(cmd);
        addMessegeToDisplay(cmd, Type.SUCCESS);
    }

    private void addOutgoingMessegeToDisplay(String cmd) {
        LOGGER.info(cmd);
        addMessegeToDisplay(cmd, Type.OUTGOING);
    }

    private void addIncommingMessegeToDisplay(String cmd) {
        LOGGER.info(cmd);
        addMessegeToDisplay(cmd, Type.INCOMING);
    }

    private void addMessegeToDisplay(String cmd, Type t) {
        addMessegeToDisplay(cmd, t, false);
    }

    private synchronized void addMessegeToDisplay(String cmd, Type t, boolean no_date) {
        logger.LogDesignedMessege(Logger.generateDesignedMessege(cmd, t, no_date));

        Text newcontent = generateDesignedMessege(cmd, t, no_date);
        logTextBox.getChildren().add(0, newcontent);
    }

    public static Text generateDesignedMessege(String cmd, Type t, boolean no_date) {

        String ts_string = "";
        if (!no_date)
            ts_string = "[" + LocalDateTime.now().toLocalTime() + "] ";

		/*
         * Currently i am converting NL char to space and comma sep.
		 */
        cmd = cmd.replace("\n", ",");
        cmd = cmd.trim();

        Text newcontent = new Text(ts_string + cmd + "\n");

        switch (t) {
            case GENERAL:
                newcontent.setStyle("-fx-font-size: 14; -fx-fill: black;");
                break;
            case SUCCESS:
                newcontent.setStyle("-fx-font-size: 14; -fx-fill: green;");
                break;
            case OUTGOING:
                newcontent.setStyle("-fx-font-size: 14; -fx-fill: blue; -fx-font-weight: bold;");
                break;
            case INCOMING:
                newcontent.setStyle("-fx-font-size: 14; -fx-fill: blue");
                break;
            case WARNING:
                newcontent.setStyle("-fx-font-size: 14; -fx-fill: orange;");
                break;
            case ERROR:
                newcontent.setStyle("-fx-font-size: 14; -fx-fill: red;");
                break;
            default:
                newcontent.setStyle("-fx-font-size: 14; -fx-fill: red;");
                break;
        }

        return newcontent;
    }

    @EventListener
    public void onLogDisplayerEvent(LogAbstractDisplayerEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case WARNING:
                    addWarningMessegeToDisplay(event.getEntry());
                    break;
                case ERROR:
                    addErrorMessegeToDisplay(event.getEntry());
                    break;
                case GENERAL:
                    addGeneralMessegeToDisplay(event.getEntry());
                    break;
                case INCOMING:
                    addIncommingMessegeToDisplay(event.getEntry());
                    break;
                case OUTGOING:
                    addOutgoingMessegeToDisplay(event.getEntry());
                    break;
                case SUCCESS:
                    addSuccessMessegeToDisplay(event.getEntry());
                    break;
            }
        });
    }
}
