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

@Component("internalFrameSignals")
public class InternalFrameSignals extends Pane implements OnDroneListener {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "drone")
	private Drone drone;
	
	private CSV csv;

	/** The time series data. */
	private static XYChart.Series<String, Number> seriesDistance;
	private static XYChart.Series<String, Number> seriesSignal;
	private static XYChart.Series<String, Number> seriesNoise;
	private static XYChart.Series<String, Number> seriesRssi;
	
	@Autowired
	public InternalFrameSignals(@Value("Signals") String title) {		
		loadChart();
	}
	
	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		csv = new CSVImpl(Environment.getRunningEnvDirectory() + Environment.DIR_SEPERATOR + "signals.csv");
		csv.open(Arrays.asList("distance", "signal", "noise", "rssi"));
		
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
			
			csv.addEntry(Arrays.asList(distance, signal, noise, rssi));
		});
	}

	private void loadChart() {
		CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
		LineChart<String,Number> lineChart = new LineChart<String,Number>(xAxis,yAxis);
       
        lineChart.setTitle("Signals");

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

		
		lineChart.setPickOnBounds(true);
		lineChart.prefWidthProperty().bind(widthProperty());
        
		getChildren().add(lineChart);
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
			int GpsdistanceFromHome = (int) (drone.getHome() == null ? 0 : drone.getHome().getDroneDistanceToHome().valueInMeters());
			int RadiosignalStrength = drone.getRadio().getSignalStrength();
			int RadionoiseStrength = (int) drone.getRadio().getNoise();
			int RadioRssiStrength = (int) drone.getRadio().getRssi();
			addValues(GpsdistanceFromHome, RadiosignalStrength, RadionoiseStrength, RadioRssiStrength);
			valueUpdate = 0;
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
