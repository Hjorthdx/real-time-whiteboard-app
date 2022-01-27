package com.whiteboardapp.controller;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.whiteboardapp.controller.viewmodels.CornerViewModel;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

// Overlay to manage and draw corner points and rectangle.
public class OverlayView implements SurfaceHolder.Callback {
    public boolean isListenForManualCornerSelection;

    private final List<CornerViewModel> cornerCircles = new ArrayList<>();
    private final Path path = new Path();
    private Paint cornerPaint;
    private Paint rectPaint;
    private Bitmap cornersBitmap; // Saved corners.
    private final int CORNER_RADIUS = 30;
    private final SurfaceView drawingOverlay;
    private final SurfaceHolder overlayHolder;
    private CornerViewModel cornerViewModelToMove = null;

    public OverlayView(SurfaceView drawingOverlay) {
        drawingOverlay.setZOrderOnTop(true);
        SurfaceHolder overlayHolder = drawingOverlay.getHolder();
        overlayHolder.setFormat(PixelFormat.TRANSPARENT);
        overlayHolder.addCallback(this);

        this.overlayHolder = overlayHolder;
        this.drawingOverlay = drawingOverlay;
        addPreviewCornerTouchListener();
        setPaint();
    }

    public void Hide() {
        drawingOverlay.setVisibility(View.INVISIBLE);
    }

    public void Show() {
        drawingOverlay.setVisibility(View.VISIBLE);
        redraw();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addPreviewCornerTouchListener() {
        drawingOverlay.setOnTouchListener((v, event) -> handleCornerTouch(event));
    }

    private void setPaint() {
        cornerPaint = new Paint();
        cornerPaint.setStyle(Paint.Style.FILL);
        cornerPaint.setColor(Color.GREEN);

        rectPaint = new Paint();
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStrokeWidth(10);
    }

    public boolean handleCornerTouch(MotionEvent event) {
        // Only handle touch events when manual corner selection.
        if (!isListenForManualCornerSelection) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        // Selecting corner.
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            CornerViewModel existingCorner = tryGrabExistingCorner(x, y);

            if (existingCorner != null) {
                cornerViewModelToMove = existingCorner;
            }
        }

        // Started moving corner.
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            if (cornerViewModelToMove != null) {
                cornerViewModelToMove.moveTo(x, y);
                drawRectOnCanvas();
            }
        }

        // Stopped moving corner.
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            cornerViewModelToMove = null;
        }

        return true;
    }

    // Returns index of existing point that contains this point.
    private CornerViewModel tryGrabExistingCorner(float x, float y) {

        for (int i = 0; i < cornerCircles.size(); i++) {
            if (cornerCircles.get(i).containsPoint(x, y)) {
                return cornerCircles.get(i);
            }

        }

        return null;
    }

    // Draws a rectangle in the center of the overlay.
    public void drawDefaultRect() {
        cornerCircles.clear();
        int widthStep = drawingOverlay.getWidth() / 4;
        int heightStep = drawingOverlay.getHeight() / 4;
        cornerCircles.add(new CornerViewModel(widthStep, heightStep, CORNER_RADIUS));
        cornerCircles.add(new CornerViewModel(drawingOverlay.getWidth() - widthStep, heightStep, CORNER_RADIUS));
        cornerCircles.add(new CornerViewModel(drawingOverlay.getWidth() - widthStep, drawingOverlay.getHeight() - heightStep, CORNER_RADIUS));
        cornerCircles.add(new CornerViewModel(widthStep, drawingOverlay.getHeight() - heightStep, CORNER_RADIUS));
        drawRectOnCanvas();

    }

    // Draw rectangle adjusted for difference between overlay and source dimensions.
    public void drawRectFromPoints(MatOfPoint2f cornerPoints, int srcWidth, int srcHeight) {
        Point[] points = cornerPoints.toArray();
        setNewCorners(srcWidth, srcHeight, points);
        drawRectOnCanvas();
    }

    private void setNewCorners(int srcWidth, int srcHeight, Point[] cornerPoints) {
        int ratioHeightAdd = 300;
        int ySubtract = 110;

        cornerCircles.clear();
        for (Point p : cornerPoints) {
            // Calculate position of new corners.
            float ratioWidth = (float) drawingOverlay.getWidth() / srcWidth;
            float ratioHeight = (float) (drawingOverlay.getHeight() + ratioHeightAdd) / srcHeight;
            cornerCircles.add(new CornerViewModel((float) p.x * ratioWidth, (float) (p.y - ySubtract) * ratioHeight, CORNER_RADIUS));
        }
    }

    private void redraw() {
        drawRectOnCanvas();
    }

    private void drawRectOnCanvas() {
        Canvas canvas = new Canvas(cornersBitmap);
        // Clear the bitmap canvas.
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        path.reset();
        // Redraw points and rectangle.
        path.moveTo(cornerCircles.get(0).getX(), cornerCircles.get(0).getY());
        for (CornerViewModel p : cornerCircles) {
            canvas.drawCircle(p.getX(), p.getY(), p.getRadius(), cornerPaint);
            path.lineTo(p.getX(), p.getY());
        }
        path.close();

        canvas.drawPath(path, rectPaint);

        // Reassign canvas
        canvas = overlayHolder.lockCanvas(null);
        // Clear canvas in current buffer.
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(cornersBitmap, 0, 0, null);
        overlayHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Create bitmap from width and height of surface when surface is ready.
        cornersBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Log.d("!!!OBS", "surfaceChanged: Bitmap instantiated ");
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

}
