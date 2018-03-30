package com.dronegcs.console.controllers.internalFrames;

import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.generic_tools.csv.CSV;
import com.generic_tools.csv.CSVFactory;
import com.generic_tools.environment.Environment;
import com.generic_tools.validations.RuntimeValidator;
import com.generic_tools.validations.ValidatorResponse;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ResourceBundle;

@Component
public class InternalFrameHeightAndSpeed extends Pane implements OnDroneListener, Initializable {

	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;

	@Autowired @NotNull(message="Internal Error: Failed to get environment")
	private Environment environment;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@NotNull @FXML private Pane root;
	@NotNull @FXML private LineChart<String,Number> lineChart;
	
	private CSV csv;

	/** The time series data. */
	private static XYChart.Series<String, Number> seriesHeight;
	private static XYChart.Series<String, Number> seriesAirSpeed;
	private static XYChart.Series<String, Number> seriesVerticalSpeed;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lineChart.setPrefWidth(root.getPrefWidth());
		lineChart.setPrefHeight(root.getPrefHeight());
		loadChart();

		ValidatorResponse validatorResponse = runtimeValidator.validate(this);
		if (validatorResponse.isFailed())
			throw new RuntimeException(validatorResponse.toString());
	}

	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");
		
		//csv = new CSVImpl(Environment.getRunningEnvLogDirectory() + Environment.DIR_SEPERATOR + "HeightAndSpeed.csv");
		csv = CSVFactory.createNew(environment.getRunningEnvLogDirectory() + File.separator + "HeightAndSpeed.csv");
		csv.addEntry(Arrays.asList("Time", "Height", "VerticalSpeed", "AirSpeed"));
		
		drone.addDroneListener(this);
	}

	private void addValues(double altitude, double airSpeed, double verticalSpeed) {
		Platform.runLater( () -> {
			String timestamp = LocalDateTime.now().toLocalTime().toString();
			
			if (seriesHeight != null)
				seriesHeight.getData().add(new XYChart.Data<String, Number>(timestamp, altitude));
			if (seriesVerticalSpeed != null)
				seriesVerticalSpeed.getData().add(new XYChart.Data<String, Number>(timestamp, verticalSpeed));
			if (seriesAirSpeed != null)
				seriesAirSpeed.getData().add(new XYChart.Data<String, Number>(timestamp, airSpeed));
			
			csv.addEntry(Arrays.asList(timestamp, altitude, airSpeed, verticalSpeed));
		});
	}

	private void loadChart() {
        seriesHeight = new XYChart.Series<String, Number>();
        seriesHeight.setName("Height (m)");
		lineChart.getData().add(seriesHeight);
		
		seriesAirSpeed = new XYChart.Series<String, Number>();
		seriesAirSpeed.setName("Air Speed (m/s)");
		lineChart.getData().add(seriesAirSpeed);
		
		seriesVerticalSpeed = new XYChart.Series<String, Number>();
		seriesVerticalSpeed.setName("Vertical Speed (m/s)");
		lineChart.getData().add(seriesVerticalSpeed);
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
			case SPEED:
				addValues(drone.getAltitude().getAltitude(), drone.getSpeed().getAirSpeed().valueInMetersPerSecond(), drone.getSpeed().getVerticalSpeed().valueInMetersPerSecond());
				break;
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
		switch (command.getCommand()) {
		case EXIT:
			if (csv != null) 
				CSVFactory.closeFile(csv);
			break;
		}
	}
}
