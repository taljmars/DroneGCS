package com.dronegcs.console.controllers.internalFrames;

import com.dronegcs.console_plugin.services.internal.logevents.QuadGuiEvent;
import com.dronegcs.mavlink.is.drone.Drone;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.DroneEventsType;
import com.dronegcs.mavlink.is.drone.DroneInterfaces.OnDroneListener;
import com.dronegcs.mavlink.is.drone.variables.Battery;
import com.generic_tools.csv.CSV;
import com.generic_tools.csv.internal.CSVImpl;
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
public class InternalFrameBattery extends Pane implements OnDroneListener, Initializable {

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
	private static XYChart.Series<String, Number> seriesCurrent;
	private static XYChart.Series<String, Number> seriesDischarge;
	private static XYChart.Series<String, Number> seriesRemain;
	private static XYChart.Series<String, Number> seriesVolt;
	
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
		
		//csv = new CSVImpl(Environment.getRunningEnvLogDirectory() + Environment.DIR_SEPERATOR + "battery.csv");
		csv = new CSVImpl(environment.getRunningEnvLogDirectory() + File.separator + "battery.csv");
		csv.open(Arrays.asList("Time", "Current", "Discharge/1000", "Remain", "Volt"));
		
		drone.addDroneListener(this);
	}

	private void addBatteyInfo(double battCurrent, double battDischarge, double battRemain, double battVolt) {
		Platform.runLater( () -> {
			String timestamp = LocalDateTime.now().toLocalTime().toString();
			
			if (seriesCurrent != null)
				seriesCurrent.getData().add(new XYChart.Data<String, Number>(timestamp, battCurrent));
			if (seriesDischarge != null)
				seriesDischarge.getData().add(new XYChart.Data<String, Number>(timestamp, battDischarge/1000));
			if (seriesRemain != null)
				seriesRemain.getData().add(new XYChart.Data<String, Number>(timestamp, battRemain));
			if (seriesVolt != null)
				seriesVolt.getData().add(new XYChart.Data<String, Number>(timestamp, battVolt));
			
			csv.addEntry(Arrays.asList(timestamp, battCurrent, battDischarge/1000, battRemain, battVolt));
		});
	}

	private void loadChart() {
		seriesCurrent = new XYChart.Series<String, Number>();
		seriesCurrent.setName("Current");
		lineChart.getData().add(seriesCurrent);
		
		seriesDischarge = new XYChart.Series<String, Number>();
		seriesDischarge.setName("Discharge");
		lineChart.getData().add(seriesDischarge);
		
		seriesRemain = new XYChart.Series<String, Number>();
		seriesRemain.setName("Remain");
		lineChart.getData().add(seriesRemain);
		
		seriesVolt = new XYChart.Series<String, Number>();
		seriesVolt.setName("Volt");
		lineChart.getData().add(seriesVolt);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case BATTERY:
			Battery bat = drone.getBattery();
			addBatteyInfo(bat.getBattCurrent(), bat.getBattDischarge().doubleValue(), bat.getBattRemain(), bat.getBattVolt());
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
