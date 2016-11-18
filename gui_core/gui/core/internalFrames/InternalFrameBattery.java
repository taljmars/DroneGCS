package gui.core.internalFrames;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import mavlink.is.drone.variables.Battery;
import tools.csv.CSV;
import tools.csv.internal.CSVImpl;
import tools.os_utilities.Environment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import gui.is.events.GuiEvent;
import javafx.application.Platform;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;

@Component("internalFrameBattery")
public class InternalFrameBattery extends Pane implements OnDroneListener {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "drone")
	private Drone drone;
	
	private CSV csv;

	/** The time series data. */
	private static XYChart.Series<String, Number> seriesCurrent;
	private static XYChart.Series<String, Number> seriesDischarge;
	private static XYChart.Series<String, Number> seriesRemain;
	private static XYChart.Series<String, Number> seriesVolt;
	
	@Autowired
	public InternalFrameBattery(@Value("Battery") String title) {		
		loadChart();
	}
	
	
	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		csv = new CSVImpl(Environment.getRunningEnvDirectory() + Environment.DIR_SEPERATOR + "battery.csv");
		csv.open(Arrays.asList("Time", "Current", "Discharge", "Remain", "Volt"));
		
		drone.addDroneListener(this);
	}

	private void addBatteyInfo(double battCurrent, double battDischarge, double battRemain, double battVolt) {
		Platform.runLater( () -> {
			String timestamp = LocalDateTime.now().toLocalTime().toString();
			
			if (seriesCurrent != null)
				seriesCurrent.getData().add(new XYChart.Data<String, Number>(timestamp, battCurrent));
			if (seriesDischarge != null)
				seriesDischarge.getData().add(new XYChart.Data<String, Number>(timestamp, battDischarge));
			if (seriesRemain != null)
				seriesRemain.getData().add(new XYChart.Data<String, Number>(timestamp, battRemain));
			if (seriesVolt != null)
				seriesVolt.getData().add(new XYChart.Data<String, Number>(timestamp, battVolt));
			
			csv.addEntry(Arrays.asList(timestamp, battCurrent, battDischarge, battRemain, battVolt));
		});
	}

	private void loadChart() {
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Time");
		
        NumberAxis yAxis = new NumberAxis();
        yAxis.setUpperBound(2200);
        yAxis.setLowerBound(0);
        
		LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
       
        lineChart.setTitle("Battery");

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
		
		lineChart.prefWidthProperty().bind(widthProperty());
		
		getChildren().add(lineChart);
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
