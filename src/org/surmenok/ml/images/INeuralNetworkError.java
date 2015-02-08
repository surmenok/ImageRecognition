package org.surmenok.ml.images;

public interface INeuralNetworkError {
    double getError(double[] outputData, double[] idealData);
}
