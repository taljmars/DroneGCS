package gui.core.dashboard;

import gui.core.internalPanels.*;
import gui.core.springConfig.AppConfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;

import gui.is.events.TextNotificationPublisher;
import gui.is.services.LoggerDisplayerManager;

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
import mavlink.is.drone.DroneInterfaces.*;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;

public class Dashboard implements OnDroneListener, OnWaypointManagerListener {
	
	private JFrame frame;

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private static final String APP_TITLE = "Quad Ground Station";
	
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
					dashboard.initializeComponents();
					dashboard.refresh();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void initializeComponents() {
		System.out.println("Start Logger Displayer Manager");
		LoggerDisplayerManager.addLoggerDisplayerListener(areaLogBox);

		System.out.println("Start GCS Heartbeat");
		GCSHeartbeat gcs = new GCSHeartbeat(drone, 1);
		gcs.setActive(true);

	}

	protected void refresh() {
		System.out.println("Sign Dashboard as drone listener");
		drone.addDroneListener(this);
		drone.addDroneListener(tbTelemtry);
		drone.addDroneListener(tbContorlButton);
		drone.getWaypointManager().setWaypointManagerListener(this);

		System.out.println("Setting for button Box");
		tbContorlButton.setButtonControl(false);

		System.out.println("Setting Configurtaion");
		areaConfiguration.setDrone(drone);

		if (drone.isConnectionAlive()) {
			tbTelemtry.SetHeartBeat(true);
			drone.notifyDroneEvent(DroneEventsType.MODE);
		}
	}

	public void SetDistanceToWaypoint(double d) {
		if (drone.getState().getMode().equals(ApmModes.ROTOR_GUIDED)) {
			// if (drone.getGuidedPoint().isIdle()) {
			if (d == 0) {
				textNotificationPublisher.publish("In Position");
				LoggerDisplayerManager.addGeneralMessegeToDisplay("Guided: In Position");
			} else {
				textNotificationPublisher.publish("Flying to destination");
				LoggerDisplayerManager.addGeneralMessegeToDisplay("Guided: Fly to distination");
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
	 * loggerDisplayerManager.addGeneralMessegeToDisplay("Stopping Follow");
	 * drone.getBeacon().setActive(false); }
	 */

	public void VerifyBattery(double bat) {
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

	public void PaintAllWindow(Color c) {
		if (c == tbTelemtry.getBackground()) {
			System.out.println("Same Color");
			return;
		}
		tbTelemtry.setBackground(c);
		int cnt = tbTelemtry.getComponentCount();
		for (int i = 0; i < cnt; i++) {
			tbTelemtry.getComponent(i).setBackground(c);
			tbTelemtry.getComponent(i).repaint();
		}
		tbTelemtry.repaint();

	}
	
	@Override
	public void onBeginWaypointEvent(WaypointEvent_Type wpEvent) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			LoggerDisplayerManager.addIncommingMessegeToDisplay("Start Syncing");
			return;
		}
		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			LoggerDisplayerManager.addIncommingMessegeToDisplay("Start Updloading Waypoints");
			return;
		}

		LoggerDisplayerManager.addIncommingMessegeToDisplay("Failed to Start Syncing (" + wpEvent.name() + ")");
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			LoggerDisplayerManager.addIncommingMessegeToDisplay("Downloading Waypoint " + index + "/" + count);
			setProgressBar(0, index, count);
			return;
		}

		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			LoggerDisplayerManager.addIncommingMessegeToDisplay("Uploading Waypoint " + index + "/" + count);
			setProgressBar(0, index, count);
			return;
		}

		LoggerDisplayerManager.addErrorMessegeToDisplay("Unexpected Syncing Failure (" + wpEvent.name() + ")");
		setProgressBar(count, count);
	}

	@Override
	public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD)) {
			LoggerDisplayerManager.addIncommingMessegeToDisplay("Waypoints Synced");
			if (drone.getMission() == null) {
				LoggerDisplayerManager.addIncommingMessegeToDisplay("Failed to find mission");
				return;
			}

			LoggerDisplayerManager.addIncommingMessegeToDisplay("Current mission was loaded to a new view");
			return;
		}

		if (wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			LoggerDisplayerManager.addIncommingMessegeToDisplay("Waypoints Synced");
			return;
		}

		LoggerDisplayerManager.addErrorMessegeToDisplay("Failed to Sync Waypoints (" + wpEvent.name() + ")");
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case LEFT_PERIMETER:
			textNotificationPublisher.publish("Outside Perimeter");
			LoggerDisplayerManager.addErrorMessegeToDisplay("Quad left the perimeter");
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		case ENFORCING_PERIMETER:
			textNotificationPublisher.publish("Enforcing Perimeter");
			LoggerDisplayerManager.addErrorMessegeToDisplay("Enforcing Perimeter");
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
		case PARAMETER:
			LoadParameter(drone.getParameters().getExpectedParameterAmount());
			return;
		case PARAMETERS_DOWNLOAD_START:
			LoggerDisplayerManager.addGeneralMessegeToDisplay("Parameters Downloading begin");
			resetProgressBar();
			return;
		case PARAMETERS_DOWNLOADED_FINISH:
			LoggerDisplayerManager.addGeneralMessegeToDisplay("Parameters Downloaded succussfully");
			return;
		case TEXT_MESSEGE:
			LoggerDisplayerManager.addIncommingMessegeToDisplay(drone.getMessegeQueue().pop());
			return;
		case WARNING_SIGNAL_WEAK:
			LoggerDisplayerManager.addErrorMessegeToDisplay("Warning: Weak signal");
			LoggerDisplayerManager.addErrorMessegeToDisplay("Warning: Weak signal");
			LoggerDisplayerManager.addErrorMessegeToDisplay("Warning: Weak signal");
			java.awt.Toolkit.getDefaultToolkit().beep();
			java.awt.Toolkit.getDefaultToolkit().beep();
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		case FOLLOW_START:
			LoggerDisplayerManager.addGeneralMessegeToDisplay("Follow Me Started");
			return;
		case FOLLOW_UPDATE:
			LoggerDisplayerManager.addGeneralMessegeToDisplay("Follow Me Updated");
			return;
		case FOLLOW_STOP:
			LoggerDisplayerManager.addGeneralMessegeToDisplay("Follow Me Ended");
			return;
		}
	}

	private void LoadParameter(int expectedParameterAmount) {
		setProgressBar(0, drone.getParameters().getLoadedDownloadedParameters(), drone.getParameters().getExpectedParameterAmount());
		int prc = (int) (((double) (drone.getParameters().getLoadedDownloadedParameters()) / drone.getParameters().getExpectedParameterAmount()) * 100);
		if (prc > 95) {
			System.out.println(getClass().getName() + " Setup stream rate");
			drone.getStreamRates().setupStreamRatesFromPref();
			tbContorlButton.setButtonControl(true);
			System.out.println(getClass().getName() + " " + drone.getParameters().getParameter("MOT_SPIN_ARMED"));
			if (drone.isConnectionAlive()) {
				tbTelemtry.SetHeartBeat(true);
				drone.notifyDroneEvent(DroneEventsType.MODE);
			}
		}
	}

	public void setProgressBar(int min, int current, int max) {
		if (!progressBar.isVisible() || progressBar.getMaximum() != max) {
			progressBar.setMinimum(min);
			progressBar.setValue(current);
			progressBar.setMaximum(max);
			progressBar.setVisible(true);
		}
		progressBar.setValue(current);

		if (progressBar.getValue() == progressBar.getMaximum()) {
			progressBar.setVisible(false);
			progressBar.setValue(0);
		}
	}

	public void setProgressBar(int min, int max) {
		setProgressBar(min, progressBar.getValue() + 1, max);
	}

	public void resetProgressBar() {
		progressBar.setValue(0);
	}
	
	@EventListener
	public void onApplicationEvent(String notification) {
		tbToolBar.SetNotification(notification);
	}
}
