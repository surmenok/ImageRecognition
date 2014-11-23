package org.surmenok.ml.images.objectDetection;

import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.surmenok.ml.images.*;
import org.surmenok.ml.images.objectDetection.ObjectFinderTrainingImage;

import java.awt.*;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;

public class DataSetBuilder {
    public MLDataSet build(ObjectFinderTrainingImage[] trainingImages, int sampleWidth, int sampleHeight, int nonObjectCountPerImage) throws IOException {
        ArrayList<double[]> inputs = new ArrayList<double[]>();
        ArrayList<double[]> outputs = new ArrayList<double[]>();

        for(int i = 0; i < trainingImages.length; i++) {
            ObjectFinderTrainingImage trainingImage = trainingImages[i];

            org.surmenok.ml.images.Image image = new org.surmenok.ml.images.Image(trainingImage.getFileName());
            int width = image.getWidth();
            int height = image.getHeight();

            Rectangle[] objectRectangles = trainingImage.getObjectRectangles();
            Rectangle[] adjustedObjectRectangles = new Rectangle[objectRectangles.length];
            for(int j = 0; j < objectRectangles.length; j++) {
                Rectangle adjustedObjectRectangle = getRectangleOfSize(objectRectangles[j], (double)sampleWidth / sampleHeight);
                adjustedObjectRectangles[j] = adjustedObjectRectangle;
            }

            for(int j = 0; j < adjustedObjectRectangles.length; j++) {
                double[] pixels = image.getRectanglePixelArray(adjustedObjectRectangles[j], sampleWidth, sampleHeight);
                inputs.add(pixels);
                outputs.add(new double[] {0, 1});
            }

            ArrayList<Rectangle> nonObjectRectangles = getNonObjectRectangles(width, height, adjustedObjectRectangles, trainingImage.getBorderRectangle(), nonObjectCountPerImage);
            for(int j = 0; j < nonObjectRectangles.size(); j++) {
                double[] pixels = image.getRectanglePixelArray(nonObjectRectangles.get(j), sampleWidth, sampleHeight);
                inputs.add(pixels);
                outputs.add(new double[] {1, 0});
            }
        }

        double[][] inputArray = new double[inputs.size()][inputs.get(0).length];
        for(int i = 0; i < inputs.size(); i++) {
            inputArray[i] = inputs.get(i);
        }

        double[][] outputArray = new double[outputs.size()][outputs.get(0).length];
        for(int i = 0; i < outputs.size(); i++) {
            outputArray[i] = outputs.get(i);
        }

        MLDataSet dataSet = new BasicMLDataSet(inputArray, outputArray);
        return dataSet;
    }


    private static ArrayList<Rectangle> getNonObjectRectangles(int width, int height, Rectangle[] objectRectangles, Rectangle borderRectangle, int count) {
        int sampleWidth = objectRectangles[0].width;
        int sampleHeight = objectRectangles[0].height;

        int deltaX = sampleWidth / 2;
        int deltaY = sampleHeight / 2;

        double minPercentage = 0.03;
        double maxPercentage = 0.1;

        int maxWidth = (int)Math.min(width * maxPercentage, height * maxPercentage * sampleWidth / sampleHeight);
        int minWidth = (int) Math.max(Math.min(maxWidth, width * minPercentage), 10);

        ArrayList<Rectangle> nonObjectRectangles = new ArrayList<Rectangle>();

        while(nonObjectRectangles.size() < count) {
            int rectangleWidth = (int)(Math.random() * (maxWidth - minWidth) + minWidth);
            int rectangleHeight = rectangleWidth * sampleHeight / sampleWidth;

            int x = (int)(Math.random() * (borderRectangle.width - rectangleWidth)) + borderRectangle.x;
            int y = (int)(Math.random() * (borderRectangle.height - rectangleHeight)) + borderRectangle.y;

            boolean isNearby = isNearby(x, y, rectangleWidth, rectangleHeight, objectRectangles, deltaX, deltaY);

            if(!isNearby) {
                nonObjectRectangles.add(new Rectangle(x, y, rectangleWidth, rectangleHeight));
            }
        }

        return nonObjectRectangles;
    }

    private static boolean isNearby(int x, int y, int width, int height, Rectangle[] objectRectangles, int deltaX, int deltaY) {
        boolean isNearby = false;

        for(Rectangle objectRectangle : objectRectangles) {
            if (Math.abs(x - objectRectangle.x) <= deltaX
                    && Math.abs(y - objectRectangle.y) <= deltaY
                    && Math.abs(x + width - objectRectangle.x - objectRectangle.width) <= deltaX
                    && Math.abs(y + height - objectRectangle.y - objectRectangle.height) <= deltaY) {
                isNearby = true;
                break;
            }
        }

        return isNearby;
    }

    private static Rectangle getRectangleOfSize(Rectangle source, double widthHeightRatio) {
        if((double)source.width / source.height > widthHeightRatio) {
            int centerY = source.y + source.height / 2;
            int height = (int)(source.width / widthHeightRatio);
            return new Rectangle(source.x, centerY - height / 2, source.width, height);
        } else {
            int centerX = source.x + source.width / 2;
            int width = (int)(source.height * widthHeightRatio);
            return new Rectangle(centerX - width / 2, source.y, width, source.height);
        }
    }
}
