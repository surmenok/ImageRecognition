package org.surmenok.ml.images.objectDetection;

import java.awt.*;

public class ObjectFinderTrainingImage {
    private String fileName;
    private Rectangle[] objectRectangles;
    private Rectangle borderRectangle;

    public ObjectFinderTrainingImage(
            String fileName,
            Rectangle[] objectRectangles,
            Rectangle borderRectangle) {
        this.fileName = fileName;
        this.objectRectangles = objectRectangles;
        this.borderRectangle = borderRectangle;
    }

    public String getFileName() {
        return fileName;
    }

    public Rectangle[] getObjectRectangles() {
        return objectRectangles;
    }

    public Rectangle getBorderRectangle() {
        return borderRectangle;
    }
}
