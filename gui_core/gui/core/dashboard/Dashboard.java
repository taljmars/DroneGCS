package gui.core.dashboard;

import gui.core.internalFrames.*;
import gui.core.internalPanels.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Timer;

import flight_controlers.KeyBoardControl;
import gui.is.NotificationsHandler;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JDesktopPane;

import mavlink.core.connection.RadioConnection;
import mavlink.core.drone.ClockImpl;
import mavlink.core.drone.PreferencesFactory;
import mavlink.core.drone.HandlerImpl;
import mavlink.core.drone.MyDroneImpl;
import mavlink.core.gcs.GCSHeartbeat;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.*;
import mavlink.is.protocol.msg_metadata.ApmModes;

public class Dashboard implements OnDroneListener, NotificationsHandler {

	private JFrame frame;

	private RadioConnection radConn;
	public static Drone drone;

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private static final int LOG_BOX_MAX_LINES = 50;// 7;
	private static final String APP_TITLE = "Quad Ground Station";

	public static Dashboard window = null;

	private JPanelTelemetrySatellite tbTelemtry;
	private JPanelButtonBoxSatellite tbContorlButton;
	private JPanelToolBarSatellite tbToolBar;
	private JDesktopPane desktopPane;
	private JTabbedPane tbSouth;
	private JToolBar toolBar;

	private JProgressBar progressBar;

	private JPanelMissionBox areaMission = null;
	private JPanelConfigurationBox areaConfiguration = null;
	private JPanelLogBox areaLogBox;

	public static LoggerDisplayerManager loggerDisplayerManager = null;
	public static NotificationManager notificationManager = null;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.out.println("Start Dashboard");
					window = new Dashboard();
					window.initializeGui();
					window.initializeComponents();
					window.refresh();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void initializeComponents() {
		System.out.println("Start Logger Displayer Manager");
		loggerDisplayerManager = new LoggerDisplayerManager(areaLogBox,
				LOG_BOX_MAX_LINES);

		System.out.println("Start Outgoing Communication");
		radConn = new RadioConnection();
		radConn.connect();

		HandlerImpl handler = new mavlink.core.drone.HandlerImpl();
		drone = new MyDroneImpl(radConn, new ClockImpl(), handler,
				PreferencesFactory.getPreferences());

		System.out.println("Start Notifications Manager");
		Timer timer = new Timer();
		notificationManager = new NotificationManager(window);
		timer.scheduleAtFixedRate(notificationManager, 0,
				NotificationManager.MSG_CHECK_PERIOD);

		System.out.println("Start GCS Heartbeat");
		GCSHeartbeat gcs = new GCSHeartbeat(drone, 1);
		gcs.setActive(true);

		System.out.println("Start Keyboard Stabilizer");
		KeyBoardControl.get();
	}

	protected void refresh() {
		System.out.println("Sign Dashboard as drone listener");
		drone.addDroneListener(window);
		drone.addDroneListener(tbTelemtry);
		drone.addDroneListener(tbContorlButton);

		System.out.println("Setting for button Box");
		tbContorlButton.setDrone(drone);
		tbContorlButton.setLoggerDisplayerManager(loggerDisplayerManager);
		tbContorlButton.setNotificationsManager(notificationManager);
		tbContorlButton.setButtonControl(false);

		System.out.println("Setting for telemetry");
		tbTelemtry.setLoggerDisplayerManager(loggerDisplayerManager);

		System.out.println("Setting Configurtaion");
		areaConfiguration.setDrone(drone);

		JInternalFrameMap.Generate(desktopPane, areaMission, areaConfiguration);
		if (drone.isConnectionAlive()) {
			tbTelemtry.SetHeartBeat(true);
			// SetFlightModeLabel(drone.getState().getMode().getName());
			drone.notifyDroneEvent(DroneEventsType.MODE);
		}
	}

	public void SetDistanceToWaypoint(double d) {
		if (drone.getState().getMode().equals(ApmModes.ROTOR_GUIDED)) {
			// if (drone.getGuidedPoint().isIdle()) {
			if (d == 0) {
				notificationManager.add("In Position");
				loggerDisplayerManager
						.addGeneralMessegeToDisplay("Guided: In Position");
			} else {
				notificationManager.add("Flying to destination");
				loggerDisplayerManager
						.addGeneralMessegeToDisplay("Guided: Fly to distination");
			}
		}
	}

	/**
	 * Create the application.
	 */
	public Dashboard() {
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
		desktopPane = new JDesktopPane();
		frame.getContentPane().add(desktopPane, BorderLayout.CENTER);

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
		Dimension southPanelDimension = new Dimension(1200, 150);
		areaLogBox = new JPanelLogBox(new GridBagLayout());
		tbSouth.addTab("Log Book", null, areaLogBox, null);
		areaConfiguration = new JPanelConfigurationBox(new JPanel(),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, southPanelDimension);
		tbSouth.addTab("Configuration", null, areaConfiguration, null);
		areaMission = new JPanelMissionBox(null,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, southPanelDimension);
		tbSouth.addTab("Mission", null, areaMission, null);

		// North Panel
		tbToolBar = new JPanelToolBarSatellite(desktopPane, areaMission,
				areaConfiguration);
		frame.getContentPane().add(tbToolBar, BorderLayout.NORTH);

		// East Panel
		JPanel eastPanel = new JPanel(new GridLayout(3, 1, 5, 5));
		frame.getContentPane().add(eastPanel, BorderLayout.EAST);
		tbContorlButton = new JPanelButtonBoxSatellite(new GridLayout(0, 2, 1,
				1));
		eastPanel.add(tbContorlButton);

		// East Panel
		tbTelemtry = new JPanelTelemetrySatellite();
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
			notificationManager.add("Low Battery");
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

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case LEFT_PERIMETER:
			notificationManager.add("Outside Perimeter");
			loggerDisplayerManager
					.addErrorMessegeToDisplay("Quad left the perimeter");
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		case ENFORCING_PERIMETER:
			notificationManager.add("Enforcing Perimeter");
			loggerDisplayerManager
					.addErrorMessegeToDisplay("Enforcing Perimeter");
			return;
		case ORIENTATION:
			SetDistanceToWaypoint(drone.getMissionStats().getDistanceToWP()
					.valueInMeters());
			return;
		case BATTERY:
			VerifyBattery(drone.getBattery().getBattRemain());
			return;
		case MODE:
			frame.setTitle(APP_TITLE + " ("
					+ drone.getState().getMode().getName() + ")");
			return;
		case PARAMETER:
			LoadParameter(drone.getParameters().getExpectedParameterAmount());
			return;
		case PARAMETERS_DOWNLOAD_START:
			loggerDisplayerManager
					.addGeneralMessegeToDisplay("Parameters Downloading begin");
			resetProgressBar();
			return;
		case PARAMETERS_DOWNLOADED_FINISH:
			loggerDisplayerManager
					.addGeneralMessegeToDisplay("Parameters Downloaded succussfully");
			return;
		case TEXT_MESSEGE:
			loggerDisplayerManager.addIncommingMessegeToDisplay(drone
					.getMessegeQueue().pop());
			return;
		case WARNING_SIGNAL_WEAK:
			loggerDisplayerManager
					.addErrorMessegeToDisplay("Warning: Weak signal");
			loggerDisplayerManager
					.addErrorMessegeToDisplay("Warning: Weak signal");
			loggerDisplayerManager
					.addErrorMessegeToDisplay("Warning: Weak signal");
			java.awt.Toolkit.getDefaultToolkit().beep();
			java.awt.Toolkit.getDefaultToolkit().beep();
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		case FOLLOW_START:
			loggerDisplayerManager
					.addGeneralMessegeToDisplay("Follow Me Started");
			return;
		case FOLLOW_UPDATE:
			loggerDisplayerManager
					.addGeneralMessegeToDisplay("Follow Me Updated");
			return;
		case FOLLOW_STOP:
			loggerDisplayerManager
					.addGeneralMessegeToDisplay("Follow Me Ended");
			return;
		}
	}

	private void LoadParameter(int expectedParameterAmount) {
		setProgressBar(0,
				drone.getParameters().getLoadedDownloadedParameters(), drone
						.getParameters().getExpectedParameterAmount());
		int prc = (int) (((double) (drone.getParameters()
				.getLoadedDownloadedParameters()) / drone.getParameters()
				.getExpectedParameterAmount()) * 100);
		if (prc > 95) {
			System.out.println(getClass().getName() + " Setup stream rate");
			// MavLinkStreamRates.setupStreamRates(drone.getMavClient(), 1, 1,
			// 1, 1, 1, 1, 1, 1);
			drone.getStreamRates().setupStreamRatesFromPref();
			tbContorlButton.setButtonControl(true);
			System.out.println(getClass().getName() + " "
					+ drone.getParameters().getParameter("MOT_SPIN_ARMED"));
			if (drone.isConnectionAlive()) {
				tbTelemtry.SetHeartBeat(true);
				// SetFlightModeLabel(drone.getState().getMode().getName());
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

	@Override
	public void ClearNotification() {
		tbToolBar.ClearNotification();
	}

	@Override
	public void SetNotification(String notification) {
		tbToolBar.SetNotification(notification);
	}

}
