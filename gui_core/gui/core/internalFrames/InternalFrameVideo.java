package gui.core.internalFrames;

import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ObjectsDetector.Detector;
import ObjectsDetector.ObjectDetectorListener;
import ObjectsDetector.MovementTracker.MovmentTracker;
import ObjectsDetector.Utilities.DetectionResults;
import gui.is.events.GuiEvent;
import gui.is.services.LoggerDisplayerSvc;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@Component("internalFrameVideo")
public class InternalFrameVideo extends Pane implements OnDroneListener, ObjectDetectorListener {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "loggerDisplayerSvc")
	@NotNull(message = "Internal Error: Failed to get logger displayer")
	private LoggerDisplayerSvc loggerDisplayerSvc;
	
	@Resource(name = "drone")
	private Drone drone;
	
	private Detector detector;
	
	private ImageView imageViewer;
	
	private Text inputTitle;
	
	private InternalFrameVideo myself;

	@Autowired
	public InternalFrameVideo(@Value("Video") String title) {
	}
	
	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		imageViewer = new ImageView();
		imageViewer.setOnMouseClicked( event -> {
			if (event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getClickCount() >=2 ) {				
				ExternalFrameVideo externalFrameVideo = new ExternalFrameVideo();
				detector.addListener(externalFrameVideo);
				detector.removeListener(myself);
				inputTitle = new Text("Video Redirection");
				myself.getChildren().add(inputTitle);
				myself.getChildren().remove(myself.imageViewer);
		        Stage stage = new Stage();
				stage.setTitle("Drone Eye");
				stage.setMaximized(true);
				stage.setResizable(false);
				stage.setScene(new Scene(externalFrameVideo, 450, 450));
				stage.setOnCloseRequest( windowEvent -> {
					if (windowEvent.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
						loggerDisplayerSvc.logGeneral("Closing maximized video");
						detector.removeListener(externalFrameVideo);
						myself.getChildren().remove(inputTitle);
						myself.getChildren().add(myself.imageViewer);
						detector.addListener(myself);
					}
				});
				stage.show();
			}
		});
		getChildren().add(imageViewer);
		
		drone.addDroneListener(this);
		
		detector = new Detector(0);
		detector.setTracker(new MovmentTracker(23, 23));
		detector.addListener(this);
		detector.start();
		
		myself = this;
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
	
	/**
	 * private class for the exteranl video window
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
	};
	
}
