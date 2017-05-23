package com.objects_detector;

import com.objects_detector.trackers.FakeTracker.FakeTracker;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Detector {
	
	private int videoDeviceId = -1;
	
	private Set<ObjectDetectorListener> listeners;
	
	private boolean isActive = false;
	
	private VideoCapture capture;
	
	private ScheduledExecutorService timer;
	
	private Tracker tracker;
	
	public Detector() {
		capture = new VideoCapture();
		listeners = new HashSet<>();
		tracker = new FakeTracker();
	}
	
	public Detector(int deviceId) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		videoDeviceId = deviceId;
		capture = new VideoCapture();
		listeners = new HashSet<>();
		tracker = new FakeTracker();
	}
	
	public Detector(int deviceId, Tracker oTracker) {
		this(deviceId);
		tracker = oTracker;
	}
	
	/**
	 * Check if video capture was started or not.
	 * capture will start if {@link start} method was called
	 *   
	 * @return true if capture started, false otherwise
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Modifying detector tracking method.
	 * the default detector have no tracker and it will publish non-filtered images.
	 * use this method to set new tracker, setting null to it will cancel the current 
	 * tracker and set not filter images.
	 * 
	 * @param tracker - a type of tracker
	 */
	public void setTracker(Tracker oTracker) {
		tracker = oTracker;
	}
	
	/**
	 * set the device id to get the video input from
	 * 
	 * @param deviceId - of the camera source
	 */
	public void setDeviceId(int deviceId) {
		this.videoDeviceId = deviceId;
	}
	
	/**
	 * start grabbing images from the device. 
	 * Output might be modified if a tracker is defined.
	 * 
	 * @return true if open camera device succeeded, false otherwise
	 */
	public boolean start() {
		if (videoDeviceId == -1) {
			System.err.println("Device Id wasn't initilialized");
			return false;
		}
		if (!this.isActive) {
			this.capture.open(videoDeviceId);
			
			if (this.capture.isOpened()) {
				isActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = () -> handleFrame();
				
				timer = Executors.newSingleThreadScheduledExecutor();
				timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				return true;
			}
			else {
				System.err.println("Failed to open the camera connection...");
				return false;
			}
		}
		else {
			try {
				isActive = false;
				timer.shutdown();
				timer.awaitTermination(330, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
			
			this.capture.release();
			return false;
		}
	}
	
	/**
	 * Stopping the video device in case it is activated. 
	 */
	public void stop() {
		if (videoDeviceId == -1) {
			System.err.println("Device Id wasn't initilialized");
			return;
		}
		if (!isActive)
			return;
		
		try {
			isActive = false;
			timer.shutdown();
			timer.awaitTermination(330, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e) {
			System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
		}
		
		this.capture.release();
	}
	
	private void handleFrame() {
		Mat frame = new Mat();
		capture.read(frame);
		for (ObjectDetectorListener listener : listeners)
			listener.handleImageProcessResults(tracker.handleFrame(frame));
	}

	/**
	 * Signed up to video capture listeners. an appropriate method will be invoked on image arrival.
	 * 
	 * @param objectDetectorListener - listener to be added
	 */
	public void addListener(ObjectDetectorListener objectDetectorListener) {
		listeners.add(objectDetectorListener);
	}
	
	/**
	 * Remove a video capture listener.
	 * 
	 * @param objectDetectorListener - listener to be removed
	 */
	public void removeListener(ObjectDetectorListener objectDetectorListener) {
		listeners.remove(objectDetectorListener);
	}	
}
