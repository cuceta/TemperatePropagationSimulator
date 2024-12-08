package org.example;
import java.util.Random;
import java.util.concurrent.*;

// Class representing each metal with its thermal constant.
class Metal {
    private final double thermalConstant;

    public Metal(double thermalConstant) {
        this.thermalConstant = thermalConstant;
    }

    public double getThermalConstant() {
        return thermalConstant;
    }

    // Method to calculate thermal interaction based on neighboring temperatures
    public double calculateThermalInteraction(double averageNeighborTemp, double percentage) {
        return percentage * thermalConstant * averageNeighborTemp;
    }
}