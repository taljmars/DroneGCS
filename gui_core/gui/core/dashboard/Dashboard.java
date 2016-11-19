package gui.core.dashboard;

import gui.core.internalPanels.*;
import gui.core.operations.OpGCSTerminationHandler;
import gui.core.springConfig.AppConfig;
import mavlink.core.gcs.GCSHeartbeat;

import java.util.List;

import gui.is.services.LoggerDisplayerSvc;
import gui.is.services.TextNotificationPublisherSvc;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.context.event.EventListener;

import javax.validation.constraints.NotNull;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.*;
import mavlink.is.drone.parameters.Parameter;
import mavlink.is.protocol.msg_metadata.ApmModes;
import mavlink.is.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;

public class Dashboard extends StackPane implements OnDroneListener, OnWaypointManagerListener, OnParameterManagerListener, EventHandler<WindowEvent> {
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	public static final String APP_TITLE = "Quad Ground Station";
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "drone")
	@NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Resource(name="frameContainer")
	@NotNull(message = "Internal Error: Missing panel")
	private Pane frameContainer;
	
	@Resource(name="areaLogBox")
	@NotNull(message = "Internal Error: Missing tab")
	private PanelLogBox areaLogBox;

	@Resource(name="areaMission")
	@NotNull(message = "Internal Error: Missing tab")
	private PanelMissionBox areaMission;

	@Resource(name="areaConfiguration")
	@NotNull(message = "Internal Error: Missing tab")
	private PanelConfigurationBox areaConfiguration;
	
	@Resource(name="telemetrySatellite")
	@NotNull(message = "Internal Error: Missing panel")
	private PanelTelemetrySatellite tbTelemtry;
	
	@Resource(name="buttonBoxSatellite")
	@NotNull(message = "Internal Error: Missing panel")
	private PanelButtonBoxSatellite tbContorlButton;
	
	@Resource(name="toolbarSatellite")
	@NotNull(message = "Internal Error: Missing panel")
	private PanelToolBarSatellite tbToolBar;
	
	@Resource(name = "textNotificationPublisherSvc")
	@NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;
	
	@Resource(name = "opGCSTerminationHandler")
	private OpGCSTerminationHandler opGCSTerminationHandler;
	
	@Resource(name = "gcsHeartbeat")
	@NotNull(message = "Internal Error: Failed to get HB mechanism")
	private GCSHeartbeat gcsHeartbeat;
	
	@NotNull(message = "Internal Error: Missing panel")
	private TabPane tabPane;
	
	@NotNull(message = "Internal Error: Missing panel")
	private ProgressBar progressBar;
	
	@NotNull(message = "Internal Error: Mission view manager")
	private Stage viewManager;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		initializeDefinitions();
		initializeGui();
	}
	
	private void initializeDefinitions() {
		System.out.println("Sign Dashboard as drone listener");
		drone.addDroneListener(this);
		drone.getWaypointManager().addWaypointManagerListener(this);
		drone.getParameters().addParameterListener(this);

		if (drone.isConnectionAlive()) {
			tbTelemtry.SetHeartBeat(true);
			drone.notifyDroneEvent(DroneEventsType.MODE);
		}
	}

	private void initializeGui() {
		BorderPane frame = new BorderPane();
		
		// North Panel
		frame.setTop(tbToolBar);

		// Central Panel
		frame.setCenter(frameContainer);

		// South Panel
		VBox southPanel = new VBox();
		frame.setBottom(southPanel);
		tabPane = new TabPane();
		southPanel.getChildren().add(tabPane);
		
		Tab tab = new Tab();
		tab.setClosable(false);
		tab.setText("Log Book");
		tab.setContent(areaLogBox);
        tabPane.getTabs().add(tab);
        
		tab = new Tab();
		tab.setClosable(false);
		tab.setText("Configuration");
		tab.setContent(areaConfiguration);
        tabPane.getTabs().add(tab);
        
        tab = new Tab();
		tab.setClosable(false);
		tab.setText("Mission");
		tab.setContent(areaMission);
        tabPane.getTabs().add(tab);
        
        progressBar = new ProgressBar();
        southPanel.getChildren().add(progressBar);
		progressBar.setPrefWidth(Screen.getPrimary().getBounds().getWidth());

		// East Panel
		VBox eastPanel = new VBox();
		frame.setRight(eastPanel);
		eastPanel.setPrefWidth(Screen.getPrimary().getBounds().getWidth() * AppConfig.FRAME_CONTAINER_REDUCE_PRECENTAGE);
		tbTelemtry.setPadding(new Insets(20,0,0,0));
		tbContorlButton.setAlignment(Pos.CENTER);
		tbTelemtry.setAlignment(Pos.CENTER);
		eastPanel.getChildren().add(tbContorlButton);
		eastPanel.getChildren().add(tbTelemtry);
        
		getChildren().add(frame);
	}

	private void VerifyBattery(double bat) {
	}
	
	private void SetDistanceToWaypoint(double d) {
		if (drone.getState().getMode().equals(ApmModes.ROTOR_GUIDED)) {
			// if (drone.getGuidedPoint().isIdle()) {
			if (d == 0) {
				textNotificationPublisherSvc.publish("In Position");
				loggerDisplayerSvc.logGeneral("Guided: In Position");
			} else {
				textNotificationPublisherSvc.publish("Flying to destination");
				loggerDisplayerSvc.logGeneral("Guided: Fly to distination");
			}
		}
	}
	
	@Override
	public void onBeginWaypointEvent(WaypointEvent_Type wpEvent) {
		initProgressBar();
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD) || wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			return;
		}
		finiProgressBar();
	}

	@Override
	public void onWaypointEvent(WaypointEvent_Type wpEvent, int index, int count) {
		if (wpEvent.equals(WaypointEvent_Type.WP_DOWNLOAD) || wpEvent.equals(WaypointEvent_Type.WP_UPLOAD)) {
			setProgressBar((double) index / count);
			return;
		}
		finiProgressBar();
	}

	@Override
	public void onEndWaypointEvent(WaypointEvent_Type wpEvent) {
		finiProgressBar();
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case LEFT_PERIMETER:
			textNotificationPublisherSvc.publish("Outside Perimeter");
			loggerDisplayerSvc.logError("Quad left the perimeter");
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		case ENFORCING_PERIMETER:
			textNotificationPublisherSvc.publish("Enforcing Perimeter");
			loggerDisplayerSvc.logError("Enforcing Perimeter");
			return;
		case ORIENTATION:
			SetDistanceToWaypoint(drone.getMissionStats().getDistanceToWP().valueInMeters());
			return;
		case BATTERY:
			VerifyBattery(drone.getBattery().getBattRemain());
			return;
		case MODE:
			viewManager.setTitle(APP_TITLE + " (" + drone.getState().getMode().getName() + ")");
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

	private synchronized void initProgressBar() {
		System.out.println("Init progress bar");
		progressBar.setVisible(true);
		progressBar.setProgress(0);
	}
	
	private synchronized void setProgressBar(double val) {
		progressBar.setProgress(val);
		if (progressBar.getProgress() == 1.0) {
			finiProgressBar();
		}
	}
	
	private synchronized void finiProgressBar() {
		System.out.println("Fini progress bar");
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
		int prc = drone.getParameters().getPrecentageComplete();
		if (prc > 95) {
			setProgressBar(1);
			drone.getStreamRates().setupStreamRatesFromPref();
			if (drone.isConnectionAlive())
				drone.notifyDroneEvent(DroneEventsType.MODE);
		}
		else {
			setProgressBar(((double )prc) / 100.0);
		}
	}

	@Override
	public void onEndReceivingParameters(List<Parameter> parameter) {
		System.out.println("Finish receiving parameters");
		finiProgressBar();
		
		drone.getStreamRates().prepareStreamRates();
		drone.getStreamRates().setupStreamRatesFromPref();
		if (drone.isConnectionAlive())
			drone.notifyDroneEvent(DroneEventsType.MODE);
	}

	public void setViewManager(Stage stage) {
		this.viewManager = stage;
		viewManager.setTitle(APP_TITLE);
		viewManager.setOnCloseRequest(this);
	}

	@Override
	public void handle(WindowEvent event) {
		if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
			try {
				opGCSTerminationHandler.go();
				event.consume();
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
