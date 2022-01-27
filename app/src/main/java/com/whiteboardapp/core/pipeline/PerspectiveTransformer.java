package com.whiteboardapp.core.pipeline;


import com.whiteboardapp.common.Calculator;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

// Transforms the perspective from given set of points.
public class PerspectiveTransformer {

    private static final int CROP_MARGIN = 10;

    // Limit dimensions to ensure no extreme outliers in the given cornerpoints.
    private static final int MIN_DIMENSION = 100;
    private static final int MAX_DIMENSION = 5000;

    private boolean hasExtremeDimensions(double maxWidth, double maxHeight) {
        return maxWidth < MIN_DIMENSION || maxHeight < MIN_DIMENSION || maxWidth > MAX_DIMENSION || maxHeight > MAX_DIMENSION;
    }

    public Mat getPerspective(Mat imgRgb, MatOfPoint2f cornerPoints) {

        // Approx target corners.
        double[] tl = cornerPoints.get(0, 0);
        double[] tr = cornerPoints.get(1, 0);
        double[] br = cornerPoints.get(2, 0);
        double[] bl = cornerPoints.get(3, 0);

        double widthBtm = Calculator.getDistanceBetweenPoints(bl, br);
        double widthTop = Calculator.getDistanceBetweenPoints(tl, tr);
        double maxWidth = Math.max(widthBtm, widthTop);

        double heightRight = Calculator.getDistanceBetweenPoints(br, tr);
        double heightLeft = Calculator.getDistanceBetweenPoints(bl, tl);
        double maxHeight = Math.max(heightRight, heightLeft);

        // Ensure no extreme dimensions.
        if (hasExtremeDimensions(maxWidth, maxHeight)) {
            return null;
        }

        // Create rectangle from approximated corners.
        MatOfPoint2f targetCorners = new MatOfPoint2f(
                new Point(0, 0), // tl
                new Point(maxWidth - 1, 0), // tr
                new Point(maxWidth - 1, maxHeight - 1), // br
                new Point(0, maxHeight - 1)); // bl

        //	Get matrix for image perspective perspective
        //	Note: Target corners represents the "real"/actual dimensions of the input points i.e. actual
        //	ratio of paper.
        Mat perspectiveMatrix = Imgproc.getPerspectiveTransform(cornerPoints, targetCorners);

        //	Perform perspective transformation.
        //	Note: The given width and height are for cropping only i.e. does not affect how image is transformed.
        Mat imgPerspective = new Mat();

        Imgproc.warpPerspective(imgRgb, imgPerspective, perspectiveMatrix, new Size(maxWidth, maxHeight));

        Mat croppedMat = cropAndResize(imgPerspective, CROP_MARGIN);

        return croppedMat;
    }

    // Crops image according to specified margin.
    private Mat cropAndResize(Mat imgPerspective, int cropMargin) {
        Rect cropRect = new Rect(cropMargin, cropMargin, imgPerspective.width() - cropMargin, imgPerspective.height() - cropMargin);
        Mat croppedMat = new Mat(imgPerspective, cropRect);
        Mat resizedMat = new Mat();
        Imgproc.resize(croppedMat, resizedMat, new Size(imgPerspective.width(), imgPerspective.height()));
        return resizedMat;
    }

}
