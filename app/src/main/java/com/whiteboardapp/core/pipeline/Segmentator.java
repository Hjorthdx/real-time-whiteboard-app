package com.whiteboardapp.core.pipeline;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

import com.whiteboardapp.common.AppUtils;

import org.checkerframework.checker.formatter.FormatUtil;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.segmenter.ColoredLabel;
import org.tensorflow.lite.task.vision.segmenter.Segmentation;
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.List;

// Segmentation using DeepLab model.
public class Segmentator {
    public final String TAG = "SegmentationTask";
    private final int NUM_THREADS = 4;
    private final String SEGMENTATION_MODEL_NAME = "deeplabv3_257_mv_gpu.tflite";
    private final int ALPHA_VALUE = 128;

    private ImageSegmenter imageSegmenter;
    private TensorImage maskTensor;
    private int[] pixelsGlobal;

    public Segmentator(Context context) {
        try {
            imageSegmenter = ImageSegmenter.createFromFile(context, SEGMENTATION_MODEL_NAME);
        } catch (Exception exception) {
            throw new RuntimeException("Exception occurred while loading model from file", exception);
        }
    }

    // Performs segmentation of the given image. 
    // Returns a Mat representing the resulting segmentation map 
    public Mat segmentate(Bitmap image) {
        long fullTimeExecutionTime = System.currentTimeMillis();
        ;

        // Do segmentation
        long imageSegmentationTime = System.currentTimeMillis();
        ;
        TensorImage tensorImage = TensorImage.fromBitmap(image);
        List<Segmentation> results = imageSegmenter.segment(tensorImage);
        imageSegmentationTime = System.currentTimeMillis() - imageSegmentationTime;
        Log.i(TAG, "Time to run the segmentation model: " + imageSegmentationTime + " ms");

        // Resize seg map to input image size.
        Bitmap maskBitmap = createMaskBitmapAndLabels(
                results.get(0), image.getWidth(),
                image.getHeight()
        );

        Mat imgSegMap = createImgSegMap(maskBitmap, image.getWidth(), image.getHeight());

        fullTimeExecutionTime = System.currentTimeMillis() - fullTimeExecutionTime;
        Log.i(TAG, "Total time in segmentation step: " + fullTimeExecutionTime + " ms");
        return imgSegMap;
    }

    // Method converted from Tensorflow Kotlin tutorial:
    // https://github.com/tensorflow/examples/tree/master/lite/examples/image_segmentation/android/lib_task_api/src/main/java/org/tensorflow/lite/examples/imagesegmentation/tflite
    private Bitmap createMaskBitmapAndLabels(Segmentation segmentation, int width, int height) {
        List<ColoredLabel> coloredLabels = segmentation.getColoredLabels();
        ArrayList<Integer> colors = new ArrayList<>();
        for (ColoredLabel coloredLabel : coloredLabels) {
            int rgb = coloredLabel.getArgb();
            // Create color setting alpha value (transparency) to 128 as the mask will be displayed on top of original image.
            colors.add(Color.argb(ALPHA_VALUE, Color.red(rgb), Color.green(rgb), Color.blue(rgb)));
        }
        // Use completely transparent for the background color.
        colors.set(0, Color.TRANSPARENT);

        // Create the mask bitmap with colors and the set of detected labels.
        maskTensor = segmentation.getMasks().get(0);
        byte[] maskArray = maskTensor.getBuffer().array();
        int[] pixels = new int[maskArray.length];
        for (int i = 0; i < maskArray.length; i++) {
            int color = colors.get(maskArray[i]);
            pixels[i] = color;
        }

        // Scale the maskBitmap to the same size as the input image.
        Bitmap maskBitmap = Bitmap.createBitmap(
                pixels, maskTensor.getWidth(), maskTensor.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        // XXX: Scaling her??
        return maskBitmap;

    }

    private Mat createImgSegMap(Bitmap maskBitmap, int width, int height) {

        // Create segmap
        Mat segMapBgr = new Mat();
        Utils.bitmapToMat(maskBitmap, segMapBgr);

        Mat segMapGrey = new Mat();
        Imgproc.cvtColor(segMapBgr, segMapGrey, Imgproc.COLOR_BGR2GRAY);

        Mat segMapBinary = new Mat(segMapGrey.rows(), segMapGrey.cols(), CvType.CV_8U);

        byte[] bufferSegGray = AppUtils.getBuffer(segMapGrey);
        byte[] bufferSegBinary = AppUtils.getBuffer(segMapBinary);

        for (int i = 0; i < bufferSegGray.length; i++) {
            if (bufferSegGray[i] == 68) {
                bufferSegBinary[i] = -1;
            } else {
                bufferSegBinary[i] = 0;

            }
        }
        segMapBinary.put(0, 0, bufferSegBinary);

        Imgproc.resize(segMapBinary, segMapBinary, new Size(width, height));
        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, new Size(20, 20));
        Mat imgSegMapDilated = new Mat();
        Imgproc.dilate(segMapBinary, imgSegMapDilated, kernel, new Point(-1, -1), 11);

        return imgSegMapDilated;
    }

}
