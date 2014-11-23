package org.surmenok.ml.images.colorDetection;

import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.surmenok.ml.images.Image;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class DataSetBuilder {
    public MLDataSet build(ArrayList<ColorFinderTrainingImage> trainingImages, int sampleWidth, int sampleHeight) throws IOException {
        ArrayList<double[]> inputs = new ArrayList<double[]>();
        ArrayList<double[]> outputs = new ArrayList<double[]>();

        for(int i = 0; i < trainingImages.size(); i++) {
            ColorFinderTrainingImage trainingImage = trainingImages.get(i);

            Image image = new org.surmenok.ml.images.Image(trainingImage.getFileName());
            int width = image.getWidth();
            int height = image.getHeight();

            double[] pixels = image.getRectanglePixelArray(new Rectangle(0, 0, width, height), sampleWidth, sampleHeight);
            inputs.add(pixels);

            float[] hsbColor = trainingImage.getHsbColor();
            outputs.add(new double[]{hsbColor[0], hsbColor[1], hsbColor[2]});
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
}
