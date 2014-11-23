package org.surmenok.ml.images;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.strategy.RegularizationStrategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class NeuralNetworkTrainer {
    private String[] activationTypes = new String[] { "Elliott", "LOG", "Ramp", "Sigmoid", "SteepenedSigmoid" };
    private String[] trainingTypes = new String[] { "QuickPropagation", "ResilientPropagation", "Backpropagation", "ScaledConjugateGradient", "ManhattanPropagation" };

    private MLDataSet trainingSet;
    private MLDataSet validationSet;

    private int epochsCount = 200;
    private int iterationsCount = 3;

    private boolean isClassification = true;

    private BasicNetwork bestNetwork;
    private double bestError = Double.MAX_VALUE;

    public void setTrainingSet(MLDataSet trainingSet) {
        this.trainingSet = trainingSet;
    }

    public void setValidationSet(MLDataSet validationSet) {
        this.validationSet = validationSet;
    }

    public void setActivationTypes(String[] activationTypes) {
        this.activationTypes = activationTypes;
    }

    public void setTrainingTypes(String[] trainingTypes) {
        this.trainingTypes = trainingTypes;
    }

    public void setEpochsCount(int epochsCount) {
        this.epochsCount = epochsCount;
    }

    public void setIterationsCount(int iterationsCount) {
        this.iterationsCount = iterationsCount;
    }

    public void setClassification(boolean isClassification) {
        this.isClassification = isClassification;
    }

    public BasicNetwork getBestNetwork() {
        return bestNetwork;
    }

    public void train() throws IOException {
        int inputSize = trainingSet.get(0).getInputArray().length;
        int outputSize = trainingSet.get(0).getIdealArray().length;

        for (String trainingType : trainingTypes) {
            for (String activationType : activationTypes) {
                for (int hiddenLayersCount = 2; hiddenLayersCount <= 2; hiddenLayersCount++) {
                    for (int hiddenLayerNeuronsCount = 500; hiddenLayerNeuronsCount <= 500; hiddenLayerNeuronsCount += 100) {
                        for (double lambda = 0; lambda <= 0; lambda += 0.01) {
                            long elapsedSum = 0;
                            double validationErrorSum = 0;
                            double classificationErrorSum = 0;

                            for (int i = 1; i <= iterationsCount; i++) {
                                long start = System.currentTimeMillis();

                                BasicNetwork network = trainNetwork(inputSize, outputSize, trainingType, activationType, hiddenLayersCount, hiddenLayerNeuronsCount, lambda);
                                ErrorRate validationError = calculateError(validationSet, network);

                                validationErrorSum += validationError.NeuralNetworkErrorRate;
                                classificationErrorSum += validationError.ClassificationErrorRate;

                                long end = System.currentTimeMillis();
                                long elapsed = end - start;
                                elapsedSum += elapsed;

                                // test the neural network
                                System.out.println("Validation error: " + validationError.ClassificationErrorRate + " Elapsed: " + elapsed);

                                if(validationError.ClassificationErrorRate < bestError) {
                                    bestError = validationError.ClassificationErrorRate;
                                    bestNetwork = network;
                                }
                            }

                            //todo: move to a separate class
                            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("c:\\Stuff\\FaceTests\\tests.csv", true)));
                            out.println(trainingType + "," + activationType + "," + hiddenLayersCount + "," + hiddenLayerNeuronsCount + "," + lambda + "," + epochsCount + "," + (validationErrorSum / iterationsCount) + "," + (elapsedSum / iterationsCount) + "," + iterationsCount + "," + (classificationErrorSum / iterationsCount));
                            out.flush();
                            out.close();
                        }
                    }
                }
            }
        }
    }

    private BasicNetwork trainNetwork(
            int inputSize, int outputSize, String trainingType, String activationType, int hiddenLayersCount, int hiddenLayerNeuronsCount, double lambda) throws IOException {
        BasicNetwork network = new BasicNetwork();

        network.addLayer(new BasicLayer(null, true, inputSize));

        final ActivationFunction activationFunction = new ActivationFunctionFactory().create(activationType);

        for(int i = 1; i <= hiddenLayersCount; i++) {
            network.addLayer(new BasicLayer(activationFunction, true, hiddenLayerNeuronsCount));
        }

        network.addLayer(new BasicLayer(activationFunction, false, outputSize));
        network.getStructure().finalizeStructure();
        network.reset();

        // train the neural network

        final Propagation train = new PropagationFactory().create(trainingType, network, trainingSet);

        train.addStrategy(new RegularizationStrategy(lambda));

        //todo: move to a separate class
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("c:\\Stuff\\FaceTests\\train_" + trainingType + "_" + activationType + "_" + hiddenLayersCount + "_" + hiddenLayerNeuronsCount + "_" + lambda + "_" + epochsCount + ".csv", true)));

        for(int epoch = 1; epoch <= epochsCount; epoch++)
        {
            train.iteration();
            System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            out.println(epoch + "," + train.getError());
        }

        out.flush();
        out.close();

        train.finishTraining();

        return network;
    }

    private ErrorRate calculateError(MLDataSet validationSet, BasicNetwork network) {
        double error = 0;

        for(int i = 0; i < validationSet.size(); i++) {
            MLDataPair dataPair = validationSet.get(i);

            MLData output = network.compute(dataPair.getInput());
            double[] outputData = output.getData();

            double[] idealData = dataPair.getIdealArray();

            double sampleError = isClassification
                    ? getClassificationError(outputData, idealData)
                    : getRegressionError(outputData, idealData);
            error += sampleError;
        }

        ErrorRate errorRate = new ErrorRate();
        errorRate.ClassificationErrorRate = error / validationSet.size();
        errorRate.NeuralNetworkErrorRate = network.calculateError(validationSet);
        return errorRate;
    }

    private double getClassificationError(double[] outputData, double[] idealData) {
        int maxIndex = 0;
        double maxValue = outputData[0];

        for(int j = 1; j < outputData.length; j++) {
            if(maxValue < outputData[j]) {
                maxIndex = j;
                maxValue = outputData[j];
            }
        }

        if(idealData[maxIndex] != 1) {
            return 1;
        }

        return 0;
    }

    private double getRegressionError(double[] outputData, double[] idealData) {
        double sampleError = 0;

        for(int j = 1; j < outputData.length; j++) {
            sampleError += Math.abs(outputData[j] - idealData[j]);
        }

        return sampleError;
    }
}
