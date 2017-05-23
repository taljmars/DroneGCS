package com.objects_detector;

import com.objects_detector.utilities.DetectionResults;
import org.opencv.core.Mat;

public interface Tracker {
	
	String getName();
	
	String getDescription();

	DetectionResults handleFrame(Mat frame);

}
