package com.objects_detector;

import com.objects_detector.utilities.DetectionResults;

public interface ObjectDetectorListener {
	
	public void handleImageProcessResults(DetectionResults frameProcessResult);
}
