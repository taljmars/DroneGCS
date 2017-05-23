package com.objects_detector.trackers.MovementTracker;

import com.objects_detector.Tracker;
import com.objects_detector.utilities.DetectedObject;
import com.objects_detector.utilities.DetectionResults;
import com.objects_detector.utilities.Utilities;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MovmentTracker implements Tracker {
	
	public static String name = "Movement Tracker";

	private Mat previous_frame_grayed;
	private Mat current_frame_grayed;
	
	private double dilatePixelSize;
	private double erodePixelSize;
	
	private Mat diff, bw_diff, bw_diff_morph;
	
	private List<DetectedObject> CurrentContours;
	
	private int cycle;
	
	public MovmentTracker(double dilatePixelSize, double erodePixelSize) {
		CurrentContours = new ArrayList<>();
		cycle = 1;
		
		this.dilatePixelSize = dilatePixelSize;
		this.erodePixelSize = erodePixelSize;
		
		current_frame_grayed = new Mat();
		diff = new Mat();
		bw_diff = new Mat();
		bw_diff_morph = new Mat();
	}
	
	public void setDilatePixelSize(double new_val) {
		this.dilatePixelSize = new_val;
	}
	
	public double getDilatePixelSize() {
		return this.dilatePixelSize;
	}
	
	public void setErodePixelSize(double new_val) {
		this.erodePixelSize = new_val;
	}
	
	public double getErodePixelSize() {
		return this.erodePixelSize;
	}	

	@Override
	public DetectionResults handleFrame(Mat frame) {
		DetectionResults res = new DetectionResults();
		
		if (previous_frame_grayed != null) {
			Imgproc.cvtColor(frame, current_frame_grayed, Imgproc.COLOR_BGR2GRAY);
			
			// get diff
			Core.absdiff(previous_frame_grayed, current_frame_grayed, diff);
			
			Imgproc.threshold(diff, bw_diff, 50, 255, Imgproc.THRESH_BINARY);
						
			// show the partial output
			res.addMidProcessImage(Utilities.mat2Image(bw_diff));

			Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(dilatePixelSize, dilatePixelSize));
			Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erodePixelSize, erodePixelSize));
			
			Imgproc.erode(bw_diff, bw_diff_morph, erodeElement);
			Imgproc.erode(bw_diff, bw_diff_morph, erodeElement);
			
			Imgproc.dilate(bw_diff, bw_diff_morph, dilateElement);
			Imgproc.dilate(bw_diff, bw_diff_morph, dilateElement);
			
			res.addMidProcessImage(Utilities.mat2Image(bw_diff_morph));
			
			frame = findAndDrawBalls(bw_diff_morph, frame);
			res.setFinalImage(Utilities.mat2Image(frame));
			
			res.setDetectedObjects(CurrentContours);
			
			previous_frame_grayed = current_frame_grayed.clone();
		}
		else {
			previous_frame_grayed = new Mat();
			Imgproc.cvtColor(frame, previous_frame_grayed, Imgproc.COLOR_BGR2GRAY);
			res.addMidProcessImage(Utilities.mat2Image(frame));
			res.addMidProcessImage(Utilities.mat2Image(frame));
			res.setFinalImage(Utilities.mat2Image(frame));
		}
		
		return res;
	}

	private Mat findAndDrawBalls(Mat maskedImage, Mat frame)
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
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
				MatOfPoint mat = contours.get(idx);
				UpdateExistingObjects(frame, mat, cycle);
			}
			
			Iterator<DetectedObject> it = CurrentContours.iterator();
			while (it.hasNext()) {
				DetectedObject dObj = it.next();
				if (dObj.getCycle() != cycle) {
					it.remove();
					continue;
				}
	
				if (dObj.getAge() > 3) {
					Scalar color = new Scalar(0, 250, 0);
					if (dObj.isStable()) {
						color = new Scalar(250,0,0);
					}
					else {
						for (Point p : dObj.getHistory())
							Imgproc.drawMarker(frame, p, color);
					}
					Rect rect = Imgproc.boundingRect(dObj.getLastContour());
					Imgproc.putText(frame, dObj.getName(), rect.tl(), Core.FONT_HERSHEY_COMPLEX, 1, color);
					Imgproc.rectangle(frame, rect.tl(), rect.br(), color);
				}
			}
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
				//System.err.println("Object found - Stable");
				detection.setCycle(cycle);
				detection.setStable(true);
				detection.updateLatestContour(mat);
				detection.getHistory().clear();
				return true;
			}
			if (lastRatio > 0.5) {
				//System.err.println("Object found - move");
				detection.setCycle(cycle);
				detection.setStable(false);
				detection.updateLatestContour(mat);
				detection.setAge(detection.getAge() + 1);
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
