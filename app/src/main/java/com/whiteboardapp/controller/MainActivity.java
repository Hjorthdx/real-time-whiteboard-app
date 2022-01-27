package com.whiteboardapp.controller;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.whiteboardapp.R;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MainActivity extends AppCompatActivity {

    // Initialize openCV library
    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("MainActivity: ", "Opencv is loaded!");
        } else {
            Log.d("MainActivity: ", "Open failed to load.");
        }
    }

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addOpenCameraBtnListener();
    }

    private void addOpenCameraBtnListener() {
        Button openCameraBtn = findViewById(R.id.openCameraBtn);
        openCameraBtn.setOnClickListener(view -> {
            if (hasCameraPermission()) {
                showCameraPreview();
            } else {
                requestCameraPermission();
            }
        });
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Starts the camera activity
    private void showCameraPreview() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivity(intent);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Camera permission granted.
        if (requestCode == CAMERA_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showCameraPreview();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setPositiveButton("OK", (dialog, id) -> {
            });

            builder.setTitle("Permission required");
            builder.setMessage("Unable to access the camera. Please change camera permissions for the app in Settings.");
            builder.create().show();
        }
    }

}