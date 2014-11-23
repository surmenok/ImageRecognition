package org.surmenok.ml.images.colorDetection;

import org.encog.Encog;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.surmenok.ml.images.Image;
import org.surmenok.ml.images.NeuralNetworkTrainer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ColorFinder {
    public static void main(String[] args) throws IOException, InterruptedException {
        ArrayList<ColorFinderTrainingImage> images = loadImages();

        int sampleWidth = 100;
        int sampleHeight = 60;

        DataSetBuilder dataSetBuilder = new DataSetBuilder();

        int trainingImageCount = (int)(0.8 * images.size());
        int validationImageCount = images.size() - trainingImageCount;

        ArrayList<ColorFinderTrainingImage> trainingImages = getSubset(images, 0, trainingImageCount);
        ArrayList<ColorFinderTrainingImage> validationImages = getSubset(images, trainingImageCount, validationImageCount);

        MLDataSet trainingSet = dataSetBuilder.build(trainingImages, sampleWidth, sampleHeight);
        MLDataSet validationSet = dataSetBuilder.build(validationImages, sampleWidth, sampleHeight);

        int iterationsCount = 3;
        int epochsCount = 200;
        String[] activationTypes = new String[] { "Elliott", "LOG", "Ramp", "Sigmoid", "SteepenedSigmoid" };
        String[] trainingTypes = new String[] { "QuickPropagation", "ResilientPropagation", "Backpropagation", "ScaledConjugateGradient", "ManhattanPropagation" };
//        String[] activationTypes = new String[] { "Sigmoid" };
//        String[] trainingTypes = new String[] { "ResilientPropagation" };

        NeuralNetworkTrainer trainer = new NeuralNetworkTrainer();
        trainer.setActivationTypes(activationTypes);
        trainer.setTrainingTypes(trainingTypes);
        trainer.setIterationsCount(iterationsCount);
        trainer.setEpochsCount(epochsCount);
        trainer.setClassification(false);

        trainer.setTrainingSet(trainingSet);
        trainer.setValidationSet(validationSet);

        trainer.train();

        BasicNetwork network = trainer.getBestNetwork();

        String fileName = "c:\\Stuff\\LipColorDataSet\\Images\\19.jpg";
        analyze(fileName, network, sampleWidth, sampleHeight);

        Encog.getInstance().shutdown();
    }

    private static ArrayList<ColorFinderTrainingImage> getSubset(ArrayList<ColorFinderTrainingImage> images, int start, int count) {
        ArrayList<ColorFinderTrainingImage> list = new ArrayList<ColorFinderTrainingImage>();
        for(int i = start; i < start + count; i++) {
            list.add(images.get(i));
        }

        return list;
    }

    private static ArrayList<ColorFinderTrainingImage> loadImages() throws IOException {
        ArrayList<ColorFinderTrainingImage> trainingImages = new ArrayList<ColorFinderTrainingImage>();

        BufferedReader br = new BufferedReader(new FileReader("c:\\Stuff\\LipColorDataSet\\dataset.csv"));
        String line;

        while ((line = br.readLine()) != null) {
            String[] values = line.split(";");

            String fileName = "c:\\Stuff\\LipColorDataSet\\Images\\" + values[0];

            String hexRgbColor = values[1];
            Color color = hex2Rgb(hexRgbColor);

            ColorFinderTrainingImage trainingImage = new ColorFinderTrainingImage(fileName, color);
            trainingImages.add(trainingImage);
        }

        br.close();
        return trainingImages;
    }

    private static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 0, 2 ), 16 ),
                Integer.valueOf( colorStr.substring( 2, 4 ), 16 ),
                Integer.valueOf( colorStr.substring( 4, 6 ), 16 ) );
    }

    private static void analyze(String fileName, BasicNetwork network, int sampleWidth, int sampleHeight) throws IOException {
        Image image = new org.surmenok.ml.images.Image(fileName);
        int width = image.getWidth();
        int height = image.getHeight();

        byte[] pixels = image.getRectanglePixels(0, 0, width, height, sampleWidth, sampleHeight);
        double[] input = new double[pixels.length];
        for(int i = 0; i < pixels.length; i++) {
            input[i] = pixels[i];
        }

        MLData mlInput = new BasicMLData(input);
        MLData mlOutput = network.compute(mlInput);

        double[] outputData = mlOutput.getData();
        Color color = new Color(Color.HSBtoRGB((float)outputData[0], (float)outputData[1], (float)outputData[2]));

        System.out.printf("Result: %s/%s/%s; rgb: %s/%s/%s\n", outputData[0], outputData[1], outputData[2], color.getRed(), color.getGreen(), color.getBlue());
    }
}
