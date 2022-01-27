package com.whiteboardapp.core;


import org.opencv.core.Mat;

import java.util.List;

public class MatPrint {

    public static void printMatList(List<Mat> list) {
        for (Mat e : list) {
            printMat(e);
        }
    }

    public static void printMat(Mat matToPrint) {
        System.out.println("Cols:" + matToPrint.cols() + "Rows:" + matToPrint.rows());
        for (int i = 0; i < matToPrint.cols(); i++) {
            for (int j = 0; j < matToPrint.rows(); j++) {
                for (int k = 0; k < matToPrint.get(j, i).length; k++) {
                    System.out.println("[" + j + "]" + "[" + i + "]" + "[" + k + "]" + matToPrint.get(j, i)[k]);
                }
            }
        }
    }
}
