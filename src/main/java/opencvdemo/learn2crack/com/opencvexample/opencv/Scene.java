package opencvdemo.learn2crack.com.opencvexample.opencv;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;

import android.graphics.Bitmap;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;

public class Scene {

	final Mat image;
	final Mat descriptors = new Mat();
	final MatOfKeyPoint keypoints = new MatOfKeyPoint();
	boolean firstTime = true;

	public Scene(Mat image) {
		this.image = image.clone();
		// DetectUtility.analyze(image, keypoints, descriptors);
	}

	public void preCompute() {
		if (firstTime) {
			DetectUtility.analyze(image, keypoints, descriptors);
			firstTime = false;
		}
	}

	public SceneDetectData compare(Scene frame, boolean isHomogrpahy, boolean imageOnly) {
		// Info to store analysis stats
		SceneDetectData s = new SceneDetectData();

		// Detect key points and compute descriptors for inputFrame
		MatOfKeyPoint f_keypoints = frame.keypoints;
		Mat f_descriptors = frame.descriptors;

		this.preCompute();
		frame.preCompute();
		

		// Compute matches
		//MatOfDMatch matches = DetectUtility.match(descriptors, f_descriptors);
		MatOfDMatch matches = DetectUtility.matchAkaze(descriptors, f_descriptors);
		//knnMatch
		// Filter matches by distance

		MatOfDMatch filtered = DetectUtility.filterMatchesByDistance(matches);

		// If count of matches is OK, apply homography check

		s.original_key1 = (int) descriptors.size().height;
		s.original_key2 = (int) f_descriptors.size().height;
		Log.d("original_key1", " " + s.original_key1 + "");
		Log.d("original_key2", " " + s.original_key2 + "");
		s.original_matches = (int) matches.size().height;
		s.dist_matches = (int) filtered.size().height;
		Log.d("original_matches", " " + s.original_matches + "");
		Log.d("dist_matches", " " + s.dist_matches + "");

		if (isHomogrpahy) {
			MatOfDMatch homo = DetectUtility.filterMatchesByHomography(
					keypoints, f_keypoints, filtered);
			Bitmap bmp = DetectUtility.drawMatches(image, keypoints,
					frame.image, f_keypoints, homo, imageOnly);
			if (s.dist_matches >= 40)
			{				s.bmp = bmp;
				s.homo_matches = (int) homo.size().height;
				//return s;
			}
		} else {
			Bitmap bmp = DetectUtility.drawMatches(image, keypoints,
					frame.image, f_keypoints, filtered, imageOnly);
			if (s.dist_matches >= 40) {
				s.bmp = bmp;
				s.homo_matches = -1;
				//return s;
			}
		}
		return s;
	}

}
