package opencvdemo.learn2crack.com.opencvexample;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by DELL on 9/27/2017.
 */

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    private int w, h;
    private CameraBridgeViewBase mOpenCvCameraView;
    TextView tvName;
    Scalar RED = new Scalar(255, 0, 0);
    Scalar GREEN = new Scalar(0, 255, 0);
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    DescriptorMatcher matcher;
    Mat descriptors2,descriptors1;
    Mat img1;
    MatOfKeyPoint keypoints1,keypoints2;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                   /* try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    private void initializeOpenCVDependencies() throws IOException {
        //Log.i(TAG, "initializeOpenCVDependencies");
        mOpenCvCameraView.enableView();
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        img1 = new Mat();
        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open("test.jpeg");
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        Utils.bitmapToMat(bitmap, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
        img1.convertTo(img1, CvType.CV_8U); //converting the image to match with the type of the cameras image
        //Log.i(TAG, "initializeOpenCVDependencies 111");
        descriptors1 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);

        descriptor.compute(img1, keypoints1, descriptors1);
        //Log.e("keypoints1", keypoints1.toString());

    }
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
       // w = width;
        //h = height;
    }

    public void onCameraViewStopped() {
    }


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

       //return recognize(inputFrame.rgba());
        return inputFrame.rgba();
    }
    public Mat recognize(Mat aInputFrame) {

        Imgproc.cvtColor(aInputFrame, aInputFrame, Imgproc.COLOR_RGB2GRAY);
        descriptors2 = new Mat();
        keypoints2 = new MatOfKeyPoint();
        detector.detect(aInputFrame, keypoints2);
        descriptor.compute(aInputFrame, keypoints2, descriptors2);

        // Matching
        MatOfDMatch matches = new MatOfDMatch();
        if (img1.type() == aInputFrame.type()) {
            if ( descriptors1 != null && descriptors2 != null &&
                    descriptors1.type() == descriptors2.type() && descriptors1.cols() == descriptors2.cols()) {
                Log.e("descriptors1", String.valueOf(descriptors1.type()) + ":" +String.valueOf(descriptors2.type()));
                matcher.match(descriptors1, descriptors2, matches);
            }
        } else {
            return aInputFrame;
        }
        List<DMatch> matchesList = matches.toList();

        Double max_dist = 0.0;
        Double min_dist = 200.0;

        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist)
                min_dist = dist;
            if (dist > max_dist)
                max_dist = dist;
        }

        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance <= (1.4 * min_dist))
                good_matches.addLast(matchesList.get(i));
        }

        MatOfDMatch goodMatches = new MatOfDMatch();
        goodMatches.fromList(good_matches);
        Log.e("good_matches:", String.valueOf(good_matches.size()));
        Mat outputImg = aInputFrame.clone();
        MatOfByte drawnMatches = new MatOfByte();
        if (aInputFrame.empty() || aInputFrame.cols() < 1 || aInputFrame.rows() < 1) {
            return aInputFrame;
        }
        //Log.e("thole", keypoints1.toString());
        if (good_matches.size() >= 50) {
            //Log.e("thole2222222", aInputFrame.toString());
            //Features2d.drawMatches(img1, keypoints1, aInputFrame, keypoints2, goodMatches, outputImg, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
            //Log.e("thole333333", aInputFrame.toString());

            List<KeyPoint> objKeypointlist = keypoints1.toList();
            List<KeyPoint> scnKeypointlist = keypoints2.toList();

            LinkedList<Point> objectPoints = new LinkedList<>();
            LinkedList<Point> scenePoints = new LinkedList<>();

            for (int i = 0; i < good_matches.size(); i++) {
                objectPoints.addLast(objKeypointlist.get(good_matches.get(i).queryIdx).pt);
                scenePoints.addLast(scnKeypointlist.get(good_matches.get(i).trainIdx).pt);
            }

            MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
            objMatOfPoint2f.fromList(objectPoints);
            MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
            scnMatOfPoint2f.fromList(scenePoints);
            Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

            Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
            Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

            obj_corners.put(0, 0, new double[]{0, 0});
            obj_corners.put(1, 0, new double[]{descriptors2.cols(), 0});
            obj_corners.put(2, 0, new double[]{descriptors2.cols(), descriptors2.rows()});
            obj_corners.put(3, 0, new double[]{0, descriptors2.rows()});

            Log.e(TAG, "Transforming object corners to scene corners...");
            Core.perspectiveTransform(obj_corners, scene_corners, homography);

            //Mat img = Highgui.imread(descriptors2, Highgui.CV_LOAD_IMAGE_COLOR);


            Log.e(TAG, "Drawing matches image...");
            //MatOfDMatch goodMatches = new MatOfDMatch();
            //goodMatches.fromList(goodMatchesList);

            //Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage
            Features2d.drawMatches(img1, keypoints1, aInputFrame, keypoints2, goodMatches, outputImg, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
            //Core.rectangle();
            Imgproc.resize(outputImg, outputImg, aInputFrame.size());
            Core.line(outputImg,
                    new Point(scene_corners.get(0,0)[0] + outputImg.cols(), scene_corners.get(0,0)[1]),
                    new Point(scene_corners.get(1,0)[0] + outputImg.cols(), scene_corners.get(1,0)[1]),
                    RED, 4);
            Core.line(outputImg,
                    new Point(scene_corners.get(1, 0)[0] + outputImg.cols(), scene_corners.get(1,0)[1]),
                    new Point(scene_corners.get(2, 0)[0] + outputImg.cols(), scene_corners.get(2,0)[1]),
                    RED, 4);
            Core.line(outputImg,
                    new Point(scene_corners.get(2, 0)[0] + outputImg.cols(), scene_corners.get(2,0)[1]),
                    new Point(scene_corners.get(3, 0)[0] + outputImg.cols(), scene_corners.get(3,0)[1]),
                    RED, 4);
            Core.line(outputImg,
                    new Point(scene_corners.get(3, 0)[0] + outputImg.cols(), scene_corners.get(3,0)[1]),
                    new Point(scene_corners.get(0, 0)[0] + outputImg.cols(), scene_corners.get(0,0)[1]),
                    RED, 4);
            /*Core.line(img1,
                    new Point(scene_corners.get(0,0)[0] + outputImg.cols(), scene_corners.get(0,0)[1]),
                    new Point(scene_corners.get(1,0)[0] + outputImg.cols(), scene_corners.get(1,0)[1]),
                    new Scalar(0, 255, 0),4);*/
            Bitmap imageMatched = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(), Bitmap.Config.RGB_565);//need to save bitmap
            Utils.matToBitmap(outputImg, imageMatched);
            //imageView.setImageBitmap(imageMatched);
            //return outputImg;
        }


        return outputImg;
    }
}
