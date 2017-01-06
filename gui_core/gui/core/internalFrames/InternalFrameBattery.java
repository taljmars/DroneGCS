package gui.core.internalFrames;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.variables.Battery;
import tools.csv.CSV;
import tools.csv.internal.CSVImpl;
import tools.os_utilities.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import gui.is.events.GuiEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;

@Component("internalFrameBattery")
public class InternalFrameBattery extends Pane implements OnDroneListener, Initializable {

	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;
	
	private CSV csv;
	
	@FXML private LineChart<String,Number> lineChart;

	/** The time series data. */
	private static XYChart.Series<String, Number> seriesCurrent;
	private static XYChart.Series<String, Number> seriesDischarge;
	private static XYChart.Series<String, Number> seriesRemain;
	private static XYChart.Series<String, Number> seriesVolt;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadChart();
	}
	
	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		csv = new CSVImpl(Environment.getRunningEnvDirectory() + Environment.DIR_SEPERATOR + "battery.csv");
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
	public void onApplicationEvent(GuiEvent command) {
		switch (command.getCommand()) {
		case EXIT:
			if (csv != null) 
				csv.close();
			break;
		}
	}
	
}
