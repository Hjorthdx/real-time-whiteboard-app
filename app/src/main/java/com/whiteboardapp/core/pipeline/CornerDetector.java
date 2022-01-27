package com.whiteboardapp.core.pipeline;


import android.util.Log;

import com.whiteboardapp.common.Calculator;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Detects corners in an image.
public class CornerDetector {

    private static final String TAG = "CornerDetector";
    private final int THRESHOLD_CANNY_MIN = 20;
    private final int THRESHOLD_CANNY_MAX = 150;

    private final int BLUR_KERNEL_SIZE = 5;
    private final int GAUSS_SIGMA = 1;

    private final int DILATION_KERNEL_SIZE = 3;

    // Finds corners in an image.
    public MatOfPoint2f findCorners(Mat imgBgr) {
        Mat imgEdges = makeEdgeImage(imgBgr);
        MatOfPoint2f cornerPoints = getCorners(imgEdges);
        if (cornerPoints.height() == 4) {
            cornerPoints = orderPoints(cornerPoints);
        }

        return cornerPoints;
    }

    // Finds edges in an image.
    private Mat makeEdgeImage(Mat imgBgr) {

        // Convert to gray scale.
        Mat imgGray = new Mat();
        Imgproc.cvtColor(imgBgr, imgGray, Imgproc.COLOR_BGR2GRAY);

        // Blur the image to remove noise.
        Mat imgBlur = new Mat();
        Imgproc.GaussianBlur(imgGray, imgBlur, new Size(BLUR_KERNEL_SIZE, BLUR_KERNEL_SIZE), GAUSS_SIGMA);

        // Find edges.
        Mat imgEdgesCanny = new Mat();
        Imgproc.Canny(imgBlur, imgEdgesCanny, THRESHOLD_CANNY_MIN, THRESHOLD_CANNY_MAX);

        // Enhance edges with dilation
        Mat imgEdgesDilated = new Mat();
        Mat dilationKernel = Mat.ones(new Size(DILATION_KERNEL_SIZE, DILATION_KERNEL_SIZE), CvType.CV_8U);

        Imgproc.dilate(imgEdgesCanny, imgEdgesDilated, dilationKernel);

        return imgEdgesDilated;
    }

    private MatOfPoint2f getCorners(Mat imgEdges) {
        // Find contours
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgEdges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size() == 0) {
            return new MatOfPoint2f();
        }

        // Find largest shape
        MatOfPoint2f shapePoints = findLargestShapePoints(contours);
        MatOfPoint2f cornerPoints = approxCornerPoints(shapePoints, imgEdges);
        return cornerPoints;

    }

    private MatOfPoint2f findLargestShapePoints(ArrayList<MatOfPoint> contours) {
        double PERIMETER_MARGIN_PERCENT = 0.02;
        double maxPerimeter = 0;
        MatOfPoint2f shapePoints = null;

        for (MatOfPoint contour : contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            // Find contour with the largest perimeter.
            double perimeter = Imgproc.arcLength(contour2f, false);
            if (perimeter > maxPerimeter) {
                MatOfPoint2f approxShapePoints = new MatOfPoint2f();

                Imgproc.approxPolyDP(contour2f, approxShapePoints, PERIMETER_MARGIN_PERCENT * perimeter, false);
                shapePoints = approxShapePoints;
                maxPerimeter = perimeter;
            }

        }

        Log.d(TAG, "findLargestShapePoints: Largest shape point count: " + shapePoints.size().height + "(h) " + shapePoints.size().width + "(w)");
        return shapePoints;
    }

    // Approximates _up_to_ 4 corner points based on points from a given shape
    // and the corners of an image containing the shape.
    private MatOfPoint2f approxCornerPoints(MatOfPoint2f shapePoints, Mat img) {

        // Corners of the original image.
        List<Point> imgCorners = Arrays.asList(
                new Point(0, 0),
                new Point(img.width(), 0),
                new Point(0, img.height()),
                new Point(img.width(), img.height())
        );

        MatOfPoint2f cornerPoints = new MatOfPoint2f();
        cornerPoints.alloc(4);

        // Create list in order to be able to remove points from list later.
        List<Point> shapePointsList = new ArrayList<>(shapePoints.toList());

        for (int i = 0; i < imgCorners.size(); i++) {

            // Break out of loop when no more points left.
            if (shapePointsList.size() == 0) {
                break;
            }
            int indexOfMinDistance = getMinDistanceIndex(shapePointsList, imgCorners.get(i));

            cornerPoints.put(i, 0, shapePointsList.get(indexOfMinDistance).x, shapePointsList.get(indexOfMinDistance).y);
            shapePointsList.remove(indexOfMinDistance);
        }

        return cornerPoints;
    }

    // Finds the index of the point with a minimum distance to a target point.
    private int getMinDistanceIndex(List<Point> points, Point targetPoint) {
        int indexOfMinDistance = -1;
        double minDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < points.size(); i++) {
            double distance = Calculator.getDistanceBetweenPoints(targetPoint, points.get(i));
            // Found a new min distance.
            if (minDistance > distance) {
                indexOfMinDistance = i;
                minDistance = distance;
            }
        }
        return indexOfMinDistance;
    }

    // Orders points according to a fixed order.
    public static MatOfPoint2f orderPoints(MatOfPoint2f cornerPoints) {

        double diff;
        double sum;

        double diffMin = Double.POSITIVE_INFINITY;
        int diffMinIndex = -1;
        double diffMax = Double.NEGATIVE_INFINITY;
        int diffMaxindex = -1;

        double sumMin = Double.POSITIVE_INFINITY;
        int sumMinIndex = -1;
        double sumMax = Double.NEGATIVE_INFINITY;
        int sumMaxIndex = -1;

        for (int i = 0; i < cornerPoints.height(); i++) {
            diff = cornerPoints.get(i, 0)[1] - cornerPoints.get(i, 0)[0];
            sum = cornerPoints.get(i, 0)[1] + cornerPoints.get(i, 0)[0];

            if (diff < diffMin) {
                diffMinIndex = i;
                diffMin = diff;
            }
            if (diff > diffMax) {
                diffMaxindex = i;
                diffMax = diff;
            }
            if (sum < sumMin) {
                sumMinIndex = i;
                sumMin = sum;
            }
            if (sum > sumMax) {
                sumMaxIndex = i;
                sumMax = sum;
            }

        }

        MatOfPoint2f orderedCornerPoints = new MatOfPoint2f();
        orderedCornerPoints.alloc(4);
        orderedCornerPoints.put(0, 0, cornerPoints.get(sumMinIndex, 0)); // tl
        orderedCornerPoints.put(1, 0, cornerPoints.get(diffMinIndex, 0)); // tr
        orderedCornerPoints.put(2, 0, cornerPoints.get(sumMaxIndex, 0)); // br
        orderedCornerPoints.put(3, 0, cornerPoints.get(diffMaxindex, 0)); // bl

        return orderedCornerPoints;

    }

    // Draws corners to an image.
    private void drawCorners(MatOfPoint2f cornerPoints, Mat imgBgr) {
        for (Point p : cornerPoints.toList()) {
            Imgproc.circle(imgBgr, p, 30, new Scalar(0, 255, 0), -1);
        }
    }
}