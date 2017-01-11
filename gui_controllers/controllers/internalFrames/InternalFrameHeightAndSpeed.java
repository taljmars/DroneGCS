package controllers.internalFrames;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import tools.os_utilities.Environment;
import validations.RuntimeValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import gui.events.QuadGuiEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import mavlink.drone.Drone;
import mavlink.drone.DroneInterfaces.DroneEventsType;
import mavlink.drone.DroneInterfaces.OnDroneListener;
import osUtilities.csv.CSV;
import osUtilities.csv.internal.CSVImpl;

@Component("internalFrameHeightAndSpeed")
public class InternalFrameHeightAndSpeed extends Pane implements OnDroneListener, Initializable {

	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;
	
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
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Value weren't initialized");
		else
			System.err.println("Validation Succeeded for instance of class " + this.getClass());
	}

	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		csv = new CSVImpl(Environment.getRunningEnvLogDirectory() + Environment.DIR_SEPERATOR + "HeightAndSpeed.csv");
		csv.open(Arrays.asList("Time", "Height", "VerticalSpeed", "AirSpeed"));
		
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
				csv.close();
			break;
		}
	}
}
