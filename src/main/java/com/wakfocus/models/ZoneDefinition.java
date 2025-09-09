package com.wakfocus.models;

import java.awt.Color;

import javafx.scene.shape.Rectangle;

public class ZoneDefinition {
    private final Rectangle zone;
    private final Color referenceColor;
    private final int tolerance;

    public ZoneDefinition(Rectangle zone, Color referenceColor, int tolerance) {
        this.zone = zone;
        this.referenceColor = referenceColor;
        this.tolerance = tolerance;
    }

    /** Vérifie si un pixel (x,y) est à l'intérieur de la zone */
    public boolean contains(int x, int y) {
        return zone.contains(x, y);
    }

    /** Vérifie si la couleur du pixel correspond à la couleur de référence */
    public boolean matches(Color pixelColor) {
        int dr = Math.abs(pixelColor.getRed() - referenceColor.getRed());
        int dg = Math.abs(pixelColor.getGreen() - referenceColor.getGreen());
        int db = Math.abs(pixelColor.getBlue() - referenceColor.getBlue());

        return dr <= tolerance && dg <= tolerance && db <= tolerance;
    }

    public Rectangle getZone() {
        return zone;
    }

    public Color getReferenceColor() {
        return referenceColor;
    }

    public int getTolerance() {
        return tolerance;
    }
}
