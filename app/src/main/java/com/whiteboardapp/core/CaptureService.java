package com.whiteboardapp.core;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.whiteboardapp.controller.MatConverter;
import com.whiteboardapp.core.pipeline.Binarization;
import com.whiteboardapp.core.pipeline.ChangeDetector;
import com.whiteboardapp.core.pipeline.Segmentator;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.whiteboardapp.common.AppUtils;

import static android.content.ContentValues.TAG;

public class CaptureService {
    private final Context appContext;
    private final Mat currentModel;
    private final ChangeDetector changeDetector;

    public CaptureService(int defaultWidth, int defaultHeight, Context appContext) {
        this.appContext = appContext;
        currentModel = new Mat(defaultHeight, defaultWidth, CvType.CV_8UC1);
        currentModel.setTo(new Scalar(255));
        changeDetector = new ChangeDetector();
    }

    // Runs image through the image processing pipeline
    public Mat capture(Mat imgBgr) {

        // Segmentation
        Log.d(TAG, "capture: Segmentation started.");
        Mat matPerspectiveRgb = new Mat();
        Imgproc.cvtColor(imgBgr, matPerspectiveRgb, Imgproc.COLOR_BGR2RGB);
        Segmentator segmentator = new Segmentator(appContext);
        Bitmap bitmapRgb = MatConverter.matToBitmap(matPerspectiveRgb);
        Mat imgSegMap = segmentator.segmentate(bitmapRgb);
        Log.d(TAG, "capture: Segmentation done.");

        // Binarize a gray scale version of the image.
        Mat imgWarpGray = new Mat();
        Imgproc.cvtColor(imgBgr, imgWarpGray, Imgproc.COLOR_BGR2GRAY);
        Mat imgBinarized = Binarization.binarize(imgWarpGray);

        // Remove segments before change detection.
        Mat currentModelCopy = removeSegmentArea(imgBinarized, currentModel, imgSegMap, imgBgr);

        // Change detection
        Mat imgPersistentChanges = changeDetector.detectChanges(imgBinarized, currentModelCopy);

        // Update current model with persistent changes.
        updateModel(imgBinarized, imgPersistentChanges);
        return currentModel;

    }

    // Removes segment area from image
    private Mat removeSegmentArea(Mat binarizedImg, Mat currentModel, Mat imgSegMap, Mat imgPerspective) {
        Mat currentModelCopy = new Mat();
        currentModel.copyTo(currentModelCopy);

        long startTime = System.currentTimeMillis();

//        for (int i = 0; i < binarizedImg.rows(); i++) {
//            for (int j = 0; j < binarizedImg.cols(); j++) {
//                if (imgSegMap.get(i, j)[0] == 255) {
//                    binarizedImg.put(i, j, 255);
//                    currentModelCopy.put(i, j, 255);
////                    imgPerspective.put(i, j, 255, 0, 0);
//                }
//            }
//        }

        byte[] bufferBinarized = AppUtils.getBuffer(binarizedImg);
        byte[] bufferSegmap = AppUtils.getBuffer(imgSegMap);
        byte[] bufferModel = AppUtils.getBuffer(currentModelCopy);

        for (int i = 0; i < bufferBinarized.length; i++) {
            if (bufferSegmap[i] == -1) {
                bufferBinarized[i] = -1;
                bufferModel[i] = -1;
            }
        }

        binarizedImg.put(0, 0, bufferBinarized);
        currentModelCopy.put(0, 0, bufferModel);

        long endTime = System.currentTimeMillis();
        System.out.println("Remove segment loop took: " + (endTime - startTime) + " milliseconds");
        return currentModelCopy;
    }

    private void updateModel(Mat imgBinarized, Mat imgPersistentChanges) {

        long startTime = System.currentTimeMillis();

        byte[] bufferModel = AppUtils.getBuffer(currentModel);
        byte[] bufferChanges = AppUtils.getBuffer(imgPersistentChanges);
        byte[] bufferBinarized = AppUtils.getBuffer(imgBinarized);

        for (int i = 0; i < bufferModel.length; i++) {
            if (bufferChanges[i] == -1) {
                bufferModel[i] = bufferBinarized[i];
            }
        }

//        for (int i = 0; i < currentModel.rows(); i++) {
//            for (int j = 0; j < currentModel.cols(); j++) {
//                if (imgPersistentChanges.get(i, j)[0] == 255) {
//                    currentModel.put(i, j, imgBinarized.get(i, j)[0]);
//                }
//            }
//        }

        currentModel.put(0, 0, bufferModel);

        long endTime = System.currentTimeMillis();
        System.out.println("Update model loop took: " + (endTime - startTime) + " milliseconds");
    }

}
