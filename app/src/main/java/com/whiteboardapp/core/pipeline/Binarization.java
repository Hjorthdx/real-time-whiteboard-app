package com.whiteboardapp.core.pipeline;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Binarization {

    private static final int MAX_THRESH_VALUE = 255;
    private static final int BLOCK_SIZE = 21;
    private static final int C = 4;
    private static final int BLUR_KERNEL_SIZE = 3;

    // Binarizes a gray scale image.
    public static Mat binarize(Mat imgGray) {
        Mat imgBinarizedThreshold = new Mat();
        Imgproc.adaptiveThreshold(imgGray, imgBinarizedThreshold, MAX_THRESH_VALUE, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, BLOCK_SIZE, C);

        // Clean image - removes most of the noise
        Mat imgBinarizedThresholdBlur = new Mat();
        Imgproc.medianBlur(imgBinarizedThreshold, imgBinarizedThresholdBlur, BLUR_KERNEL_SIZE);

        // Invert image (black on white background)
        Mat imgBinarizedFinal = new Mat();
        Core.bitwise_not(imgBinarizedThresholdBlur, imgBinarizedFinal);

        return imgBinarizedFinal;
    }
}
