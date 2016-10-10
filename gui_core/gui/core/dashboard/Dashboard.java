package gui.core.dashboard;

import gui.core.internalPanels.*;
import gui.core.springConfig.AppConfig;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.util.List;

import gui.is.services.LoggerDisplayerSvc;
import gui.is.services.TextNotificationPublisher;

import javax.annotation.Resource;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JDesktopPane;

import org.springframework.context.event.EventListener;

import mavlink.core.gcs.GCSHeartbeat;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.OnParameterManagerListener;
import mavlink.is.drone.DroneInterfaces.*;
import mavlink.is.drone.parameters.Parameter;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;

public class Dashboard implements OnDroneListener, OnWaypointManagerListener, OnParameterManagerListener {
	
	private JFrame frame;

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private static final String APP_TITLE = "Quad Ground Station";
	
	@Resource(name = "loggerDisplayerSvc")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "drone")
	public Drone drone;
	
	@Resource(name="frameContainer")
	private JDesktopPane frameContainer;
	
	@Resource(name="areaLogBox")
	private JPanelLogBox areaLogBox;

	@Resource(name="areaMission")
	private JPanelMissionBox areaMission;
	
	@Resource(name="areaConfiguration")
	private JPanelConfigurationBox areaConfiguration;
	
	@Resource(name="telemetrySatellite")
	private JPanelTelemetrySatellite tbTelemtry;
	
	@Resource(name="buttonBoxSatellite")
	private JPanelButtonBoxSatellite tbContorlButton;
	
	@Resource(name="toolbarSatellite")
	private JPanelToolBarSatellite tbToolBar;
	
	@Resource(name = "textNotificationPublisher")
	private TextNotificationPublisher textNotificationPublisher;
	
	@Resource(name = "gcsHeartbeat")
	public GCSHeartbeat gcsHeartbeat;
	
	private JTabbedPane tbSouth;
	private JToolBar toolBar;
	private JProgressBar progressBar;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.out.println("Start Dashboard");
					Dashboard dashboard = (Dashboard) AppConfig.context.getBean("dashboard");
					dashboard.initializeGui();
					dashboard.refresh();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void refresh() {		
		System.out.println("Sign Dashboard as drone listener");
		drone.addDroneListener(this);
		drone.addDroneListener(tbTelemtry);
		drone.addDroneListener(tbContorlButton);
		drone.addDroneListener(tbToolBar);
		drone.getWaypointManager().setWaypointManagerListener(this);
		drone.getParameters().setParameterListener(this);
		
		System.out.println("Start GCS Heartbeat");
		gcsHeartbeat.setFrq(1);
		gcsHeartbeat.setActive(true);

		System.out.println("Setting for button Box");
		tbContorlButton.setButtonControl(false);

		System.out.println("Setting Configurtaion");
		areaConfiguration.setDrone(drone);

		if (drone.isConnectionAlive()) {
			tbTelemtry.SetHeartBeat(true);
			drone.notifyDroneEvent(DroneEventsType.MODE);
		}
	}

	private void SetDistanceToWaypoint(double d) {
		if (drone.getState().getMode().equals(ApmModes.ROTOR_GUIDED)) {
			// if (drone.getGuidedPoint().isIdle()) {
			if (d == 0) {
				textNotificationPublisher.publish("In Position");
				loggerDisplayerSvc.logGeneral("Guided: In Position");
			} else {
				textNotificationPublisher.publish("Flying to destination");
				loggerDisplayerSvc.logGeneral("Guided: Fly to distination");
			}
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initializeGui() {
		frame = new JFrame(APP_TITLE);
		frame.setBounds(100, 100, 450, 300);
		frame.setSize(400, 400);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		// Central Panel
		frame.getContentPane().add(frameContainer, BorderLayout.CENTER);

		// South Panel
		JPanel southPanel = new JPanel(new BorderLayout());
		frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		southPanel.add(progressBar, BorderLayout.SOUTH);

		// South Panel
		toolBar = new JToolBar();
		southPanel.add(toolBar, BorderLayout.CENTER);
		tbSouth = new JTabbedPane(JTabbedPane.TOP);
		toolBar.add(tbSouth);
		
		tbSouth.addTab("Log Book", null, areaLogBox, null);
		tbSouth.addTab("Configuration", null, areaConfiguration, null);
		tbSouth.addTab("Mission", null, areaMission, null);
		
		// North Panel
		frame.getContentPane().add(tbToolBar, BorderLayout.NORTH);

		// East Panel
		JPanel eastPanel = new JPanel(new GridLayout(3, 1, 5, 5));
		frame.getContentPane().add(eastPanel, BorderLayout.EAST);
		eastPanel.add(tbContorlButton);

		// East Panel
		eastPanel.add(tbTelemtry);

		frame.setVisible(true);
	}

	/*
	 * private void ActivateBeacon() throws InterruptedException { int delay =
	 * 5; addGeneralMessegeToDisplay("Start following beacon in ..."); while
	 * (delay > 0) { addGeneralMessegeToDisplay("" + delay); Thread.sleep(1000);
	 * delay--; } addGeneralMessegeToDisplay("Go");
	 * drone.getBeacon().setActive(true); }
	 */

	/*
	 * private void DeactivateBeacon() {
	 * loggerDisplayerSvc.logGeneral("Stopping Follow");
	 * drone.getBeacon().setActive(false); }
	 */

	private void VerifyBattery(double bat) {
		// final Color orig_color = lblBattery.getBackground();
		if (drone.getState().isFlying() && bat < 100) {
			java.awt.Toolkit.getDefaultToolkit().beep();
			// PaintAllWindow(Color.RED);
			textNotificationPublisher.publish("Low Battery");
		} else {
			// lblBattery.setForeground(Color.BLACK);
			// Color c = new Color(238, 238 ,238);
			// PaintAllWindow(orig_color);
		}
		// lblBattery.setText((bat < 0 ? 0 : bat) + "%");
	}

//	private void PaintAllWindow(Color c) {
//		if (c == tbTelemtry.getBackground()) {
//			System.out.println("Same Color");
//			return;
//		}
//		tbTelemtry.setBackground(c);
//		int cnt = tbTelemtry.getComponentCount();
//		for (int i = 0; i < cnt; i++) {
//			tbTelemtry.getComponent(i).setBackground(c);
//			tbTelemtry.getComponent(i).repaint();
//		}
//		tbTelemtry.repaint();
//
//	}
	
	@Override
	public void onBeginWaypointEvent(WaypointEvent_Type wpEvent) {
		initProgressBar();
		
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Start Syncing");
			return;
		}
		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Start Updloading Waypoints");
			return;
		}

		System.out.println("Failed to Start Syncing (" + wpEvent.name() + ")");
		loggerDisplayerSvc.logError("Failed to Start Syncing (" + wpEvent.name() + ")");
		
		finiProgressBar();
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Downloading Waypoint " + index + "/" + count);
			incProgressBar(count);
			return;
		}

		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Uploading Waypoint " + index + "/" + count);
			incProgressBar(count);
			return;
		}

		System.out.println("Unexpected Syncing Failure (" + wpEvent.name() + ")");
		loggerDisplayerSvc.logError("Unexpected Syncing Failure (" + wpEvent.name() + ")");
		finiProgressBar();
	}

	@Override
	public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			loggerDisplayerSvc.logIncoming("Waypoints Synced");
			if (drone.getMission() == null) {
				loggerDisplayerSvc.logError("Failed to find mission");
				return;
			}

			loggerDisplayerSvc.logGeneral("Current mission was loaded to a new view");
			return;
		}

		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			loggerDisplayerSvc.logIncoming("Waypoints Synced");
			return;
		}

		System.out.println("Failed to Sync Waypoints (" + wpEvent.name() + ")");
		loggerDisplayerSvc.logError("Failed to Sync Waypoints (" + wpEvent.name() + ")");
		finiProgressBar();
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case LEFT_PERIMETER:
			textNotificationPublisher.publish("Outside Perimeter");
			loggerDisplayerSvc.logError("Quad left the perimeter");
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		case ENFORCING_PERIMETER:
			textNotificationPublisher.publish("Enforcing Perimeter");
			loggerDisplayerSvc.logError("Enforcing Perimeter");
			return;
		case ORIENTATION:
			SetDistanceToWaypoint(drone.getMissionStats().getDistanceToWP().valueInMeters());
			return;
		case BATTERY:
			VerifyBattery(drone.getBattery().getBattRemain());
			return;
		case MODE:
			frame.setTitle(APP_TITLE + " (" + drone.getState().getMode().getName() + ")");
			return;
		case TEXT_MESSEGE:
			loggerDisplayerSvc.logIncoming(drone.getMessegeQueue().pop());
			return;
		case WARNING_SIGNAL_WEAK:
			loggerDisplayerSvc.logError("Warning: Weak signal");
			loggerDisplayerSvc.logError("Warning: Weak signal");
			loggerDisplayerSvc.logError("Warning: Weak signal");
			java.awt.Toolkit.getDefaultToolkit().beep();
			java.awt.Toolkit.getDefaultToolkit().beep();
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		case FOLLOW_START:
			loggerDisplayerSvc.logGeneral("Follow Me Started");
			return;
		case FOLLOW_UPDATE:
			loggerDisplayerSvc.logGeneral("Follow Me Updated");
			return;
		case FOLLOW_STOP:
			loggerDisplayerSvc.logGeneral("Follow Me Ended");
			return;
		}
	}

	public synchronized void initProgressBar() {
		System.out.println("Init progress bar");
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setVisible(true);
	}
	
	public synchronized void incProgressBar(int max) {
		int value = progressBar.getValue();
		if (progressBar.getMaximum() != max) {
			progressBar.setMaximum(max);
		}
		progressBar.setValue(value + 1);

		if (progressBar.getValue() == progressBar.getMaximum()) {
			finiProgressBar();
		}
	}
	
	public synchronized void finiProgressBar() {
		System.out.println("Fini progress bar");
		progressBar.setValue(progressBar.getMaximum());
		progressBar.setVisible(false);
	}
	
	@EventListener
	public void onApplicationEvent(String notification) {
		tbToolBar.SetNotification(notification);
	}

	@Override
	public void onBeginReceivingParameters() {
		System.out.println("Start Receiving parameters");
		initProgressBar();
		drone.getStreamRates().prepareStreamRates();
	}

	@Override
	public void onParameterReceived(Parameter parameter, int index, int count) {
		System.out.println("received paramter " + index + " out of " + count);
		incProgressBar(count);
		int prc = drone.getParameters().getPrecentageComplete();
		if (prc > 95) {
			drone.getStreamRates().setupStreamRatesFromPref();
			tbContorlButton.setButtonControl(true);
			if (drone.isConnectionAlive()) {
				tbTelemtry.SetHeartBeat(true);
				drone.notifyDroneEvent(DroneEventsType.MODE);
			}
		}
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> parameter) {
		System.out.println("Finish receiving parameters");
		finiProgressBar();
		
		drone.getStreamRates().prepareStreamRates();
		drone.getStreamRates().setupStreamRatesFromPref();
		tbContorlButton.setButtonControl(true);
		if (drone.isConnectionAlive()) {
			tbTelemtry.SetHeartBeat(true);
			drone.notifyDroneEvent(DroneEventsType.MODE);
		}
	}
}
