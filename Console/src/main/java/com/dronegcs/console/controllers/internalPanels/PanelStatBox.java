package com.dronegcs.console.controllers.internalPanels;

import com.dronegcs.console_plugin.services.EventPublisherSvc;
import com.dronegcs.mavlink.is.connection.ConnectionStatistics;
import com.dronegcs.mavlink.is.connection.MavLinkConnectionStatisticsListener;
import com.dronegcs.mavlink.is.drone.Drone;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class PanelStatBox extends Pane implements Initializable, MavLinkConnectionStatisticsListener {
	
	@Autowired @NotNull(message = "Internal Error: Failed to get GUI event publisher")
	protected EventPublisherSvc eventPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired
	private RuntimeValidator runtimeValidator;

	@NotNull @FXML public Label receivedPacketsVal;
	@NotNull @FXML public Label transmittedPacketsVal;
	@NotNull @FXML public Label lostPacketsVal;
	@NotNull @FXML public Label errorReceivedPacketsVal;
	@NotNull @FXML public Label errorTransmittedPacketsVal;
	@NotNull @FXML public Label receivedPacketsPerSecondVal;
	@NotNull @FXML public Label transmittedPacketsPerSecondVal;

	@NotNull @FXML public Label receivedBytesVal;
	@NotNull @FXML public Label receivedBytesPerSecondVal;
	@NotNull @FXML public Label transmittedBytesVal;
	@NotNull @FXML public Label transmittedBytesPerSecondVal;
	@NotNull @FXML public Label throughputBytesPerSecondVal;

	//
	private static int called = 0;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");

		drone.getMavClient().addMavLinkConnectionStatisticsListener(this.getClass().toString(), this);
	}
	
	@Override 
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());
	}

	@Override
	public void onConnectionStatistics(ConnectionStatistics connectionStatistics) {
		Platform.runLater(() -> {
			receivedPacketsVal.setText(connectionStatistics.getReceivedPackets() + "");
			transmittedPacketsVal.setText(connectionStatistics.getTransmittedPackets() + "");
			lostPacketsVal.setText(connectionStatistics.getLostPackets() + "");
			errorReceivedPacketsVal.setText(connectionStatistics.getReceivedErrorPackets() + "");
			errorTransmittedPacketsVal.setText(connectionStatistics.getTransmittedErrorPackets() + "");
			receivedPacketsPerSecondVal.setText(connectionStatistics.getReceivedPacketsPerSecond() + "");
			transmittedPacketsPerSecondVal.setText(connectionStatistics.getTransmittedPacketsPerSecond() + "");

			receivedBytesVal.setText(connectionStatistics.getReceivedBytes() + "");
			receivedBytesPerSecondVal.setText(connectionStatistics.getReceivedBytesPerSecond() + "");
			transmittedBytesVal.setText(connectionStatistics.getTransmittedBytes() + "");
			transmittedBytesPerSecondVal.setText(connectionStatistics.getTransmittedBytesPerSecond() + "");
			throughputBytesPerSecondVal.setText(connectionStatistics.getReceivedBytesPerSecond() + connectionStatistics.getTransmittedBytesPerSecond() + "");
		});
	}
}
