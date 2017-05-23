package com.objects_detector.utilities;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class DetectedObject {
	
	private Point center;
	private int radius;
	private MatOfPoint lastContour;
	private MatOfPoint firstContour;
	private int cycle;
	private boolean isStable;
	private String name;
	private List<Point> history;
	private int azimuth;
	private int age;
	
	public DetectedObject(String name, MatOfPoint mat, int cycle) {
		setFirstContour(mat);
		this.setHistory(new ArrayList<>());
		this.setCycle(cycle);
		this.setStable(false);
		this.setName(name);
		updateLatestContour(mat);
	}
	
	public void updateLatestContour(MatOfPoint mat) {
		lastContour = mat;			
		Rect r = Imgproc.boundingRect(lastContour);
		this.setCenter(new Point(r.x + r.width/2, r.y + r.height/2));
		this.setRadius(Math.max(r.width/2, r.height/2));
		
		this.getHistory().add(getCenter());
		
		if (getHistory().size() >= 2) {
			Point last = getHistory().get(getHistory().size() - 1);
			Point before = getHistory().get(getHistory().size() - 2);
			azimuth = Utilities.getAngle(last, before);
		}
	}

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public List<Point> getHistory() {
		return history;
	}

	public void setHistory(List<Point> history) {
		this.history = history;
	}

	public boolean isStable() {
		return isStable;
	}

	public void setStable(boolean isStable) {
		this.isStable = isStable;
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public MatOfPoint getFirstContour() {
		return firstContour;
	}

	public void setFirstContour(MatOfPoint firstContour) {
		this.firstContour = firstContour;
	}
	
	public MatOfPoint getLastContour() {
		return lastContour;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
