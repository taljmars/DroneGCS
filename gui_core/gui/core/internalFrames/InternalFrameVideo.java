package gui.core.internalFrames;

import java.net.URL;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;
import tools.validations.RuntimeValidator;

import org.springframework.beans.factory.annotation.Autowired;
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
import gui.is.events.GuiEvent;
import gui.is.services.LoggerDisplayerSvc;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component("internalFrameVideo")
public class InternalFrameVideo extends Pane implements OnDroneListener, ObjectDetectorListener, Initializable {

	@Autowired @NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Autowired @NotNull( message="Internal Error: Failed to get drone" )
	private Drone drone;
	
	@Autowired
	private RuntimeValidator runtimeValidator;
	
	@FXML private ImageView imageViewer;
	@FXML private Label redirectionLabel;
	@FXML private Button opCamera;
	@FXML private ComboBox<TrackersEnum> cbDetectorSelect;
	
	private Detector detector;
	private InternalFrameVideo myself;

	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		if (!runtimeValidator.validate(this))
			throw new RuntimeException("Value weren't initialized");
		else
			System.err.println("Validation Succeeded for instance of class " + this.getClass());
		
		drone.addDroneListener(this);
		
		detector = new Detector(0);
		detector.setTracker(new MovmentTracker(23, 23));
		detector.addListener(this);
		
		myself = this;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cbDetectorSelect.getItems().addAll(FXCollections.observableArrayList( TrackersEnum.values() ));
	};
	
	@FXML
	public void handleVideoMouseClick(MouseEvent mouseEvent) {
		if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED && mouseEvent.getClickCount() >=2 ) {				
			ExternalFrameVideo externalFrameVideo = new ExternalFrameVideo();
			detector.addListener(externalFrameVideo);
			detector.removeListener(myself);
			redirectionLabel.setVisible(true);
			imageViewer.setVisible(false);
	        Stage stage = new Stage();
			stage.setTitle("Drone Eye");
			stage.setMaximized(true);
			stage.setResizable(false);
			stage.setScene(new Scene(externalFrameVideo, 450, 450));
			stage.setOnCloseRequest( windowEvent -> {
				if (windowEvent.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
					loggerDisplayerSvc.logGeneral("Closing maximized video");
					detector.removeListener(externalFrameVideo);
					redirectionLabel.setVisible(false);
					imageViewer.setVisible(true);
					detector.addListener(myself);
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
	public void onApplicationEvent(GuiEvent command) {
		switch (command.getCommand()) {
		case EXIT:
			detector.stop();
			break;
		case CAMERA_DEVICEID:
			loggerDisplayerSvc.logGeneral("Changing device id");
			if (detector.isActive()) {
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
		imageViewer.setFitWidth(getWidth());
		imageViewer.setFitHeight(getHeight());
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
	public void handleDetectorSelectOnAction(ActionEvent actionEvent) {
		TrackersEnum value = cbDetectorSelect.getValue();
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
	
	public void HandleTracker(MovmentTracker movmentTracker) {
		System.err.println("Movment tacker");
	}
	
	public void HandleTracker(ColorTracker colorTracker) {
		System.err.println("Coolor tacker");
	}
	
	/**
	 * private class for the external video window
	 * @author taljmars
	 *
	 */
	class ExternalFrameVideo extends Pane implements ObjectDetectorListener {
		private ImageView imageViewer;
		
		public ExternalFrameVideo() {
			imageViewer = new ImageView();
			getChildren().add(imageViewer);
		}
		
		@Override
		public void handleImageProcessResults(DetectionResults frameProcessResult) {
			Image img = frameProcessResult.getFinalImage();
			imageViewer.setFitWidth(getWidth());
			imageViewer.setFitHeight(getHeight());
			imageViewer.setPreserveRatio(true);
			imageViewer.setImage(img);
		}
	}	
}
