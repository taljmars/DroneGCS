package main.java.gui_controllers.controllers.internalFrames;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import tools.os_utilities.Environment;
import main.java.is.validations.RuntimeValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import main.java.is.gui.events.QuadGuiEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import main.java.is.mavlink.drone.Drone;
import main.java.is.mavlink.drone.DroneInterfaces.DroneEventsType;
import main.java.is.mavlink.drone.DroneInterfaces.OnDroneListener;
import main.java.is.objects.csv.CSV;
import main.java.is.objects.csv.internal.CSVImpl;

@Component
public class InternalFrameSignals extends Pane implements OnDroneListener, Initializable {

	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@NotNull @FXML private Pane root;
	@NotNull @FXML private LineChart<String,Number> lineChart;
	
	private CSV csv;

	/** The time series data. */
	private static XYChart.Series<String, Number> seriesDistance;
	private static XYChart.Series<String, Number> seriesSignal;
	private static XYChart.Series<String, Number> seriesNoise;
	private static XYChart.Series<String, Number> seriesRssi;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lineChart.setPrefWidth(root.getPrefWidth());
		lineChart.setPrefHeight(root.getPrefHeight());
		loadChart();
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Value weren't initialized");
	}
	
	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		csv = new CSVImpl(Environment.getRunningEnvLogDirectory() + Environment.DIR_SEPERATOR + "signals.csv");
		csv.open(Arrays.asList("Time", "distance", "signal", "noise", "rssi"));
		
		drone.addDroneListener(this);
	}

	private void addValues(int distance, int signal, int noise, int rssi) {
		Platform.runLater( () -> {
			String timestamp = LocalDateTime.now().toLocalTime().toString();
			
			if (seriesDistance != null)
				seriesDistance.getData().add(new XYChart.Data<String, Number>(timestamp, distance));
			if (seriesSignal != null)
				seriesSignal.getData().add(new XYChart.Data<String, Number>(timestamp, signal));
			if (seriesNoise != null)
				seriesNoise.getData().add(new XYChart.Data<String, Number>(timestamp, noise));
			if (seriesRssi != null)
				seriesRssi.getData().add(new XYChart.Data<String, Number>(timestamp, rssi));
			
			csv.addEntry(Arrays.asList(timestamp, distance, signal, noise, rssi));
		});
	}

	private void loadChart() {
        seriesDistance = new XYChart.Series<String, Number>();
        seriesDistance.setName("Distance (m)");
		lineChart.getData().add(seriesDistance);
		
		seriesSignal = new XYChart.Series<String, Number>();
		seriesSignal.setName("Signal (%)");
		lineChart.getData().add(seriesSignal);
		
		seriesRssi = new XYChart.Series<String, Number>();
		seriesRssi.setName("Rssi (%)");
		lineChart.getData().add(seriesRssi);
		
		seriesNoise = new XYChart.Series<String, Number>();
		seriesNoise.setName("Noise (%)");
		lineChart.getData().add(seriesNoise);
	}
	
	private int valueUpdate = 0;

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
			case RADIO:
				valueUpdate++;
				break;
			case GPS:
				valueUpdate++;
				break;
		}
		
		if (valueUpdate == 2) {
			int GpsdistanceFromHome = (int) (drone.getHome() == null ? 0 : drone.getHome().getDroneDistanceToHome());
			int RadiosignalStrength = drone.getRadio().getSignalStrength();
			int RadionoiseStrength = (int) drone.getRadio().getNoise();
			int RadioRssiStrength = (int) drone.getRadio().getRssi();
			addValues(GpsdistanceFromHome, RadiosignalStrength, RadionoiseStrength, RadioRssiStrength);
			valueUpdate = 0;
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
