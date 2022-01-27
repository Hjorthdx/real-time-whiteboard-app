package com.whiteboardapp;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.whiteboardapp.common.AppUtils;
import com.whiteboardapp.controller.MatConverter;
import com.whiteboardapp.core.CaptureService;
import com.whiteboardapp.core.pipeline.Binarization;
import com.whiteboardapp.core.pipeline.ChangeDetector;
import com.whiteboardapp.core.pipeline.CornerDetector;
import com.whiteboardapp.core.pipeline.PerspectiveTransformer;
import com.whiteboardapp.core.pipeline.Segmentator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {

    // Initialize openCV library
    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("MainActivity: ", "Opencv is loaded!");
        } else {
            Log.d("MainActivity: ", "Open failed to load.");
        }
    }

    @Test
    public void findCorners_returnsCorrectCorners() {
        // Arrange
        String filePath = "test_image.png";
        Bitmap bitmap = loadBitmap(filePath);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        CornerDetector cornerDetector = new CornerDetector();

        // Act
        MatOfPoint2f actualCorners = cornerDetector.findCorners(mat);
        MatOfPoint2f expectedCorners = new MatOfPoint2f(new Point(346, 63), new Point(2123, 24), new Point(2150, 1288), new Point(376, 1328));

        // Assert
        Assert.assertArrayEquals(actualCorners.toArray(), expectedCorners.toArray());
    }

    // Loads bitmap from asset directory.
    private Bitmap loadBitmap(String fileName) {
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
        AssetManager assetManager = appContext.getAssets();

        InputStream stream = null;
        try {
            stream = assetManager.open(fileName);
        } catch (IOException e) {
            System.out.println("Unable to load file: " + fileName);
            e.printStackTrace();
        }

        return BitmapFactory.decodeStream(stream);
    }

    @Test
    public void getPerspective_returnsCorrectImage() {

        // ARRANGE
        // Read image into mat
        String refFilePath = "ref_perspective.jpg";
        Bitmap expectedBitmap = loadBitmap(refFilePath);

        String filePath = "test_image.png";
        Bitmap bitmap = loadBitmap(filePath);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);

        //  ACT
        CornerDetector cornerDetector = new CornerDetector();
        MatOfPoint2f corners = cornerDetector.findCorners(mat);
        PerspectiveTransformer transformer = new PerspectiveTransformer();
        Mat imgPerspectiveActual = transformer.getPerspective(mat, corners);
        Bitmap actualBitmap = MatConverter.matToBitmap(imgPerspectiveActual);
        AppUtils.saveWithName(actualBitmap, "actual_perspective");
        actualBitmap = loadImageFromStorage("actual_perspective.jpg");

        // ASSERT
        boolean isSame = expectedBitmap.sameAs(actualBitmap);
        Assert.assertTrue(isSame);

    }

    private Bitmap getBitmapChanges() {
        String filePath1 = "change1.jpg";
        String filePath2 = "change2.jpg";
        Bitmap bitmap1 = loadBitmap(filePath1);
        Bitmap bitmap2 = loadBitmap(filePath2);
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bitmap1, mat1);
        Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGB2GRAY);
        Mat mat2 = new Mat();
        Utils.bitmapToMat(bitmap2, mat2);
        Imgproc.cvtColor(mat2, mat2, Imgproc.COLOR_RGB2GRAY);

        ChangeDetector changeDetector = new ChangeDetector();
        Mat changesMat = changeDetector.detectChanges(mat1, mat2);
        changesMat = changeDetector.detectChanges(mat1, mat2);

        return MatConverter.matToBitmap(changesMat);
    }

    private Bitmap getBitmapBinarization() {
        String filePath = "gray.jpg";
        Bitmap bitmap = loadBitmap(filePath);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
        Mat binarizer = Binarization.binarize(mat);
        return MatConverter.matToBitmap(binarizer);
    }

    @Test
    public void binarization_returnsCorrectImage() {
//        Bitmap binarizationBitmap = getBitmapBinarization();
//        AppUtils.saveWithName(binarizationBitmap, "gray");

        Bitmap actualBitmap = getBitmapBinarization();
        AppUtils.saveWithName(actualBitmap, "actual_binarization");
        actualBitmap = loadImageFromStorage("actual_binarization.jpg");
//
//        // ARRANGE
        // Read image into mat
        String refFilePath = "ref_binarization.jpg";
        Bitmap expectedBitmap = loadBitmap(refFilePath);

//        // ASSERT
        boolean isSame = expectedBitmap.sameAs(actualBitmap);
        Assert.assertTrue(isSame);
    }

    @Test
    public void detectChanges_returnsCorrectImage() {
//        Bitmap changeBitmap = getBitmapChanges();
//        AppUtils.saveWithName(changeBitmap, "ref_changes");

        Bitmap actualBitmap = getBitmapChanges();
        AppUtils.saveWithName(actualBitmap, "actual_changes");
        actualBitmap = loadImageFromStorage("actual_changes.jpg");

        // ARRANGE
        // Read image into mat
        String refFilePath = "ref_changes.jpg";
        Bitmap expectedBitmap = loadBitmap(refFilePath);

        // ASSERT
        boolean isSame = expectedBitmap.sameAs(actualBitmap);
        Assert.assertTrue(isSame);
    }

    @Test
    public void tester_returnsCorrectImage() {
        // Arrange

        // Read image into mat
        String filePath = "test_image.png";
        Bitmap bitmap = loadBitmap(filePath);

        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);

        AppUtils.saveWithName(bitmap, "img1");
        AppUtils.saveWithName(bitmap, "img2");

        Bitmap img1 = loadImageFromStorage("img1.jpg");
        Bitmap img2 = loadImageFromStorage("img2.jpg");

        Mat img1Mat = new Mat();
        Utils.bitmapToMat(img1, img1Mat);
        Mat img2Mat = new Mat();
        Utils.bitmapToMat(img2, img2Mat);

        boolean isSame = img1.sameAs(img2);

        Assert.assertTrue(isSame);

        System.out.println("Hej! ");

    }

    public Bitmap loadImageFromStorage(String fileName) {
        String picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(picturesDir + "/saved_images");
        myDir.mkdirs();
        try {
            File f = new File(myDir, fileName);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getBitmapSegmentation() {
        String filePath = "test_seg.jpg";
        Bitmap bitmap = loadBitmap(filePath);
        Segmentator segmentator = new Segmentator(InstrumentationRegistry.getInstrumentation().getTargetContext());
        Mat segMap = segmentator.segmentate(bitmap);
        return MatConverter.matToBitmap(segMap);
    }

    @Test
    public void segmentate_returnsCorrectImage() {
//        Bitmap segmentationBitmap = getBitmapSegmentation();
//        AppUtils.saveWithName(segmentationBitmap, "ref_segmentation");

        Bitmap actualBitmap = getBitmapSegmentation();
        AppUtils.saveWithName(actualBitmap, "actual_segmentationn");
        actualBitmap = loadImageFromStorage("actual_segmentationn.jpg");
//
//        // ARRANGE
        // Read image into mat
        String refFilePath = "ref_segmentation.jpg";
        Bitmap expectedBitmap = loadBitmap(refFilePath);

//        // ASSERT
        boolean isSame = expectedBitmap.sameAs(actualBitmap);
        Assert.assertTrue(isSame);
    }

    private Bitmap getBitmapCaptureService() {

        String filePath1 = "capture1.jpg";
        String filePath2 = "capture2.jpg";
        Bitmap bitmap1 = loadBitmap(filePath1);
        Bitmap bitmap2 = loadBitmap(filePath2);
        Mat mat1 = new Mat();
        Utils.bitmapToMat(bitmap1, mat1);
        Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_RGB2BGR);
        Mat mat2 = new Mat();
        Utils.bitmapToMat(bitmap2, mat2);
        Imgproc.cvtColor(mat2, mat2, Imgproc.COLOR_RGB2BGR);

        CaptureService captureService = new CaptureService(mat1.width(), mat1.height(), InstrumentationRegistry.getInstrumentation().getTargetContext());
        Mat captureMat = captureService.capture(mat1);
        captureMat = captureService.capture(mat2);

        return MatConverter.matToBitmap(captureMat);
    }

    @Test
    public void captureService_returnsCorrectImage() {
//        Bitmap captureBitmap = getBitmapCaptureService();
//        AppUtils.saveWithName(captureBitmap, "ref_capture");

        //
        Bitmap actualBitmap = getBitmapCaptureService();
        AppUtils.saveWithName(actualBitmap, "actual_capture");
        actualBitmap = loadImageFromStorage("actual_capture.jpg");
//
//        // ARRANGE
        // Read image into mat
        String refFilePath = "ref_capture.jpg";
        Bitmap expectedBitmap = loadBitmap(refFilePath);

//        // ASSERT
        boolean isSame = expectedBitmap.sameAs(actualBitmap);
        Assert.assertTrue(isSame);

    }

}