package main.java.gui_controllers.controllers.dashboard;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import main.java.is.gui.events.QuadGuiEvent;
import main.java.is.gui.services.EventPublisherSvc;
import main.java.is.gui.services.LoggerDisplayerSvc;
import main.java.is.gui.services.TextNotificationPublisherSvc;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.dronedb.ws.DroneDbCrudSvcRemote;

import controllers.internalFrames.InternalFrameMap;
import core.operations.OpGCSTerminationHandler;

import javax.validation.constraints.NotNull;

import main.java.is.mavlink.drone.Drone;
import main.java.is.mavlink.drone.DroneInterfaces.*;
import main.java.is.mavlink.drone.parameters.Parameter;
import main.java.is.mavlink.protocol.msg_metadata.ApmModes;
import main.java.is.mavlink.protocol.msgbuilder.WaypointManager.WaypointEvent_Type;
import main.java.is.springConfig.AppConfig;
import main.java.is.validations.RuntimeValidator;

@ComponentScan("controllers.internalFrames")
@ComponentScan("gui.core.operations")
@Component("dashboard")
public class Dashboard extends StackPane implements OnDroneListener, OnWaypointManagerListener, OnParameterManagerListener, EventHandler<WindowEvent>, Initializable {

	public static final String APP_TITLE = "Quad Ground Station";
	
	@Autowired @NotNull(message = "Internal Error: Failed to get drone")
	private Drone drone;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get text publisher")
	private TextNotificationPublisherSvc textNotificationPublisherSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get GCS terminator handler")
	private OpGCSTerminationHandler opGCSTerminationHandler;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get map frame")
	private InternalFrameMap internalFrameMap;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get current frames amount")
	private EventPublisherSvc eventPublisherSvc;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@Autowired
	private DroneDbCrudSvcRemote droneDbCrudSvcRemote;
	
	@FXML private HBox frameContainer;
	@FXML private ProgressBar progressBar;
	
	private Stage viewManager;

	private SimpleIntegerProperty maxFramesAmount;
	
	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Validation failed");
		
		initializeDefinitions();
		
		System.out.println(droneDbCrudSvcRemote.CheckConnection());
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeGui();
	}
	
	private void initializeDefinitions() {
		System.out.println("Sign Dashboard as drone listener");
		drone.addDroneListener(this);
		drone.getWaypointManager().addWaypointManagerListener(this);
		drone.getParameters().addParameterListener(this);

		if (drone.isConnectionAlive()) {
//			tbTelemtry.SetHeartBeat(true);
			drone.notifyDroneEvent(DroneEventsType.MODE);
		}
	}

	private void initializeGui() {
		maxFramesAmount = new SimpleIntegerProperty(2);
		
		frameContainer.setOnDragOver( (event) -> {
		        /* data is dragged over the target */
		        /* accept it only if it is not dragged from the same node 
		         * and if it has a string data */
		        if (event.getGestureSource() != frameContainer && event.getDragboard().hasString()) {
		            /* allow for both copying and moving, whatever user chooses */
		            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
		        }
		        
		        event.consume();
		        frameContainer.setStyle("-fx-border-color: red; -fx-border-width: 2; -fx-background-color: #C6C6C6; -fx-border-style: solid;");
		});
		
		frameContainer.setOnDragExited( (event) -> frameContainer.setStyle(""));
		
		frameContainer.setOnDragDropped( (event) -> {
		        /* data dropped */
		        /* if there is a string data on dragboard, read it and use it */
		    	Dragboard db = event.getDragboard();
		    	boolean success = false;
		    	if (db.hasString()) {
		    		loggerDisplayerSvc.logGeneral(db.getString());
		    		success = true;
		    	}
		    	
		    	if (event.getGestureSource() instanceof Button) {
		    		Button button = (Button) event.getGestureSource();
		    		int index = GetFrameIndexInsideContainer(event.getScreenX());
		    		handleFrameContainerRequest((String) button.getUserData(), index);
		    		/* let the source know whether the string was successfully transferred and used */
			    	event.setDropCompleted(success);
		    	}
		    	else {
		    		event.setDropCompleted(false);
		    	}
		        	
		    	event.consume();
		});
		frameContainer.setOnDragEntered( (event) -> event.consume());
	}
	
	private int GetFrameIndexInsideContainer(double intersectedPoint) {
		if (maxFramesAmount.get() == 1)
			return 0;
		
		ObservableList<Node> children = frameContainer.getChildren();
		if (children.isEmpty())
			return 0;
		
		double size = frameContainer.getWidth();
		double positionRelativeToFrameContainerSize = intersectedPoint - frameContainer.getLayoutX();
		
		int index = (int) Math.round((positionRelativeToFrameContainerSize / size) * (maxFramesAmount.get() - 1));
		return index;
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
			SetDistanceToWaypoint(drone.getMissionStats().getDistanceToWP());
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
	
	private void handleFrameContainerRequest(String springInstanciation, int index) {
		if (springInstanciation.isEmpty())
			return;
		
		ObservableList<Node> children = frameContainer.getChildren();
		Node selectedPane = (Node) AppConfig.loader.loadInternalFrame(springInstanciation, frameContainer.getWidth() / maxFramesAmount.get(), frameContainer.getHeight() - 25);
		selectedPane.setUserData(springInstanciation);
		if (selectedPane != null) {
			selectedPane.setUserData(springInstanciation);
			Platform.runLater(() -> {
				if (maxFramesAmount.get() == 1 || isFrameAlreadyOpen(children, selectedPane))
					children.clear();
				
				if (children.size() != maxFramesAmount.get())
					children.add(selectedPane);
				else {
					children.remove(index);
					children.add(index, selectedPane);
				}
			});
		}
	}
	
	private boolean isFrameAlreadyOpen(ObservableList<Node> lst, Node obj) {
		for (Node node : lst) {
			if (node.getUserData().equals(obj.getUserData()))
				return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
		switch (command.getCommand()) {
		case SPLIT_FRAMECONTAINER:
			maxFramesAmount.set((Integer) command.getSource());
			frameContainer.getChildren().clear();
			break;
		}
	}

}
