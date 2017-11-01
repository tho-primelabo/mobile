package opencvdemo.learn2crack.com.opencvexample.opencv;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.google.android.gms.vision.CameraSource;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import opencvdemo.learn2crack.com.opencvexample.R;

/**
 * Created by DELL on 9/28/2017.
 */

public class SelfFieActivity  extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, Camera.PictureCallback, Camera.ShutterCallback {
    private static final String TAG = "OCVSample::Activity";
    private Mat                    mRgba;
    private Mat                    mGray;
    private CameraBridgeViewBase   mOpenCvCameraView;
    private File cascadeFile;
    private CascadeClassifier cascadeClassifier;
    private String mPictureFileName;
    private  int mAbsoluteFaceSize ;
    private Camera mCamera ;
    Scalar GREEN = new Scalar(0, 255, 0);
    private Map<Integer, Integer> rectBuckts = new HashMap<>();
    private Map<Integer, Rect>rectCue = new HashMap<>();

	public SelfFieActivity() {
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
        mOpenCvCameraView.setCameraIndex(1);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
                mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //Flip around the Y axis
        Core.flip(inputFrame.rgba(), mRgba, 1);
        Core.flip(inputFrame.gray(),mGray,1);
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            /*if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }*/
        }

        MatOfRect closedHands = new MatOfRect();
        long timing = System.currentTimeMillis();
        if (cascadeClassifier != null)
            cascadeClassifier.detectMultiScale(mGray, closedHands, 1.1, 2, 2,new
                    Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Log.e("Timing", Long.toString(System.currentTimeMillis() - timing));
        Rect[] facesArray = closedHands.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
                    GREEN, 3);
            Point quatnizedTL=new Point(((int)(facesArray[i].tl().x/100))*100,
                    ((int)(facesArray[i].tl().y/100))*100);
            Point quatnizedBR=new Point(((int)(facesArray[i].br().x/100))*100,
                    ((int)(facesArray[i].br().y/100))*100);
            int bucktID=quatnizedTL.hashCode()+quatnizedBR.hashCode()*2;
            if(rectBuckts.containsKey(bucktID))
            {
                rectBuckts.put(bucktID, rectBuckts.get(bucktID)+1);
                rectCue.put(bucktID, new Rect(quatnizedTL,quatnizedBR));
            }
            else
            {
                rectBuckts.put(bucktID, 1);
            }
        }
        int maxDetections=0;
        int maxDetectionsKey=0;
        for(Map.Entry<Integer,Integer> e : rectBuckts.entrySet())
        {
            if(e.getValue()>maxDetections)
            {
                maxDetections=e.getValue();
                maxDetectionsKey=e.getKey();
            }
        }
        if(maxDetections>5)
        {
            Core.rectangle(mRgba, rectCue.get(maxDetectionsKey).tl(),
                    rectCue.get(maxDetectionsKey).br(), GREEN, 3);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateandTime = sdf.format(new Date());
            String fileName = Environment.getExternalStorageDirectory().getPath() +
                    "/sample_picture_" + currentDateandTime + ".jpg";
            takePicture(fileName);
           /* Notification.MessagingStyle.Message msg = handler.obtainMessage();
            msg.arg1 = 1;
            Bundle b=new Bundle();
            b.putString("msg", fileName + " saved");
            msg.setData(b);
            handler.sendMessage(msg);*/
            rectBuckts.clear();
        }
        mRgba=inputFrame.rgba();
        return mRgba;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.haarhand);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        cascadeFile = new File(cascadeDir, "haarhand.xml");
                        FileOutputStream os = new FileOutputStream(cascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();os.close();
                            //Initialize the Cascade Classifier object using the
                            // trained cascade file
                        cascadeClassifier = new
                                CascadeClassifier(cascadeFile.getAbsolutePath());
                        if (cascadeClassifier.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            cascadeClassifier = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " +
                                    cascadeFile.getAbsolutePath());
                        cascadeDir.delete();
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    mOpenCvCameraView.enableView();
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
    public void onPictureTaken(byte[] data, android.hardware.Camera mCamera) {
        Log.i(TAG, "Saving a bitmap to file");
        // The camera preview was automatically stopped. Start it
        // again.
        // again.
        mCamera.startPreview();
        mCamera.setPreviewCallback((android.hardware.Camera.PreviewCallback) this);
        // Write the image in a file (in jpeg format)
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFileName);
            fos.write(data);
            fos.close();
        } catch (java.io.IOException e) {
            Log.e("PictureDemo", "Exception in photoCallback", e);
        }
    }

    public void takePicture(final String fileName) {
        Log.i(TAG, "Taking picture");
        this.mPictureFileName = fileName;
        // Postview and jpeg are sent in the same buffers if the
        //queue is not empty when performing a capture.
        // Clear up buffers to avoid mCamera.takePicture to be stuck
        //because of a memory issue
        if (mCamera == null) {
            mCamera = Camera.open();
        }
        mCamera.setPreviewCallback(null);
        // PictureCallback is implemented by the current class
        try {
            mCamera.takePicture(this, this, this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onShutter() {

    }
}
