package com.objects_detector.utilities;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;

public class Utilities {
	
	public static Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer, according to the PNG format
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	public static int getAngle(Point target, Point source) {
	    int angle = (int) Math.toDegrees(Math.atan2(target.y - source.y, target.x - source.x));

	    if(angle < 0)
	        angle += 360;

	    return angle;
	}
	
	public static double getIntersectRatio(Mat frame, MatOfPoint mat1, MatOfPoint mat2) {
		// Left Top corner starts from 0,0.
		
		Rect rect1 = Imgproc.boundingRect(mat1);
		Rect rect2 = Imgproc.boundingRect(mat2);
		
		Point rect1_tl = rect1.tl();
		Point rect1_tr = new Point(rect1.tl().x + rect1.width, rect1_tl.y);
		Point rect1_br = rect1.br();
		Point rect1_bl = new Point(rect1.br().x - rect1.width, rect1_br.y);
		
		Point rect2_tl = rect2.tl();
		Point rect2_tr = new Point(rect2.tl().x + rect2.width, rect2_tl.y);
		Point rect2_br = rect2.br();
		Point rect2_bl = new Point(rect2.br().x - rect2.width, rect2_br.y);
		
		// No Intersection
		if (rect2_tl.y > rect1_br.y || rect1_tl.y > rect2.br().y || rect2_tl.x > rect1_br.x || rect2_br.x < rect1_tl.x) {
			//System.err.println("No Intersection at all");
			return 0.0;
		}
		
		if (rect2_tl.inside(rect1) && rect1_br.inside(rect2)) {
			Rect tmp = new Rect(rect2_tl, rect1_br);
			return Math.max(tmp.area() / rect1.area(), tmp.area() / rect2.area());
		}
		
		if (rect2_tr.inside(rect1) && rect1_bl.inside(rect2)) {
			Rect tmp = new Rect(rect2_tr, rect1_bl);
			return Math.max(tmp.area() / rect1.area(), tmp.area() / rect2.area());
		}
		
		if (rect2_br.inside(rect1) && rect1_tl.inside(rect2)) {
			Rect tmp = new Rect(rect2_br, rect1_tl);
			return Math.max(tmp.area() / rect1.area(), tmp.area() / rect2.area());
		}
		
		if (rect2_bl.inside(rect1) && rect1_tr.inside(rect2)) {
			Rect tmp = new Rect(rect2_bl, rect1_tr);
			return Math.max(tmp.area() / rect1.area(), tmp.area() / rect2.area());
		}
		
		return Math.min(rect1.area(), rect2.area()) / Math.max(rect1.area(), rect2.area());
	}
	

	public static void imageViewProperties(ImageView image, int dimension) {
		// set a fixed width for the given ImageView
		image.setFitWidth(dimension);
		// preserve the image ratio
		image.setPreserveRatio(true);
	}
	
	public static <T> void onFXThread(final ObjectProperty<T> property, final T value) {
		Platform.runLater( () -> property.set(value) );
	}

}
