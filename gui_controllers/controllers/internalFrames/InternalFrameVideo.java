package controllers.internalFrames;

import java.net.URL;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import validations.RuntimeValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ObjectsDetector.Detector;
import ObjectsDetector.ObjectDetectorListener;
import ObjectsDetector.Trackers.TrackersEnum;
import ObjectsDetector.Trackers.ColorTracker.ColorTracker;
import ObjectsDetector.Trackers.ColorTrackerLockSingleObject.ColorTrackerLockSingleObject;
import ObjectsDetector.Trackers.FakeTracker.FakeTracker;
import ObjectsDetector.Trackers.MovementTracker.MovmentTracker;
import ObjectsDetector.Utilities.DetectionResults;
import controllers.droneEye.DroneEye;
import gui.events.QuadGuiEvent;
import gui.services.LoggerDisplayerSvc;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mavlink.drone.Drone;
import mavlink.drone.DroneInterfaces.DroneEventsType;
import mavlink.drone.DroneInterfaces.OnDroneListener;
import springConfig.AppConfig;

@ComponentScan("gui.services")
@ComponentScan("validations")
@Component("internalFrameVideo")
public class InternalFrameVideo extends Pane implements OnDroneListener, ObjectDetectorListener, Initializable {

	@Autowired @NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@NotNull @FXML private Pane root;
	@NotNull @FXML private ImageView imageViewer;
	@NotNull @FXML private Label redirectionLabel;
	@NotNull @FXML private Button opCamera;
	@NotNull @FXML private ComboBox<TrackersEnum> cbTrackerSelect;
	
	private Detector detector;
	private InternalFrameVideo myself;

	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		drone.addDroneListener(this);
		
		detector = new Detector(0);
		detector.setTracker(new MovmentTracker(23, 23));
		detector.addListener(this);

		myself = this;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cbTrackerSelect.getItems().addAll(FXCollections.observableArrayList( TrackersEnum.values() ));
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Value weren't initialized");
		else
			System.err.println("Validation Succeeded for instance of class " + this.getClass());
	};
	
	@Autowired @NotNull 
	private DroneEye externalFrameVideo;
	
	@FXML
	public void handleVideoMouseClick(MouseEvent mouseEvent) {
		if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED && mouseEvent.getClickCount() >=2 ) {
			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			Parent droneEyeView = (Parent) AppConfig.loader.loadInternalFrame("/views/DroneEyeView.fxml", primaryScreenBounds.getWidth(), primaryScreenBounds.getHeight() );
			loggerDisplayerSvc.logGeneral("add drone listening to drone eye view");
			drone.addDroneListener(externalFrameVideo);
			detector.addListener(externalFrameVideo);
			detector.removeListener(myself);
			redirectionLabel.setVisible(true);
			imageViewer.setVisible(false);
	        Stage stage = new Stage();
			stage.setTitle("Drone Eye");
			stage.setMaximized(true);
			stage.setScene(new Scene(droneEyeView, 800, 800));
			stage.setOnCloseRequest( windowEvent -> {
				if (windowEvent.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
					loggerDisplayerSvc.logGeneral("Closing maximized video");
					detector.removeListener(externalFrameVideo);
					redirectionLabel.setVisible(false);
					imageViewer.setVisible(true);
					detector.addListener(myself);
					loggerDisplayerSvc.logGeneral("Removing drone listening from drone eye view");
					drone.removeDroneListener(externalFrameVideo);
				}
			});
			stage.show();
		}
	}


	@SuppressWarnings("incomplete-switch")
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		}
	}
	
	@SuppressWarnings("incomplete-switch")
	@EventListener
	public void onApplicationEvent(QuadGuiEvent command) {
		switch (command.getCommand()) {
		case EXIT:
			if (detector != null)
				detector.stop();
			break;
		case CAMERA_DEVICEID:
			loggerDisplayerSvc.logGeneral("Changing device id");
			if (detector != null && detector.isActive()) {
				loggerDisplayerSvc.logGeneral("Device is running  stopping current before switching id");
				detector.stop();
				detector.setDeviceId((int) command.getSource());
				detector.start();
			}
			break;
		}
	}

	@Override
	public void handleImageProcessResults(DetectionResults frameProcessResult) {
		Image img = frameProcessResult.getFinalImage();
		imageViewer.setFitWidth(root.getPrefWidth());
		imageViewer.setPreserveRatio(true);
		imageViewer.setImage(img);		
	}
	
	@FXML
	public void handleOpCameraOnAction(ActionEvent actionEvent) {
		if (detector.isActive()) {
			opCamera.setText("Start Camera");
			detector.stop();
		}
		else {
			opCamera.setText("Stop Camera");
			detector.start();
		}
	}
	
	@FXML
	public void handleTrackerSelectOnAction(ActionEvent actionEvent) {
		TrackersEnum value = cbTrackerSelect.getValue();
		switch (value) {
			case MOVEMENT_TRACKER:
				detector.setTracker(new MovmentTracker(10, 24));
				break;
			case COLOR_TRACKER:
				detector.setTracker(new ColorTracker(156, 180, 171, 232, 80, 185, 10, 24, 30));
				break;
			case COLOR_TRACKER_SINGLE_OBJECT:
				detector.setTracker(new ColorTrackerLockSingleObject(156, 180, 171, 232, 80, 185, 10, 24, 30));
				break;
			case VIDEO_ONLY:
				detector.setTracker(new FakeTracker());
				break;
		default:
			break;
		}
	}
}
