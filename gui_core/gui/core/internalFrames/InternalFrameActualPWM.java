package gui.core.internalFrames;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
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

@Component("internalFrameActualPWM")
public class InternalFrameActualPWM extends Pane implements OnDroneListener {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "drone")
	private Drone drone;
	
	private CSV csv;

	/** The time series data. */
	private static XYChart.Series<String, Number> seriesRoll;
	private static XYChart.Series<String, Number> seriesPitch;
	private static XYChart.Series<String, Number> seriesThr;
	private static XYChart.Series<String, Number> seriesYaw;
	
	@Autowired
	public InternalFrameActualPWM(@Value("Actual PWM") String title) {		
		loadChart();
	}
	
	
	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		csv = new CSVImpl(Environment.getRunningEnvDirectory() + Environment.DIR_SEPERATOR + "actualPWM.csv");
		csv.open(Arrays.asList("Time", "Roll", "Pitch", "Thrust", "Yaw"));
		
		drone.addDroneListener(this);
	}

	private void addRCActual(int roll, int pitch, int thr, int yaw) {
		Platform.runLater( () -> {
			String timestamp = LocalDateTime.now().toLocalTime().toString();
			
			if (seriesRoll != null)
				seriesRoll.getData().add(new XYChart.Data<String, Number>(timestamp, roll));
			if (seriesPitch != null)
				seriesPitch.getData().add(new XYChart.Data<String, Number>(timestamp, pitch));
			if (seriesThr != null)
				seriesThr.getData().add(new XYChart.Data<String, Number>(timestamp, thr));
			if (seriesYaw != null)
				seriesYaw.getData().add(new XYChart.Data<String, Number>(timestamp, yaw));
			
			csv.addEntry(Arrays.asList(timestamp, roll, pitch, thr, yaw));
		});
	}

	private void loadChart() {
		CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Time");
		
        NumberAxis yAxis = new NumberAxis();
        yAxis.setUpperBound(2200);
        yAxis.setLowerBound(0);
        
		LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
       
        lineChart.setTitle("PWM");

		seriesRoll = new XYChart.Series<String, Number>();
		seriesRoll.setName("E1");
		lineChart.getData().add(seriesRoll);
		
		seriesPitch = new XYChart.Series<String, Number>();
		seriesPitch.setName("E2");
		lineChart.getData().add(seriesPitch);
		
		seriesThr = new XYChart.Series<String, Number>();
		seriesThr.setName("E3");
		lineChart.getData().add(seriesThr);
		
		seriesYaw = new XYChart.Series<String, Number>();
		seriesYaw.setName("E4");
		lineChart.getData().add(seriesYaw);
		
		lineChart.prefWidthProperty().bind(widthProperty());
		
		getChildren().add(lineChart);
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
	public void onApplicationEvent(GuiEvent command) {
		switch (command.getCommand()) {
		case EXIT:
			if (csv != null) 
				csv.close();
			break;
		}
	}
	
}
