package com.whiteboardapp.controller.viewmodels;


import com.whiteboardapp.common.Calculator;

public class CornerViewModel {

    // Center point coordinates.
    private float x;
    private float y;

    private final float radius;

    public CornerViewModel(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public boolean containsPoint(float x, float y) {
        double distanceToPoint = Calculator.getDistanceBetweenPoints(this.x, this.y, x, y);
        return distanceToPoint < radius;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void moveTo(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }
}
