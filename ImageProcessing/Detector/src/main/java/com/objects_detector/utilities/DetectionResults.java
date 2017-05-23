package com.objects_detector.utilities;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class DetectionResults {
	
	private List<DetectedObject> detectedObject;
	private List<Image> images;
	private Image clearImage;
	private Image finalImage;

	public DetectionResults() {
		detectedObject = new ArrayList<>();
		images = new ArrayList<Image>();
	}
	
	public void addMidProcessImage(Image image) {
		images.add(image);
	}
	
	public List<Image> getMidProcessImages() {
		return images;
	}
	
	public void setDetectedObjects(List<DetectedObject> currentContours) {
		this.detectedObject = currentContours;		
	}
	
	public List<DetectedObject> getDetectedObjects() {
		return this.detectedObject;
	}
	
	public void setFinalImage(Image finalImage) {
		this.finalImage = finalImage;
	}
	
	public Image getFinalImage() {
		return this.finalImage;
	}

	public Image getClearImage() {
		return clearImage;
	}

	public void setClearImage(Image clearImage) {
		this.clearImage = clearImage;
	}


}
