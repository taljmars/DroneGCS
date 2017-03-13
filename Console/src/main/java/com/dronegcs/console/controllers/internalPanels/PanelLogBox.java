package com.dronegcs.console.controllers.internalPanels;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import com.dronegcs.gcsis.logger.Logger;
import com.dronegcs.gcsis.logger.Logger.Type;
import com.dronegcs.gcsis.validations.RuntimeValidator;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.dronegcs.console.services.internal.logevents.LogAbstractDisplayerEvent;
import com.dronegcs.gcsis.validations.ValidatorResponse;

@Component
public class PanelLogBox extends Pane implements Initializable {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get com.dronegcs.gcsis.logger")
	private Logger logger;
	
	@NotNull @FXML private TextFlow logTextBox;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());
	}
	
	private void addGeneralMessegeToDisplay(String cmd) {
		System.out.println(cmd);
		addMessegeToDisplay(cmd, Type.GENERAL);
	}
	
	private void addErrorMessegeToDisplay(String cmd) {
		System.err.println(cmd);
		addMessegeToDisplay(cmd, Type.ERROR);
	}
	
	private void addOutgoingMessegeToDisplay(String cmd) {
		System.out.println(cmd);
		addMessegeToDisplay(cmd, Type.OUTGOING);
	}
	
	private void addIncommingMessegeToDisplay(String cmd) {
		System.out.println(cmd);
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
	
	public static Text generateDesignedMessege(String cmd, Type t, boolean no_date)
	{
		
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
			case OUTGOING:
				newcontent.setStyle("-fx-font-size: 14; -fx-fill: blue;");
				break;
			case INCOMING:
				newcontent.setStyle("-fx-font-size: 14; -fx-fill: green;");
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
		Platform.runLater( () -> {
			switch (event.getType()) {
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
			}
		});
	}
}