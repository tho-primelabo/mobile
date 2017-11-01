package opencvdemo.learn2crack.com.opencvexample.opencv;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
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
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import opencvdemo.learn2crack.com.opencvexample.R;

/**
 * Created by DELL on 10/5/2017.
 */

public class MyView extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "thole";
    private CameraBridgeViewBase mOpenCvCameraView;
    private ImageView imageView;
    private Bitmap inputImage, inputImage2, outBitMap; // make bitmap from image resource

    ImageView matchDrawArea;
    ImageView matchDrawArea2;
    Button addBtn;
    private FeatureDetector detector;//= FeatureDetector.create(FeatureDetector.SIFT);
    ///////////////////DESCRIPTORS
    private DescriptorExtractor extractor;
    /*Mat firstImgMatOfKeyPoints = new Mat();*/

    private DescriptorMatcher matcher;// = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

    private Mat camPic;
    //ArrayList<Scene> scenes = new ArrayList<Scene>();
    Scene refScene;

    private Mat img1, img2;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.helloopencvlayout);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        inputImage = BitmapFactory.decodeResource(getResources(), R.drawable.film2);
        imageView = (ImageView) this.findViewById(R.id.refImageView2);

        inputImage2 = BitmapFactory.decodeResource(getResources(), R.drawable.python);

        matchDrawArea = (ImageView) findViewById(R.id.refImageView);
        matchDrawArea2 = (ImageView) findViewById(R.id.refImageView2);
        addBtn = (Button) findViewById(R.id.button1);

    }

    boolean showOriginal = true;

    public void cameraclick(View w) {
        showOriginal = !showOriginal;
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    if (detector == null) {
                        detector = FeatureDetector.create(FeatureDetector.ORB);
                    }
                    if (extractor == null) {
                        extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
                    }
                    if (matcher == null) {
                        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
                    }
                    //sift();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
       // OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public Mat sift(Mat rgba2) {
        Mat rgba = new Mat();
        //Mat rgba2 = new Mat();
        //MatOfDMatch matches = new MatOfDMatch();
        Mat descriptors = new Mat();
        Mat descriptors2 = new Mat();
        //Utils.bitmapToMat(img1, rgba);
        //Utils.bitmapToMat(inputImage2, rgba2);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();
        if (img1 != null) {
            Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2RGB);
        }
        Imgproc.cvtColor(rgba2, rgba2, Imgproc.COLOR_BGR2RGB);
       /* if (detector == null) {
            detector = FeatureDetector.create(FeatureDetector.ORB);
        }*/
        detector.detect(rgba, keyPoints);
        extractor.compute(rgba, keyPoints, descriptors);

        detector.detect(rgba2, keyPoints2);
        extractor.compute(rgba2, keyPoints2, descriptors2);

        //matcher.match(descriptors, descriptors2,  matches);
        //Log.i(TAG, "matches: " + matches.size() );


        MatOfDMatch good_matches = new MatOfDMatch();
        LinkedList<MatOfDMatch> dmatchesListOfMat = new LinkedList<>();
        if (rgba.rows() == rgba2.rows() && rgba.type() == rgba2.type()) {
            matcher.knnMatch(descriptors, descriptors2, dmatchesListOfMat, 2);
        }
        LinkedList<DMatch> good_matchesList = new LinkedList<>();
        double ratio = 0.8;
        for (int matchIndx = 0; matchIndx < dmatchesListOfMat.size(); matchIndx++) {

            if (dmatchesListOfMat.get(matchIndx).toArray()[0].distance < ratio * dmatchesListOfMat.get(matchIndx).toArray()[1].distance) {
                good_matchesList.addLast(dmatchesListOfMat.get(matchIndx).toArray()[0]);
            }
        }
        good_matches.fromList(good_matchesList);
        Log.i(TAG, "good_matches: " + good_matches.toArray().length + ":" + good_matchesList.isEmpty());
        //feature and connection colors
        Scalar RED = new Scalar(255, 0, 0);
        Scalar GREEN = new Scalar(0, 255, 0);
        //output image
        Mat outputImg = new Mat();
        MatOfByte drawnMatches = new MatOfByte();

        Log.i(TAG, "pointsAct: " + outputImg.rows() + ":" + outputImg.cols());


        Features2d.drawMatches(rgba, keyPoints, rgba2, keyPoints2, new MatOfDMatch(),
                outputImg, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
        Core.putText(outputImg, "FRAME", new Point(rgba.width() / 2, 30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255, 255), 2);
        if (good_matches.toArray().length < 30) {

            Core.putText(outputImg, "NG", new Point(rgba.width() + rgba2.width() / 2, 30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 2);
        } else {

            Core.putText(outputImg, "OK", new Point(rgba.width() + rgba2.width() / 2, 30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255, 0), 2);
        }

        //Imgproc.cvtColor(outputImg, outputImg, Imgproc.COLOR_BGR2RGB);
        //Bitmap bmp = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(outputImg, bmp);
        //imageView.setImageBitmap(bmp);
        return rgba2;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        camPic = inputFrame.rgba();
        return  shapeDetect(null);//sift(camPic.clone());//camPic;
    }

    public void takePicStandard(View w) {
        if (img1  != null) {
            img1.release();
        }
        img1 = camPic.clone();
        //Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2RGB);
        Bitmap bmp = Bitmap.createBitmap(img1.cols(), img1.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img1, bmp);
        // matchDrawArea.setVisibility(View.VISIBLE);
        matchDrawArea.setImageBitmap(bmp);
        //refScene = new Scene(camPic);
    }

    public void takePicRefer(View w) {
        if (img2 != null) {
            img2.release();
        }
        img2 = camPic.clone();
        //Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2RGB);
        Bitmap bmp = Bitmap.createBitmap(img2.cols(), img2.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img2, bmp);
        matchDrawArea2.setImageBitmap(bmp);
        //refScene = new Scene(camPic);
    }

    public void akaze(Mat rgba, Mat rgba2) {

        //MatOfDMatch matches = new MatOfDMatch();
        Mat descriptors = new Mat();
        Mat descriptors2 = new Mat();

        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        MatOfKeyPoint keyPoints2 = new MatOfKeyPoint();
        Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_BGR2RGB);
        Imgproc.cvtColor(rgba2, rgba2, Imgproc.COLOR_BGR2RGB);
       /* if (detector == null) {
            detector = FeatureDetector.create(FeatureDetector.ORB);
        }*/
        detector.detect(rgba, keyPoints);
        extractor.compute(rgba, keyPoints, descriptors);

        detector.detect(rgba2, keyPoints2);
        extractor.compute(rgba2, keyPoints2, descriptors2);
        MatOfDMatch good_matches = new MatOfDMatch();
        LinkedList<MatOfDMatch> dmatchesListOfMat = new LinkedList<>();
        if (descriptors.cols() == descriptors2.cols() && descriptors.type() == descriptors2.type()) {
            matcher.knnMatch(descriptors, descriptors2, dmatchesListOfMat, 2);
        }
        LinkedList<DMatch> good_matchesList = new LinkedList<>();
        double ratio = 0.8;
        for (int matchIndx = 0; matchIndx < dmatchesListOfMat.size(); matchIndx++) {

            if (dmatchesListOfMat.get(matchIndx).toArray()[0].distance < ratio * dmatchesListOfMat.get(matchIndx).toArray()[1].distance) {
                good_matchesList.addLast(dmatchesListOfMat.get(matchIndx).toArray()[0]);
            }
        }
        good_matches.fromList(good_matchesList);
        Log.i(TAG, "good_matches: " + good_matches.toArray().length + ":" + good_matchesList.isEmpty());
        //feature and connection colors
        Scalar RED = new Scalar(255, 0, 0);
        Scalar GREEN = new Scalar(0, 255, 0);
        //output image
        Mat outputImg = new Mat();
        MatOfByte drawnMatches = new MatOfByte();
        Features2d.drawMatches(rgba, keyPoints, rgba2, keyPoints2, new MatOfDMatch(),
                outputImg, GREEN, RED, drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);
        Core.putText(outputImg, "FRAME", new Point(rgba.width() / 2, 30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255, 255), 2);
        if (good_matches.toArray().length < 30) {

            Core.putText(outputImg, "NG", new Point(rgba.width() + rgba2.width() / 2, 30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255, 0, 0), 2);
        } else {

            Core.putText(outputImg, "OK", new Point(rgba.width() + rgba2.width() / 2, 30), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(0, 255, 0), 2);
        }
        //Imgproc.cvtColor(outputImg, outputImg, Imgproc.COLOR_BGR2RGB);
        // Bitmap bmp = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(), Bitmap.Config.ARGB_8888);
        outBitMap = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputImg, outBitMap);
        //matchDrawArea.setVisibility(View.GONE);
        //matchDrawArea2.setMaxHeight(500);
        //imageView.setImageBitmap(bmp);


    }

    public void compareClick(View w) {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.result_layout);
        dialog.setTitle("Result Compare");
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

        // set the custom dialog components - text, image and button
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        // image.setImageResource(R.drawable.ic_launcher);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        if (img1 == null) {
            img1 = new Mat();
            Utils.bitmapToMat(inputImage, img1);
        }
        akaze(img1, img2);
        image.setImageBitmap(outBitMap);
        dialog.show();
    }

    public void shapeDetect1(View v) {
        Mat bw = new Mat();
        Imgproc.Canny(img2, bw, 100, 300);

        Imgproc.blur(bw, bw, new Size(10,10));
       // Mat dest = Mat.zeros(bw.size(), CvType.CV_32SC1);
        Mat draw = new Mat(bw.size(), bw.type());

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(bw, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE);
       // Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Log.d("CONTOURS", "" + contours.size());
        Scalar White = new Scalar(0,255,0);
        // Draw contours in dest Mat
        //Imgproc.drawContours(dest, contours, -1, new Scalar(255, 255, 255));
        for (MatOfPoint contour: contours) {
            //Core.fillPoly(dest, Arrays.asList(contour), White);
            RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            drawRotatedRect(img2, rotatedRect, White, 4);
        }

        // if any contour exist...
      /*  if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
        {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
            {
                Imgproc.drawContours(img2, contours, idx, new Scalar(250, 0, 0));
            }
        }*/
        Bitmap bmp = Bitmap.createBitmap(draw.cols(), draw.rows(), Bitmap.Config.ARGB_8888);

      /*  for (MatOfPoint contour: contours) {
            RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            drawRotatedRect(dest, rotatedRect, green, 4);
        }*/
        Utils.matToBitmap(img2, bmp);
        matchDrawArea.setImageBitmap(bmp);
    }
    public static void drawRotatedRect(Mat image, RotatedRect rotatedRect, Scalar color, int thickness) {
        Point[] vertices = new Point[4];
        rotatedRect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(image, Arrays.asList(points), -1, color, thickness);
        //drawContours( drawing, contours, i, color, 2, 8, hierarchy, 0, Point() );
    }
    public   Mat shapeDetect(View v) {
        System.out.println("Started....");
        System.out.println("Loading images...");
        Mat objectImage = img1;
        //Mat objectImage = new Mat();
        if (objectImage == null) {
            objectImage = new Mat();
            Utils.bitmapToMat(inputImage, objectImage);
        }
        Mat sceneImage = camPic.clone();
       //if (img1 != null && img2 != null) {
           Imgproc.cvtColor(objectImage, objectImage, Imgproc.COLOR_BGR2RGB);
           Imgproc.cvtColor(sceneImage, sceneImage, Imgproc.COLOR_BGR2RGB);
           // Mat objectImage = img1.r;
           // Mat sceneImage = img2.clone();

           MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
           //FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SURF);
           System.out.println("Detecting key points...");
           detector.detect(objectImage, objectKeyPoints);
           KeyPoint[] keypoints = objectKeyPoints.toArray();
           System.out.println(keypoints);

           MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
           //DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
           System.out.println("Computing descriptors...");
           extractor.compute(objectImage, objectKeyPoints, objectDescriptors);

           // Create the matrix for output image.
           Mat outputImage = new Mat(objectImage.rows(), objectImage.cols(), Highgui.CV_LOAD_IMAGE_COLOR);
           Scalar newKeypointColor = new Scalar(255, 0, 0);

           System.out.println("Drawing key points on object image...");
           Features2d.drawKeypoints(objectImage, objectKeyPoints, outputImage, newKeypointColor, 0);

           // Match object image with the scene image
           MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
           MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
           System.out.println("Detecting key points in background image...");
           detector.detect(sceneImage, sceneKeyPoints);
           System.out.println("Computing descriptors in background image...");
           extractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);

           Mat matchoutput = new Mat(sceneImage.rows() * 2, sceneImage.cols() * 2, Highgui.CV_LOAD_IMAGE_COLOR);
           Scalar matchestColor = new Scalar(0, 255, 0);

           List<MatOfDMatch> matches = new LinkedList<MatOfDMatch>();
           // DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
           if (objectDescriptors.cols() == sceneDescriptors.cols() && objectDescriptors.type() == sceneDescriptors.type()) {
               matcher.knnMatch(objectDescriptors, sceneDescriptors, matches, 2);
           }
           System.out.println("Matching object and scene images..." + matches.size());
           System.out.println("Calculating good match list...");
           LinkedList<DMatch> goodMatchesList = new LinkedList<DMatch>();

           float nndrRatio = 0.8f;

           for (int i = 0; i < matches.size(); i++) {
               MatOfDMatch matofDMatch = matches.get(i);
               DMatch[] dmatcharray = matofDMatch.toArray();
               if (dmatcharray.length > 1) {
                   DMatch m1 = dmatcharray[0];
                   DMatch m2 = dmatcharray[1];


                   if (m1.distance <= m2.distance * nndrRatio) {
                       goodMatchesList.addLast(m1);

                   }
               }
           }


           if (goodMatchesList.size() >= 30) {
               System.out.println("Object Found!!!");

               List<KeyPoint> objKeypointlist = objectKeyPoints.toList();
               List<KeyPoint> scnKeypointlist = sceneKeyPoints.toList();

               LinkedList<Point> objectPoints = new LinkedList<>();
               LinkedList<Point> scenePoints = new LinkedList<>();

               //RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));

               for (int i = 0; i < goodMatchesList.size(); i++) {
                   objectPoints.addLast(objKeypointlist.get(goodMatchesList.get(i).queryIdx).pt);
                   scenePoints.addLast(scnKeypointlist.get(goodMatchesList.get(i).trainIdx).pt);
               }

               MatOfPoint2f objMatOfPoint2f = new MatOfPoint2f();
               objMatOfPoint2f.fromList(objectPoints);
               MatOfPoint2f scnMatOfPoint2f = new MatOfPoint2f();
               scnMatOfPoint2f.fromList(scenePoints);

               Mat homography = Calib3d.findHomography(objMatOfPoint2f, scnMatOfPoint2f, Calib3d.RANSAC, 3);

               Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
               Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

               obj_corners.put(0, 0, new double[]{0, 0});
               obj_corners.put(1, 0, new double[]{objectImage.cols(), 0});
               obj_corners.put(2, 0, new double[]{objectImage.cols(), objectImage.rows()});
               obj_corners.put(3, 0, new double[]{0, objectImage.rows()});

               System.out.println("Transforming object corners to scene corners...");
               Core.perspectiveTransform(obj_corners, scene_corners, homography);

               Mat img = new Mat();
               Imgproc.cvtColor(sceneImage, img, Imgproc.COLOR_BGR2RGB);
               Core.line(img, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 4);
               Core.line(img, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 4);
               Core.line(img, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
               Core.line(img, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);

               System.out.println("Drawing matches image...:" + goodMatchesList.size());
               MatOfDMatch goodMatches = new MatOfDMatch();
               goodMatches.fromList(goodMatchesList);

               Features2d.drawMatches(objectImage, objectKeyPoints, sceneImage, sceneKeyPoints, goodMatches, matchoutput, matchestColor, newKeypointColor, new MatOfByte(), 2);

              /* Bitmap bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);


               Utils.matToBitmap(img, bmp);*/
               //matchDrawArea.setImageBitmap(bmp);
               camPic = img;

               // Bitmap bmp2 = Bitmap.createBitmap(img1.cols(), img1.rows(), Bitmap.Config.ARGB_8888);
               //matchDrawArea2.setImageBitmap(bmp2);
                /*Highgui.imwrite("output//outputImage.jpg", outputImage);
                Highgui.imwrite("output//matchoutput.jpg", matchoutput);
                Highgui.imwrite("output//img.jpg", img);*/
           } else {
               System.out.println("Object Not Found");
           }
       //}
        System.out.println("Ended....");
        return camPic;
    }

}
