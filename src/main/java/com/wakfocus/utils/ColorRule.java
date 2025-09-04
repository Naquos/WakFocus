package com.wakfocus.utils;

import java.awt.Color;

public class ColorRule {
    private final Color targetColor;
    private final int tolerance;
    private final int minPixels;

    public ColorRule(Color targetColor, int tolerance, int minPixels) {
        this.targetColor = targetColor;
        this.tolerance = tolerance;
        this.minPixels = minPixels;
    }

    public boolean matches(int x, int y, int w, int h) {
        return FocusColorUtils.containsColorRaw(targetColor, tolerance, x, y, w, h, minPixels);
    }

    
    public Color getTargetColor() {
        return targetColor;
    }

    public int getTolerance() {
        return tolerance;
    }

    public int getMinPixels() {
        return minPixels;
    }
}
