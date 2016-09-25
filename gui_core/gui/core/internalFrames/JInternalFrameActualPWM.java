package gui.core.internalFrames;

import java.awt.Dimension;
import java.awt.FlowLayout;

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

public class JInternalFrameActualPWM extends AbstractJInternalFrame implements OnDroneListener {

	private static final long serialVersionUID = 1L;
	private static final String frameName = "Actual PWM";

	/** The time series data. */
	private static TimeSeries seriesRoll;
	private static TimeSeries seriesPitch;
	private static TimeSeries seriesThr;
	private static TimeSeries seriesYaw;
	
	private JInternalFrameActualPWM(String name, boolean resizable, boolean closable,
			boolean maximizable, boolean iconifiable) {
		super(name, resizable, closable, maximizable, iconifiable);
		loadChart();
		
		setBounds(25, 25, 800, 400);
	}

	private JInternalFrameActualPWM(String name) {
		this(name, true, true, true, true);
	}
	
	public JInternalFrameActualPWM() {
		this(frameName);
	}
	
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createTimeSeriesChart("", "", "PWM", dataset, true, true, false);
		final XYPlot plot = result.getXYPlot();
		ValueAxis axis = plot.getDomainAxis();
		axis.setAutoRange(true);
		axis.setFixedAutoRange(60000.0); // 60 seconds
		axis = plot.getRangeAxis();
		axis.setRange(0.0, 2500);
		return result;
	}

	public void addRCActual(int roll, int pitch, int thr, int yaw) {
		if (seriesRoll != null)
			seriesRoll.add(new Millisecond(), roll);
		if (seriesPitch != null)
			seriesPitch.add(new Millisecond(), pitch);
		if (seriesThr != null)
			seriesThr.add(new Millisecond(), thr);
		if (seriesYaw != null)
			seriesYaw.add(new Millisecond(), yaw);
	}

	@SuppressWarnings("deprecation")
	public void loadChart() {
		final TimeSeriesCollection dataset = new TimeSeriesCollection();

		seriesRoll = new TimeSeries("E1", Millisecond.class);
		dataset.addSeries(seriesRoll);
		seriesPitch = new TimeSeries("E2", Millisecond.class);
		dataset.addSeries(seriesPitch);
		seriesThr = new TimeSeries("E3", Millisecond.class);
		dataset.addSeries(seriesThr);
		seriesYaw = new TimeSeries("E4", Millisecond.class);
		dataset.addSeries(seriesYaw);

		final JFreeChart chart = createChart(dataset);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setMouseWheelEnabled(true);

		chartPanel.setPreferredSize(new Dimension(600, 200));
		FlowLayout fl_chartPanel = new FlowLayout(FlowLayout.LEFT, 5, 5);
		chartPanel.setLayout(fl_chartPanel);

		setContentPane(chartPanel);
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

	public static AbstractJInternalFrame generateMyOwn() {
		return new JInternalFrameActualPWM();
	}
}
