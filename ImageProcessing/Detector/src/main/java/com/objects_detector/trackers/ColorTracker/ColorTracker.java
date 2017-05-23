package com.objects_detector.trackers.ColorTracker;

import com.objects_detector.Tracker;
import com.objects_detector.utilities.DetectedObject;
import com.objects_detector.utilities.DetectionResults;
import com.objects_detector.utilities.Utilities;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColorTracker implements Tracker {
	
	public static String name = "Color Tracker";
	
	private double hueStart = 1;
	private double hueStop = 180;
	
	private double saturationStart = 1;
	private double saturationStop = 180;
	
	private double valueStart = 1;
	private double valueStop = 180;
	
	private double dilatePixelSize = 24;
	private double erodePixelSize = 24;
	
	private double speed = 1;
	
	protected List<DetectedObject> CurrentContours;
	
	private int cycle = 1;
	
	
	public ColorTracker(double hueStart, double hueStop, double saturationStart, double saturationStop, double valueStart, 
						double valueStop, double dilatePixelSize, double erodePixelSize, double speed) {
		CurrentContours = new ArrayList<>();
		this.hueStart = hueStart;
		this.hueStop = hueStop;
		this.saturationStart = saturationStart;
		this.saturationStop = saturationStop;
		this.valueStart = valueStart;
		this.valueStop = valueStop;
		this.dilatePixelSize = dilatePixelSize;
		this.erodePixelSize = erodePixelSize;
		this.speed = speed;
	}
	
	public void setHueStart(double new_val) {
		this.hueStart = new_val;
	}

	public void setHueStop(double hueStop) {
		this.hueStop = hueStop;
	}

	public void setSaturationStart(double saturationStart) {
		this.saturationStart = saturationStart;
	}

	public void setSaturationStop(double saturationStop) {
		this.saturationStop = saturationStop;
	}
	
	public void setValueStart(double valueStart) {
		this.valueStart = valueStart;
	}

	public void setValueStop(double valueStop) {
		this.valueStop = valueStop;
	}

	public void setDilatePixelSize(double dilatePixelSize) {
		this.dilatePixelSize = dilatePixelSize;
	}

	public void setErodePixelSize(double erodePixelSize) {
		this.erodePixelSize = erodePixelSize;
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	@Override
	public DetectionResults handleFrame(Mat frame) {
		if (!frame.empty()) {
			// init
			Mat blurredImage = new Mat();
			Mat hsvImage = new Mat();
			Mat mask = new Mat();
			Mat morphOutput = new Mat();
			
			DetectionResults res = new DetectionResults();
			
			// remove some noise
			Imgproc.blur(frame, blurredImage, new Size(7, 7));
			
			// convert the frame to HSV
			Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);
			
			// get thresholding values from the UI
			// remember: H ranges 0-180, S and V range 0-255
			Scalar minValues = new Scalar(hueStart, saturationStart, valueStart);
			Scalar maxValues = new Scalar(hueStop, saturationStop, valueStop);
			
			// threshold HSV image to select tennis balls
			Core.inRange(hsvImage, minValues, maxValues, mask);
			// show the partial output
			res.addMidProcessImage(Utilities.mat2Image(mask));
			
			// morphological operators
			// dilate with large element, erode with small ones
			Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilatePixelSize, dilatePixelSize));
			Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erodePixelSize, erodePixelSize));
			
			Imgproc.erode(mask, morphOutput, erodeElement);
			Imgproc.erode(mask, morphOutput, erodeElement);
			
			Imgproc.dilate(mask, morphOutput, dilateElement);
			Imgproc.dilate(mask, morphOutput, dilateElement);
			
			// show the partial output
			res.addMidProcessImage(Utilities.mat2Image(morphOutput));
			
			// find the tennis ball(s) contours and show them
			frame = this.findAndDrawBalls(morphOutput, frame);
			res.setFinalImage(Utilities.mat2Image(frame));
			
			res.setDetectedObjects(CurrentContours);
			
			// convert the Mat object (OpenCV) to Image (JavaFX)
			return res;
		}
		return null;
	}
	
	protected Mat findAndDrawBalls(Mat maskedImage, Mat frame)
	{
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		cycle++;
		
		// find contours
		Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// if any contour exist...
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
		{
			// for each contour, display it in blue
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
			{
				MatOfPoint mat = contours.get(idx);
				UpdateExistingObjects(frame, mat, cycle);
			}
			
			Iterator<DetectedObject> it = CurrentContours.iterator();
			while (it.hasNext()) {
				DetectedObject dObj = it.next();
				if (dObj.getCycle() != cycle) {
					it.remove();
					System.out.println("Object removed");
					continue;
				}
			}
			
			if (CurrentContours.size() != 1) {
				System.out.println("Too many object recognized or there are not objects");
				return frame;
			}
			
			DetectedObject dObj = CurrentContours.get(0);
			Scalar color = new Scalar(0, 250, 0);
			if (dObj.isStable()) {
				color = new Scalar(250,0,0);
			}
			else {
				for (Point p : dObj.getHistory())
					Imgproc.drawMarker(frame, p, color);
			}
			//Rect rect = Imgproc.boundingRect(dObj.lastContour);
			Imgproc.circle(frame, dObj.getCenter(), dObj.getRadius(), color);
			//Imgproc.putText(frame, dObj.name, rect.tl(), Core.FONT_HERSHEY_COMPLEX, 1, color);
			//Imgproc.rectangle(frame, rect.tl(), rect.br(), color);
		}
		
		return frame;
	}
	
	private boolean UpdateExistingObjects(Mat frame, MatOfPoint mat, int cycle) {
		for (int i = 0 ; i < CurrentContours.size() ; i ++ ) {
			DetectedObject detection = CurrentContours.get(i);
			
			double lastRatio = Utilities.getIntersectRatio(frame, detection.getLastContour(), mat);
			double firstRatio = Utilities.getIntersectRatio(frame, detection.getFirstContour(), mat);			
			
			if (lastRatio > 0.9 && firstRatio > 0.9) {
				// Object wasn't moved
				detection.setCycle(cycle);
				detection.setStable(true);
				detection.updateLatestContour(mat);
				detection.getHistory().clear();
				return true;
			}
			
			double cover = speed / 100;
			if (lastRatio > cover) {
				detection.setCycle(cycle);
				detection.setStable(false);
				detection.updateLatestContour(mat);
				return true;
			}
		}
		
		CurrentContours.add(new DetectedObject("Object " + cycle, mat, cycle));
		return false;
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
