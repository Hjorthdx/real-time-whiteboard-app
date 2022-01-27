package com.whiteboardapp.controller;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.whiteboardapp.R;
import com.whiteboardapp.core.CaptureService;
import com.whiteboardapp.core.MatPrint;
import com.whiteboardapp.core.pipeline.CornerDetector;
import com.whiteboardapp.core.pipeline.PerspectiveTransformer;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CaptureActivity extends AppCompatActivity {

    private final String TAG = "CaptureActivity"; // For logging

    // View components
    private PreviewView previewView;
    private OverlayView overlayView;

    // View bools
    private boolean isManualSelectionEnabled = false;
    private boolean isCapturingStarted = false;
    private boolean isStartOfManualSelectionHandled = false;

    // From domain
    private MatOfPoint2f cornerPoints;
    private CaptureService captureService;
    private Mat currentModel;
    private ImageView capturedImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        setViews();
        addBtnListeners();
        setupAndStartCamera();

        currentModel = new Mat(100, 100, CvType.CV_8UC1);
        currentModel.setTo(new Scalar(255));
    }

    private void setViews() {
        previewView = findViewById(R.id.cameraPreview);
        capturedImageView = findViewById(R.id.capturedImage);

        // Overlay to draw corner points and rectangle.
        SurfaceView drawingOverlay = findViewById(R.id.drawingOverlay);
        overlayView = new OverlayView(drawingOverlay);

    }

    private boolean isShowingCapturedImage = false;

    private void addBtnListeners() {
        Button cornerButton = findViewById(R.id.setCornersBtn);
        Button capturingButton = findViewById(R.id.startCapturingBtn);
        Button changeImageBtn = findViewById(R.id.changeImageBtn);

        changeImageBtn.setOnClickListener(view -> {

            if (isShowingCapturedImage) {
                capturedImageView.setVisibility(View.INVISIBLE);
                isShowingCapturedImage = false;
                overlayView.Show();

            } else {
                capturedImageView.setVisibility(View.VISIBLE);
                isShowingCapturedImage = true;
                overlayView.Hide();

            }
        });

        cornerButton.setOnClickListener(view -> {
            isManualSelectionEnabled = true;
            capturingButton.setEnabled(true);
        });

        capturingButton.setOnClickListener(view -> {
            if (!isCapturingStarted) {
                isCapturingStarted = true;
                changeImageBtn.setEnabled(true);
                capturingButton.setText("Stop capturing");
                cornerButton.setEnabled(false);
            } else {
                // Reset view.
                capturingButton.setText("Start capturing");
                capturingButton.setEnabled(false);
                changeImageBtn.setEnabled(false);
                cornerButton.setEnabled(true);
                isManualSelectionEnabled = false;
                isCapturingStarted = false;
                isStartOfManualSelectionHandled = false;
                captureService = null; // Reset capture service.
            }
        });
    }

    // Attach behaviour to camera.
    private void setupAndStartCamera() {
        // Get the singleton CameraProvider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            // Verify that its initialization succeeded when the view is created
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                e.printStackTrace();

            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void bindUseCases(@NonNull ProcessCameraProvider cameraProvider) {
        // Select a camera.
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview previewUseCase = createPreviewUseCase();
        ImageAnalysis analysisUseCase = createAnalysisUseCase();

        // Create camera with objects bound to lifecycle.
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase, analysisUseCase);

    }

    // Use case for showing live camera preview.
    private Preview createPreviewUseCase() {

        // Configure and build preview
        Preview previewUseCase = new Preview.Builder()
                .build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        return previewUseCase;
    }

    private long totalTime = 0;
    private int rounds = 0;

    // Use case for analyzing image.
    private ImageAnalysis createAnalysisUseCase() {
        Size resolution = new Size(1280, 960);

        // Create new image analysis use case. Always get the latest frame from camera (non-blocking).
        ImageAnalysis imageAnalysisUseCase = new ImageAnalysis.Builder()
                .setTargetResolution(resolution)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // Attach image analyzer. Will execute in its' own thread.
        Executor analysisExecutor = Executors.newSingleThreadExecutor();

        imageAnalysisUseCase.setAnalyzer(analysisExecutor, imageProxy -> {
            long startTime = System.currentTimeMillis();

            analyseImage(imageProxy); // Should be a domain method!!
            imageProxy.close();

            long endTime = System.currentTimeMillis();
            System.out.println("Analysis took " + (endTime - startTime) + " milliseconds");

            if (isCapturingStarted) {
                rounds++;
                totalTime += (endTime - startTime);
                System.out.println("Capturing analysis AVERAGE is " + ((double) totalTime / rounds) + " milliseconds");
            }

        });

        return imageAnalysisUseCase;

    }

    public static Bitmap rotateBitmap(Bitmap source, float rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private void analyseImage(ImageProxy imageProxy) {
        if (imageProxy == null) {
            Log.i(TAG, "analyseImage: imageProxy was null");
            return;
        }

        @SuppressLint("UnsafeExperimentalUsageError") Image image = imageProxy.getImage();
        if (image == null) {
            Log.i(TAG, "analyseImage: getImage() returned null");
            return;
        }

        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
        Bitmap bitmapRgb = MatConverter.imageYUV_420_888toBitmap(image);

        // Rotate bitmap if necessary. The image analysis receives rotated images.
        if (rotationDegrees != 0) {
            bitmapRgb = rotateBitmap(bitmapRgb, rotationDegrees);
        }

        Mat imgRgb = new Mat();
        Utils.bitmapToMat(bitmapRgb, imgRgb);

        Mat imgBgr = new Mat();
        Imgproc.cvtColor(imgRgb, imgBgr, Imgproc.COLOR_RGB2BGR);
        CornerDetector cornerDetector = new CornerDetector();

        // Search for and display corners as long as user has not activated manual selection.
        if (!isManualSelectionEnabled && !isCapturingStarted) {
            cornerPoints = cornerDetector.findCorners(imgBgr);
            if (cornerPoints.height() == 4) {
                overlayView.drawRectFromPoints(cornerPoints, imgBgr.width(), imgBgr.height());
            }

        } else if (isManualSelectionEnabled && !isStartOfManualSelectionHandled && !isCapturingStarted) {
            // Draw a rectangle based on found corners or default if no corners found.
            // We have to repeat some of the find corners logic if user clicks on button and no corners have been
            // detected automatically yet.

            cornerPoints = cornerDetector.findCorners(imgBgr);
            if (cornerPoints.height() == 4) {
                overlayView.drawRectFromPoints(cornerPoints, imgBgr.width(), imgBgr.height());
            } else {
                overlayView.drawDefaultRect();
            }
            overlayView.isListenForManualCornerSelection = true;
            isStartOfManualSelectionHandled = true;

        } else if (isCapturingStarted) {
            // Capturing must have been started.

            PerspectiveTransformer transformer = new PerspectiveTransformer();

            // Perspective transform
            Mat imgPerspective = transformer.getPerspective(imgBgr, cornerPoints);

            // Run image processing pipeline if perspective obtained.
            if (imgPerspective != null) {
                if (captureService == null) {
                    captureService = new CaptureService(imgPerspective.width(), imgPerspective.height(), this);
                }

                currentModel = captureService.capture(imgPerspective);

                if (isShowingCapturedImage) {
                    Bitmap currentModelBitmap = MatConverter.matToBitmap(currentModel);

                    // Update view on UI thread.
                    runOnUiThread(() -> {
                        capturedImageView.setImageBitmap(currentModelBitmap);
                    });
                }

            }
        }

    }

}
