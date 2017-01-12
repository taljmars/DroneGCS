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
import objects.csv.CSV;
import objects.csv.internal.CSVImpl;

@Component("internalFrameActualPWM")
public class InternalFrameActualPWM extends Pane implements OnDroneListener, Initializable {

	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;
	
	@NotNull @FXML private Pane root;
	@NotNull @FXML private LineChart<String,Number> lineChart;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	private CSV csv;

	/** The time series data. */
	private static XYChart.Series<String, Number> seriesE1;
	private static XYChart.Series<String, Number> seriesE2;
	private static XYChart.Series<String, Number> seriesE3;
	private static XYChart.Series<String, Number> seriesE4;
			
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
		
		csv = new CSVImpl(Environment.getRunningEnvLogDirectory() + Environment.DIR_SEPERATOR + "actualPWM.csv");
		csv.open(Arrays.asList("Time", "E1", "E2", "E3", "E4"));
		
		drone.addDroneListener(this);
	}

	private void addRCActual(int e1, int e2, int e3, int e4) {
		Platform.runLater( () -> {
			String timestamp = LocalDateTime.now().toLocalTime().toString();
			
			if (seriesE1 != null)
				seriesE1.getData().add(new XYChart.Data<String, Number>(timestamp, e1));
			if (seriesE2 != null)
				seriesE2.getData().add(new XYChart.Data<String, Number>(timestamp, e2));
			if (seriesE3 != null)
				seriesE3.getData().add(new XYChart.Data<String, Number>(timestamp, e3));
			if (seriesE4 != null)
				seriesE4.getData().add(new XYChart.Data<String, Number>(timestamp, e4));
			
			csv.addEntry(Arrays.asList(timestamp, e1, e2, e3, e4));
		});
	}

	private void loadChart() {
		seriesE1 = new XYChart.Series<String, Number>();
		seriesE1.setName("E1");
		lineChart.getData().add(seriesE1);
		
		seriesE2 = new XYChart.Series<String, Number>();
		seriesE2.setName("E2");
		lineChart.getData().add(seriesE2);
		
		seriesE3 = new XYChart.Series<String, Number>();
		seriesE3.setName("E3");
		lineChart.getData().add(seriesE3);
		
		seriesE4 = new XYChart.Series<String, Number>();
		seriesE4.setName("E4");
		lineChart.getData().add(seriesE4);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case RC_OUT:
			addRCActual(drone.getRC().out[0], drone.getRC().out[1], drone.getRC().out[2], drone.getRC().out[3]);
			return;
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
