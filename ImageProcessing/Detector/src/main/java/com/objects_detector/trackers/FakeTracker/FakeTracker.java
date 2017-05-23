package com.objects_detector.trackers.FakeTracker;

import com.objects_detector.Tracker;
import com.objects_detector.utilities.DetectionResults;
import com.objects_detector.utilities.Utilities;
import javafx.scene.image.Image;
import org.opencv.core.Mat;

public class FakeTracker implements Tracker {
	
	public static String name = "Video Only";

	@Override
	public DetectionResults handleFrame(Mat frame) {
		Image img = Utilities.mat2Image(frame);
		DetectionResults detectionResults = new DetectionResults();
		detectionResults.setFinalImage(img);
		return detectionResults;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

}
