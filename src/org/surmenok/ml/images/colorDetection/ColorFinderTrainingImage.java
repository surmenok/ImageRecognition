package org.surmenok.ml.images.colorDetection;

import java.awt.*;

public class ColorFinderTrainingImage {
    private String fileName;
    private Color color;

    public ColorFinderTrainingImage(
            String fileName,
            Color color) {
        this.fileName = fileName;
        this.color = color;
    }

    public String getFileName() {
        return fileName;
    }

    public float[] getHsbColor() {
        return Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    }
}
