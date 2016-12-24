package gui.core.internalFrames;

import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import mavlink.is.drone.Drone;
import mavlink.is.drone.DroneInterfaces.DroneEventsType;
import mavlink.is.drone.DroneInterfaces.OnDroneListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ObjectsDetector.Detector;
import ObjectsDetector.ObjectDetectorListener;
import ObjectsDetector.Utilities.DetectionResults;
import gui.is.events.GuiEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

@Component("internalFrameVideo")
public class InternalFrameVideo extends Pane implements OnDroneListener, ObjectDetectorListener {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	@Resource(name = "drone")
	private Drone drone;
	
	private Detector detector;
	
	private ImageView imageViewer;

	@Autowired
	public InternalFrameVideo(@Value("Video") String title) {		
	}
	
	private static int called;
	@PostConstruct
	private void init() throws URISyntaxException {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
		
		imageViewer = new ImageView();
		getChildren().add(imageViewer);
		
		drone.addDroneListener(this);
		
		detector = new Detector(1);
		detector.addListener(this);
		detector.start();
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
		}
	}

	@Override
	public void handleImageProcessResults(DetectionResults frameProcessResult) {
		Image img = frameProcessResult.getFinalImage();
		imageViewer.setImage(img);
	}
	
}
