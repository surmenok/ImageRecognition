package org.surmenok.ml.images.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class DisplayWindow {
    JFrame frame;
    org.surmenok.ml.images.Image sourceImage;
    Graphics2D graphics;
    int rectangleWidth;
    int rectangleHeight;
    ImagePanel imagePanel;
    ArrayList<ColoredRectangle> foundObjects;

    public DisplayWindow(org.surmenok.ml.images.Image sourceImage, int rectangleWidth, int rectangleHeight) {
        this.foundObjects = new ArrayList<ColoredRectangle>();

        this.sourceImage = sourceImage;
        this.rectangleWidth = rectangleWidth;
        this.rectangleHeight = rectangleHeight;

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        BufferedImage displayedImage = new BufferedImage(sourceImage.getWidth() + 50 + rectangleWidth, sourceImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        graphics = displayedImage.createGraphics();
        graphics.drawImage(sourceImage.getSourceImage(), 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);

        imagePanel = new ImagePanel(displayedImage);
        frame.add(imagePanel);
    }

    public void setTrainingRectangle(Rectangle rectangle, boolean isObject) {
        Color color = isObject ? Color.green : Color.red;
        graphics.setColor(color);
        graphics.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        imagePanel.repaint();
    }

    public void setResultRectangle(int x, int y, int width, int height) {
        graphics.setColor(Color.blue);
        graphics.drawRect(x, y, width, height);

        BufferedImage rectangleImage = sourceImage.getRectangleImage(x, y, width, height, rectangleWidth, rectangleHeight);
        graphics.drawImage(rectangleImage, sourceImage.getWidth() + 50, 0, rectangleWidth, rectangleHeight, null);

        imagePanel.repaint();
    }

    public void setCurrentRectangle(int x, int y, int width, int height, double success) {
        drawSourceImage();

        boolean isObject = success > 0.9999999;

//        int r = successProbability < 0.5 ? (int)(successProbability * 512) : 0;
//        int g = successProbability > 0.5 ? (int)((1 - successProbability) * 512) : 0;
//        System.out.println("r: " + r + " g: " + g);
//        Color color = new Color(r, g, 0);
        Color color = success > 0.9999999 ? Color.green : Color.yellow;

        if(isObject) {
            this.foundObjects.add(new ColoredRectangle(new Rectangle(x, y, width, height), color));
        }

        for(ColoredRectangle coloredRectangle : this.foundObjects) {
            Rectangle rectangle = coloredRectangle.getRectangle();
            graphics.setColor(Color.green);
            graphics.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }

        if(!isObject) {
            graphics.setColor(Color.red);
            graphics.drawRect(x, y, width, height);
        }

        BufferedImage rectangleImage = sourceImage.getRectangleImage(x, y, width, height, rectangleWidth, rectangleHeight);
        graphics.drawImage(rectangleImage, sourceImage.getWidth() + 50, 0, rectangleWidth, rectangleHeight, null);

        imagePanel.repaint();
    }

    private void drawSourceImage() {
        graphics.drawImage(sourceImage.getSourceImage(), 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
    }
}

