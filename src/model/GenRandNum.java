package model;

import java.util.Random;

public class GenRandNum {
    private static Random random = new Random();


    // Метод генерации экспоненциального распределения
    public static double generateExponential(double mean) {
        return Math.log(random.nextDouble()) * -mean;
    }

    // Метод генерации нормального распределения
    public static double generateNormal(double mean, double stdDev) {
        return mean + stdDev * random.nextGaussian();
    }
}