package gui.core.internalFrames;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.WindowConstants;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("internalFrameSignals")
public class JInternalFrameSignals extends AbstractJInternalFrame implements OnDroneListener {

	private static final long serialVersionUID = 1L;
	
	@Resource(name = "drone")
	private Drone drone;

	/** The time series data. */
	private static TimeSeries seriesDistance;
	private static TimeSeries seriesSignal;
	private static TimeSeries seriesNoise;
	private static TimeSeries seriesRssi;
	
	@Autowired
	public JInternalFrameSignals(@Value("Signals") String title) {
		this(title, true, true, true, true);
	}
	
	private JInternalFrameSignals(String name, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable) {
		super(name, resizable, closable, maximizable, iconifiable);
		loadChart();
		
		setBounds(25, 25, 800, 400);
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		drone.addDroneListener(this);
	}
	
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createTimeSeriesChart("", "", "Signal and Distance over Time", dataset, true, true, false);
		final XYPlot plot = result.getXYPlot();
		ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		axis.setFixedAutoRange(120000.0); // 120 seconds
		axis = plot.getRangeAxis();
		axis.setRange(-20, 200);
		return result;
	}

	private void addValues(int distance, int signal, int noise, int rssi) {
		if (seriesDistance != null)
			seriesDistance.add(new Millisecond(), distance);
		if (seriesSignal != null)
			seriesSignal.add(new Millisecond(), signal);
		if (seriesNoise != null)
			seriesNoise.add(new Millisecond(), noise);
		if (seriesRssi != null)
			seriesRssi.add(new Millisecond(), rssi);
	}

	@SuppressWarnings("deprecation")
	private void loadChart() {
		final TimeSeriesCollection dataset = new TimeSeriesCollection();

		seriesDistance = new TimeSeries("Distance(m)", Millisecond.class);
		dataset.addSeries(seriesDistance);
		seriesSignal = new TimeSeries("Signal(%)", Millisecond.class);
		dataset.addSeries(seriesSignal);
		seriesRssi = new TimeSeries("Rssi(%)", Millisecond.class);
		dataset.addSeries(seriesRssi);
		seriesNoise = new TimeSeries("Noise(%)", Millisecond.class);
		dataset.addSeries(seriesNoise);

		final JFreeChart chart = createChart(dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseWheelEnabled(true);

		chartPanel.setPreferredSize(new Dimension(600, 200));
		FlowLayout fl_chartPanel = new FlowLayout(FlowLayout.LEFT, 5, 5);
		chartPanel.setLayout(fl_chartPanel);

		setContentPane(chartPanel);
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
}
