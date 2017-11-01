package opencvdemo.learn2crack.com.opencvexample.opencv;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;

import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.util.Log;

public class DetectUtility {
	final static FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
	final static DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
	final static DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

	static void analyze(Mat image, MatOfKeyPoint keypoints, Mat descriptors){
		//Imgproc.resize(image, image,new Size(480,320));
		detector.detect(image, keypoints);
		extractor.compute(image, keypoints, descriptors);
	}
	
	static MatOfDMatch match(Mat desc1, Mat desc2){
		MatOfDMatch matches = new MatOfDMatch();
		if (desc1.cols() == desc2.cols() && desc1.type() == desc2.type()) {
			matcher.match(desc1, desc2, matches);
		}
		Log.d("matches", "ORG SIZE:" + matches.size() + "");
		return matches;
	}
    static MatOfDMatch matchAkaze(Mat desc1, Mat desc2){
        MatOfDMatch matches = new MatOfDMatch();
        LinkedList<MatOfDMatch> dmatchesListOfMat = new LinkedList<>();
		if (desc1.cols() == desc2.cols() && desc1.type() == desc2.type()) {
			matcher.knnMatch(desc1, desc2, dmatchesListOfMat, 2);
		}
        LinkedList<DMatch> good_matchesList = new LinkedList<>();
        double ratio = 0.8;
        for (int matchIndx = 0; matchIndx < dmatchesListOfMat.size() ; matchIndx++) {

            if (dmatchesListOfMat.get(matchIndx).toArray()[0].distance  < ratio * dmatchesListOfMat.get(matchIndx).toArray()[1].distance) {
                good_matchesList.addLast(dmatchesListOfMat.get(matchIndx).toArray()[0]);
            }
        }
        matches.fromList(good_matchesList);
        Log.d("matches", "ORG SIZE:" + matches.size() + "");
        return matches;
    }

	
	static MatOfDMatch filterMatchesByDistance(MatOfDMatch matches){
		List<DMatch> matches_original = matches.toList();
		List<DMatch> matches_filtered = new ArrayList<DMatch>();
		
		int DIST_LIMIT = 30;
		// Check all the matches distance and if it passes add to list of filtered matches  
		Log.d("DISTFILTER", "ORG SIZE:" + matches_original.size() + "");
		for (int i = 0; i < matches_original.size(); i++) {
			DMatch d = matches_original.get(i); 
			if (Math.abs(d.distance) <= DIST_LIMIT) {
				matches_filtered.add(d);				
			}
		}
		Log.d("DISTFILTER", "FIL SIZE:" + matches_filtered.size() + "");
		
		MatOfDMatch mat = new MatOfDMatch();
		mat.fromList(matches_filtered);
		return mat;
	}
	
	static MatOfDMatch filterMatchesByHomography(MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2, MatOfDMatch matches){
		List<Point> lp1 = new ArrayList<Point>(500);
		List<Point> lp2 = new ArrayList<Point>(500);
		
		KeyPoint[] k1 = keypoints1.toArray();
		KeyPoint[] k2 = keypoints2.toArray();
		
		
		List<DMatch> matches_original = matches.toList();		

		if (matches_original.size() < 4){
			MatOfDMatch mat = new MatOfDMatch();
			return mat;
		}
		
		// Add matches keypoints to new list to apply homography
		for(DMatch match : matches_original){
			Point kk1 = k1[match.queryIdx].pt;
			Point kk2 = k2[match.trainIdx].pt;
			lp1.add(kk1);
			lp2.add(kk2);
		}	

		MatOfPoint2f srcPoints = new MatOfPoint2f(lp1.toArray(new Point[0]));
		MatOfPoint2f dstPoints  = new MatOfPoint2f(lp2.toArray(new Point[0]));
		
		Mat mask = new Mat();
		Mat homography = Calib3d.findHomography(srcPoints, dstPoints, Calib3d.LMEDS, 0.2, mask);
		List<DMatch> matches_homo = new ArrayList<DMatch>();
		int size = (int) mask.size().height;
		for(int i = 0; i < size; i++){			
			if ( mask.get(i, 0)[0] == 1){
				DMatch d = matches_original.get(i);
				matches_homo.add(d);
			}
		}
		
		MatOfDMatch mat = new MatOfDMatch();
		mat.fromList(matches_homo);

		//-- Draw lines between the corners (the mapped object in the scene - image_2 )

		Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
		Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

		obj_corners.put(0, 0, new double[] {0,0});
		obj_corners.put(1, 0, new double[] {keypoints1.cols(),0});
		obj_corners.put(2, 0, new double[] {keypoints1.cols(),keypoints1.rows()});
		obj_corners.put(3, 0, new double[] {0,keypoints1.rows()});

		Core.perspectiveTransform(obj_corners,scene_corners, homography);
		Core.line(mat, new Point(scene_corners.get(0,0)), new Point(scene_corners.get(1,0)), new Scalar(0, 255, 0),4);
		Core.line(mat, new Point(scene_corners.get(1,0)), new Point(scene_corners.get(2,0)), new Scalar(0, 255, 0),4);
		Core.line(mat, new Point(scene_corners.get(2,0)), new Point(scene_corners.get(3,0)), new Scalar(0, 255, 0),4);
		Core.line(mat, new Point(scene_corners.get(3,0)), new Point(scene_corners.get(0,0)), new Scalar(0, 255, 0),4);

		return mat;
	}
	
	static Bitmap drawMatches(Mat img1, MatOfKeyPoint key1, Mat img2, MatOfKeyPoint key2, MatOfDMatch matches, boolean imageOnly){
		Mat out = new Mat();
		Mat im1 = new Mat();
		Mat im2 = new Mat(); 
		Imgproc.cvtColor(img1, im1, Imgproc.COLOR_BGR2RGB);
		Imgproc.cvtColor(img2, im2, Imgproc.COLOR_BGR2RGB);
		if ( imageOnly){
			MatOfDMatch emptyMatch = new MatOfDMatch();
			MatOfKeyPoint emptyKey1 = new MatOfKeyPoint();
			MatOfKeyPoint emptyKey2 = new MatOfKeyPoint();			
			Features2d.drawMatches(im1, emptyKey1, im2, emptyKey2, emptyMatch, out);
			Log.d("DISTFILTER", "imageOnlyE:"  + "");
		} else {
			Features2d.drawMatches(im1, key1, im2, key2, matches, out);
			Log.d("DISTFILTER", "imageOnly No:"  + "");
		}
		Bitmap bmp = Bitmap.createBitmap(out.cols(), out.rows(), Bitmap.Config.ARGB_8888);		
		Imgproc.cvtColor(out, out, Imgproc.COLOR_BGR2RGB);
		Core.putText(out, "FRAME", new Point(img1.width() / 2,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0,255,255),3);
		Core.putText(out, "MATCHED", new Point(img1.width() + img2.width() / 2,30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255,0,0),3);
		Utils.matToBitmap(out, bmp);

		//Core.line( img_matches, scene_corners[3] + Point2f( img_object.cols, 0), scene_corners[0] + Point2f( img_object.cols, 0), Scalar( 0, 255, 0), 4 );
		return bmp;
	}
}
