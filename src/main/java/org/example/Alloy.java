package org.example;
import java.util.Random;
import java.util.concurrent.*;
/*
Representation of the alloy itself as a grid (2D array)
    Each spot of the 2D array = a cell = a region
 */
class Alloy {
    private final double[][] temperatureGrid;
    private final double[][][] metalPercentages;
    private final int width;
    private final int height;

    public Alloy(int width, int height) {
        if (width < 4 * height) {
            throw new IllegalArgumentException("Width must be at least 4 times the height.");
        }
        this.width = width;
        this.height = height;
        temperatureGrid = new double[height][width];
        metalPercentages = new double[height][width][3];
        initializeGrid();
    }

    private void initializeGrid() {
        Random random = new Random();
        // Initialize the grid with random metal percentages and temperatures
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // Randomly assign percentages for the three metals, ensuring they sum to 1.0 and have a max difference of 20%
                double p1 = 0.4 + random.nextDouble() * 0.2; // Between 0.4 and 0.6
                double p2 = 0.4 + random.nextDouble() * 0.2; // Between 0.4 and 0.6
                double p3 = 1.0 - p1 - p2;

                // Ensure all percentages are within 20% of each other
                if (Math.abs(p1 - p2) > 0.2 || Math.abs(p2 - p3) > 0.2 || Math.abs(p1 - p3) > 0.2) {
                    p1 = 0.4;
                    p2 = 0.4;
                    p3 = 0.2;
                }


//                double p1 = random.nextDouble();
//                double r = random.nextDouble();
//                double p2 = 0.0;
//                while(p2 == 0.0) {
//                    if ((p1 + r) < 0.99) {
//                        p2 = r;
//                    } else {
//                        r = random.nextDouble();
//                    }
//                }
//                double p3 = 1.0 - p1 - p2;


                metalPercentages[i][j][0] = p1;
                metalPercentages[i][j][1] = p2;
                metalPercentages[i][j][2] = p3;

                // Set initial temperature (with some random noise)
                temperatureGrid[i][j] = 20.0 + random.nextDouble() * 5.0; // Base temperature with noise
            }
        }
        temperatureGrid[0][0] = 100.0; // Top-left corner heated at S degrees
        temperatureGrid[height - 1][width - 1] = 100.0; // Bottom-right corner heated at T degrees
    }

    public double getTemperature(int row, int col) {
        return temperatureGrid[row][col];
    }

    public void setTemperature(int row, int col, double temperature) {
        temperatureGrid[row][col] = temperature;
    }

    public double[] getMetalPercentages(int row, int col) {
        return metalPercentages[row][col];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // Method to check if a cell is on the edge (boundary)
    public boolean isEdge(int row, int col) {
        return row == 0 || row == height - 1 || col == 0 || col == width - 1;
    }
}
