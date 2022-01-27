package com.whiteboardapp.common;


import org.opencv.core.Point;

public class Calculator {

    public static double getDistanceBetweenPoints(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
    }

    public static double getDistanceBetweenPoints(Point point1, Point point2) {
        return getDistanceBetweenPoints(point1.x, point1.y, point2.x, point2.y);
    }

    public static double getDistanceBetweenPoints(double[] point1, double[] point2) {
        return getDistanceBetweenPoints(point1[0], point1[1], point2[0], point2[1]);
    }

}
