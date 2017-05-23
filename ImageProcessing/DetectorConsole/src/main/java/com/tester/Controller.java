package com.tester;

import com.objects_detector.Detector;
import com.objects_detector.ObjectDetectorListener;
import com.objects_detector.trackers.ColorTracker.ColorTracker;
import com.objects_detector.trackers.ColorTrackerLockSingleObject.ColorTrackerLockSingleObject;
import com.objects_detector.trackers.MovementTracker.MovmentTracker;
import com.objects_detector.utilities.DetectionResults;
import com.objects_detector.utilities.Utilities;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import org.opencv.core.Scalar;

public class Controller implements ObjectDetectorListener
{
	// FXML camera button
	@FXML
	private Button cameraButton;
	
	@FXML
	private Button fixTrackButton;
	// the FXML area for showing the current frame
	@FXML
	private ImageView originalFrame;
	// the FXML area for showing the mask
	@FXML
	private ImageView maskImage;
	// the FXML area for showing the output of the morphological operations
	@FXML
	private ImageView morphImage;
	// FXML slider for setting HSV ranges
	@FXML
	private Slider hueStart;
	@FXML
	private Slider hueStop;
	@FXML
	private Slider saturationStart;
	@FXML
	private Slider saturationStop;
	@FXML
	private Slider valueStart;
	@FXML
	private Slider valueStop;
	@FXML
	private Slider erodePixelSize;
	@FXML
	private Slider dilatePixelSize;
	@FXML
	private Slider speed;
	// FXML label to show the current values set with the sliders
	@FXML
	private Label hsvCurrentValues;
	
	@FXML
	private Label instructionsValues;
	
	private ObjectProperty<String> instructions;
	
	// property for object binding
	private ObjectProperty<String> hsvValuesProp;
		
	/**
	 * The action triggered by pushing the button on the GUI
	 */
	@FXML
	private void startCamera() {
		startVideoStreaming();
	}
	
	private void startVideoStreaming() {
		Utilities.imageViewProperties(this.originalFrame, 400);
		Detector detector = new Detector(0);
		detector.addListener(this);
		detector.start();
	}
	
	private void startMotionTracker() {
		// set a fixed width for all the image to show and preserve image ratio
		Utilities.imageViewProperties(this.originalFrame, 400);
		Utilities.imageViewProperties(this.maskImage, 200);
		Utilities.imageViewProperties(this.morphImage, 200);		
		
		Detector detector = new Detector(0);
		MovmentTracker tracker = new MovmentTracker(dilatePixelSize.getValue(), erodePixelSize.getValue());
		detector.setTracker(tracker);
		detector.addListener(this);
		detector.start();
				
		erodePixelSize.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setErodePixelSize((double) new_val);});
		dilatePixelSize.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setDilatePixelSize((double) new_val);});
	}
	
	private void startColorTracker() {
		// bind a text property with the string containing the current range of
		// HSV values for object detection
		hsvValuesProp = new SimpleObjectProperty<>();
		instructions = new SimpleObjectProperty<>();
		this.hsvCurrentValues.textProperty().bind(hsvValuesProp);
		this.instructionsValues.textProperty().bind(instructions);
				
		// set a fixed width for all the image to show and preserve image ratio
		Utilities.imageViewProperties(this.originalFrame, 400);
		Utilities.imageViewProperties(this.maskImage, 200);
		Utilities.imageViewProperties(this.morphImage, 200);		
		
		Detector detector = new Detector(0);
		ColorTracker tracker = new ColorTrackerLockSingleObject(hueStart.getValue(), hueStop.getValue(), saturationStart.getValue(), 
												saturationStop.getValue(), valueStart.getValue(), valueStop.getValue(), 
												dilatePixelSize.getValue(), erodePixelSize.getValue(), speed.getValue());
		detector.setTracker(tracker);
		detector.addListener(this);
		detector.start();
		
		hueStart.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setHueStart((double) new_val); updateValuesInGui();});
		hueStop.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setHueStop((double) new_val); updateValuesInGui();});
		saturationStart.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setSaturationStart((double) new_val); updateValuesInGui();});
		saturationStop.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setSaturationStop((double) new_val); updateValuesInGui();});
		valueStart.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setValueStart((double) new_val); updateValuesInGui();});
		valueStop.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setValueStop((double) new_val); updateValuesInGui();});
		erodePixelSize.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setErodePixelSize((double) new_val); updateValuesInGui();});
		dilatePixelSize.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setDilatePixelSize((double) new_val); updateValuesInGui();});
		speed.valueProperty().addListener( (ov, old_val, new_val) -> {tracker.setSpeed((double) new_val); updateValuesInGui();});
	}
		
	private void updateValuesInGui() {
		Scalar minValues = new Scalar(hueStart.getValue(), saturationStart.getValue(), valueStart.getValue());
		Scalar maxValues = new Scalar(hueStop.getValue(), saturationStop.getValue(), valueStop.getValue());
		
		// show the current selected HSV range
		String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
				+ "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
				+ minValues.val[2] + "-" + maxValues.val[2];
		Utilities.onFXThread(this.hsvValuesProp, valuesToPrint);
	}

	@Override
	public void handleImageProcessResults(DetectionResults frameProcessResult) {
		//utilities.onFXThread(this.maskImage.imageProperty(), frameProcessResult.getMidProcessImages().get(0));
		//utilities.onFXThread(this.morphImage.imageProperty(), frameProcessResult.getMidProcessImages().get(1));
		Utilities.onFXThread(this.originalFrame.imageProperty(), frameProcessResult.getFinalImage());
		if (frameProcessResult.getDetectedObjects().size() != 0)
			System.err.println(frameProcessResult.getDetectedObjects().get(0).getCenter().toString() + " !! ");
	}	
}