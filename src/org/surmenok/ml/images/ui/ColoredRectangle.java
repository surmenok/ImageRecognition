package org.surmenok.ml.images.ui;

import java.awt.*;

public class ColoredRectangle {
    private Rectangle rectangle;
    private Color color;

    public ColoredRectangle(Rectangle rectangle, Color color) {
        this.rectangle = rectangle;
        this.color = color;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.rectangle = rectangle;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
