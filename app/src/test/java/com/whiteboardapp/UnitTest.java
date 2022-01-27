package com.whiteboardapp;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.whiteboardapp.core.pipeline.CornerDetector;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class UnitTest {

    // Initialize openCV library
    static {
        if (OpenCVLoader.initDebug()) {
            System.out.println("Opencv is loaded!");
        } else {
            System.out.println("Open failed to load.");
        }
    }

    @Test
    public void findCorners_returnsCorrectCorners() {

        // Arrange
        String filePath = "test_image.png";
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(filePath);
        Bitmap bmp = BitmapFactory.decodeStream(in);
        System.out.println("Hej!");

//                Mat mat = new Mat();
//        Utils.bitmapToMat(bitmap, mat);
//        CornerDetector cornerDetector = new CornerDetector();
//
//        // Act
//        MatOfPoint2f actualCorners = cornerDetector.findCorners(mat);
//        MatOfPoint2f expectedCorners = new MatOfPoint2f(new Point(346, 63), new Point(2123, 24), new Point(2150, 1288), new Point(376, 1328));
//
//        // Assert
//        Assert.assertArrayEquals(actualCorners.toArray(), expectedCorners.toArray());
    }

//    // Loads bitmap from asset directory.
//    private Bitmap loadBitmap(String fileName) {
//        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
//        AssetManager assetManager = appContext.getAssets();
//
//        InputStream stream = null;
//        try {
//            stream = assetManager.open(fileName);
//        } catch (IOException e) {
//            System.out.println("Unable to load file: " + fileName);
//            e.printStackTrace();
//        }
//
//        return BitmapFactory.decodeStream(stream);
//    }

}