package com.objects_detector.trackers.ColorTrackerLockSingleObject;

import com.objects_detector.trackers.ColorTracker.ColorTracker;
import com.objects_detector.utilities.DetectedObject;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ColorTrackerLockSingleObject extends ColorTracker {
	
	public static String name = "Single Object Color Tracker";
	
	public ColorTrackerLockSingleObject(double hueStart, double hueStop, double saturationStart, double saturationStop,
			double valueStart, double valueStop, double dilatePixelSize, double erodePixelSize, double speed) {
		super(hueStart, hueStop, saturationStart, saturationStop, valueStart, valueStop, dilatePixelSize, erodePixelSize,
				speed);
	}

	protected Mat findAndDrawBalls(Mat maskedImage, Mat frame)
	{
		Mat resFrame = super.findAndDrawBalls(maskedImage, frame);
		
		if (CurrentContours.size() != 1) {
			//System.out.println("Too many object recognized or there are not objects");
			return resFrame;
		}
			
		DetectedObject dObj = CurrentContours.get(0);
		Scalar color = new Scalar(0, 250, 0);
		if (dObj.isStable()) {
			color = new Scalar(250,0,0);
		}
		else {
			for (Point p : dObj.getHistory())
				Imgproc.drawMarker(resFrame, p, color);
		}
		//Rect rect = Imgproc.boundingRect(dObj.lastContour);
		Imgproc.circle(resFrame, dObj.getCenter(), dObj.getRadius(), color);
		//Imgproc.putText(frame, dObj.name, rect.tl(), Core.FONT_HERSHEY_COMPLEX, 1, color);
		//Imgproc.rectangle(frame, rect.tl(), rect.br(), color);
	
		return resFrame;
	}
}
