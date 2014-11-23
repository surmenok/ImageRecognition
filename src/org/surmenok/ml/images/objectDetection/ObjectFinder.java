package org.surmenok.ml.images.objectDetection;

import org.encog.Encog;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.surmenok.ml.images.*;
import org.surmenok.ml.images.ui.DisplayWindow;

import java.awt.*;
import java.io.IOException;

public class ObjectFinder {
    public static void main(String[] args) throws IOException, InterruptedException {
        ObjectFinderTrainingImage[] trainingImages = new ObjectFinderTrainingImage[] {
                new ObjectFinderTrainingImage(
                        "c:\\Users\\Pavel\\Documents\\10658991_10204639006436569_6385980699179530729_o.jpg",
                        new Rectangle[] { new Rectangle(925, 379, 68, 48), new Rectangle(1039, 375, 68, 48) },
                        new Rectangle(846, 232, 273, 412)),
        };

        int sampleWidth = 70;
        int sampleHeight = 50;

        DataSetBuilder dataSetBuilder = new DataSetBuilder();

        MLDataSet trainingSet = dataSetBuilder.build(trainingImages, sampleWidth, sampleHeight, 1000);
        MLDataSet validationSet = dataSetBuilder.build(trainingImages, sampleWidth, sampleHeight, 100);

        int iterationsCount = 3;
        int epochsCount = 20;
//        String[] activationTypes = new String[] { "Elliott", "LOG", "Ramp", "Sigmoid", "SteepenedSigmoid" };
//        String[] trainingTypes = new String[] { "QuickPropagation", "ResilientPropagation", "Backpropagation", "ScaledConjugateGradient", "ManhattanPropagation" };
        String[] activationTypes = new String[] { "Sigmoid" };
        String[] trainingTypes = new String[] { "ResilientPropagation" };

        NeuralNetworkTrainer trainer = new NeuralNetworkTrainer();
        trainer.setActivationTypes(activationTypes);
        trainer.setTrainingTypes(trainingTypes);
        trainer.setIterationsCount(iterationsCount);
        trainer.setEpochsCount(epochsCount);

        trainer.setTrainingSet(trainingSet);
        trainer.setValidationSet(validationSet);

        trainer.train();

        BasicNetwork network = trainer.getBestNetwork();

        String fileName = "c:\\Users\\Pavel\\Documents\\10658991_10204639006436569_6385980699179530729_o.jpg";
        analyze(fileName, network, sampleWidth, sampleHeight, new Rectangle(846, 232, 273, 412));

        Encog.getInstance().shutdown();
    }

    private static void analyze(String fileName, BasicNetwork network, int sampleWidth, int sampleHeight, Rectangle borderRectangle) throws IOException {
        org.surmenok.ml.images.Image image = new org.surmenok.ml.images.Image(fileName);
        int width = image.getWidth();
        int height = image.getHeight();

        int xyStep = width / 100;
        if (xyStep == 0) {
            xyStep = 1;
        }

        double minPercentage = 0.03;
        double maxPercentage = 0.1;

        int maxWidth = (int)Math.min(width * maxPercentage, height * maxPercentage * sampleWidth / sampleHeight);
        int minWidth = (int) Math.max(Math.min(maxWidth, width * minPercentage), 10);

        int widthStep = (maxWidth - minWidth) / 5;

        DisplayWindow displayWindow = new DisplayWindow(image, sampleWidth, sampleHeight);

        int bestX = 0;
        int bestY = 0;
        int bestWidth = 0;
        int bestHeight = 0;
        double bestSuccess = -100000;

        for(int w = minWidth; w < maxWidth; w += widthStep) {
            int h = (int)((double)sampleHeight / (double)sampleWidth * (double)w);

            for(int x = borderRectangle.x; x < borderRectangle.x + borderRectangle.width - w - 1; x += xyStep) {
                for(int y = borderRectangle.y; y < borderRectangle.y + borderRectangle.height - h - 1; y += xyStep) {
                    byte[] pixels = image.getRectanglePixels(x, y, w, h, sampleWidth, sampleHeight);
                    double[] input = new double[pixels.length];
                    for(int i = 0; i < pixels.length; i++) {
                        input[i] = pixels[i];
                    }

                    MLData mlInput = new BasicMLData(input);
                    MLData mlOutput = network.compute(mlInput);

                    double success = mlOutput.getData()[1] - mlOutput.getData()[0];

                    if(success > bestSuccess) {
                        bestX = x;
                        bestY = y;
                        bestWidth = w;
                        bestHeight = h;
                        bestSuccess = success;
                        System.out.println("success: " + success + " (" + mlOutput.getData()[1] + " - " + mlOutput.getData()[0] + ")");
                    }

                    displayWindow.setCurrentRectangle(x, y, w, h, success);
                    displayWindow.setResultRectangle(bestX, bestY, bestWidth, bestHeight);
                }
            }
        }
    }
}
