package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Class handling the simulation and calculations.
class TemperaturePropagationSimulator {

    private final Alloy alloy;
    private final double threshold;
    private final int maxIterations;
    private final ExecutorService executor;
    private final Metal[] metals;
    private final JFrame frame;
    private final JPanel gridPanel;

    public TemperaturePropagationSimulator(Alloy alloy, double threshold, int maxIterations, int numThreads, Metal[] metals) {
        this.alloy = alloy;
        this.threshold = threshold;
        this.maxIterations = maxIterations;
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.metals = metals;

        // Initialize GUI components
        frame = new JFrame("Heat Propagation Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(2000, 450);

        gridPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                int cellSize = Math.min(600 / alloy.getWidth(), 600 / alloy.getHeight());
                return new Dimension(cellSize * alloy.getWidth(), cellSize * alloy.getHeight());
            }
        };
        gridPanel.setLayout(new GridLayout(alloy.getHeight(), alloy.getWidth(), 0, 0));
        initializeGrid();

        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(Box.createRigidArea(new Dimension(20, 20)), BorderLayout.NORTH);
        frame.add(Box.createRigidArea(new Dimension(20, 20)), BorderLayout.SOUTH);
        frame.add(Box.createRigidArea(new Dimension(20, 20)), BorderLayout.WEST);
        frame.add(Box.createRigidArea(new Dimension(20, 20)), BorderLayout.EAST);
        frame.setVisible(true);
    }

    private void initializeGrid() {
        for (int i = 0; i < alloy.getHeight(); i++) {
            for (int j = 0; j < alloy.getWidth(); j++) {
                JPanel cell = new JPanel();
                cell.setPreferredSize(new Dimension(40, 40)); // Set each cell to be 40x40 pixels
                cell.setBackground(getColorForTemperature(alloy.getTemperature(i, j)));
                gridPanel.add(cell);
            }
        }
    }

    private void updateGrid() {
        Component[] components = gridPanel.getComponents();
        int index = 0;
        for (int i = 0; i < alloy.getHeight(); i++) {
            for (int j = 0; j < alloy.getWidth(); j++) {
                JPanel cell = (JPanel) components[index++];
                cell.setBackground(getColorForTemperature(alloy.getTemperature(i, j)));
            }
        }
        frame.repaint();
    }

    private Color getColorForTemperature(double temperature) {
        // Map temperature ranges to specific colors with 5-degree intervals
        if (temperature <= 25) {
            return new Color(0, 0, 255); // Blue for <= 25
        } else if (temperature <= 30) {
            return new Color(0, 64, 255); // Blue-ish for 26-30
        } else if (temperature <= 35) {
            return new Color(0, 128, 255); // Light Blue for 31-35
        } else if (temperature <= 40) {
            return new Color(0, 192, 255); // Sky Blue for 36-40
        } else if (temperature <= 45) {
            return new Color(0, 255, 255); // Cyan for 41-45
        } else if (temperature <= 50) {
            return new Color(0, 255, 128); // Green-Cyan for 46-50
        } else if (temperature <= 55) {
            return new Color(0, 255, 0); // Green for 51-55
        } else if (temperature <= 60) {
            return new Color(128, 255, 0); // Yellow-Green for 56-60
        } else if (temperature <= 65) {
            return new Color(192, 255, 0); // Green-Yellow for 61-65
        } else if (temperature <= 70) {
            return new Color(255, 255, 0); // Yellow for 66-70
        } else if (temperature <= 75) {
            return new Color(255, 192, 0); // Orange-Yellow for 71-75
        } else if (temperature <= 80) {
            return new Color(255, 128, 0); // Orange for 76-80
        } else if (temperature <= 85) {
            return new Color(255, 64, 0); // Reddish-Orange for 81-85
        } else if (temperature <= 90) {
            return new Color(255, 0, 0); // Red for 86-90
        } else if (temperature <= 95) {
            return new Color(192, 0, 0); // Dark Red for 91-95
        } else {
            return new Color(128, 0, 0); // Deep Red for >95
        }
    }

    // Method to print the current temperature grid
    private void printTemperatureGrid() {
        for (int i = 0; i < alloy.getHeight(); i++) {
            for (int j = 0; j < alloy.getWidth(); j++) {
                System.out.printf("%.2f ", alloy.getTemperature(i, j));
                if ((j < alloy.getWidth() - 1)) {
                    System.out.print("|| ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public void runSimulation() throws InterruptedException, ExecutionException {
        boolean converged = false;
        int iteration = 0;
        while (!converged && iteration < maxIterations) {
            System.out.println("Iteration " + iteration + ":");
            printTemperatureGrid();
            converged = true;
            double[][] newTemperatures = new double[alloy.getHeight()][alloy.getWidth()];
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < alloy.getHeight(); i++) {
                for (int j = 0; j < alloy.getWidth(); j++) {
                    final int row = i;
                    final int col = j;
                    futures.add(executor.submit(() -> {
                        if ((row == 0 && col == 0) || (row == alloy.getHeight() - 1 && col == alloy.getWidth() - 1)) {
                            // Keep the corner temperatures constant
                            newTemperatures[row][col] = 100.0;
                            return false;
                        }
                        double newTemp = calculateNewTemperature(row, col);
                        newTemperatures[row][col] = newTemp;
                        return Math.abs(newTemp - alloy.getTemperature(row, col)) > threshold;
                    }));
                }
            }

            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    converged = false;
                }
            }

            // Update temperatures
            for (int i = 0; i < alloy.getHeight(); i++) {
                for (int j = 0; j < alloy.getWidth(); j++) {
                    alloy.setTemperature(i, j, newTemperatures[i][j]);
                }
            }

            // Update GUI
            updateGrid();

            iteration++;
            Thread.sleep(50); // Add longer delay for visualization
        }

        executor.shutdown();
        System.out.println("Final Temperature Grid:");
        printTemperatureGrid();
        System.out.println("Simulation completed in " + iteration + " iterations.");
    }

    private double calculateNewTemperature(int row, int col) {
        double newTemp = 0.0;

        // Skip constant corners
        if ((row == 0 && col == 0) || (row == alloy.getHeight() - 1 && col == alloy.getWidth() - 1)) {
            return 100.0;
        }

        // Get metal percentages and normalize
        double[] metalPercentages = alloy.getMetalPercentages(row, col);
        double sum = 0.0;
        for (double percentage : metalPercentages) {
            sum += percentage;
        }
        if (sum != 1.0) {
            for (int m = 0; m < metalPercentages.length; m++) {
                metalPercentages[m] /= sum;
            }
        }

        // Neighbor contributions
        for (int m = 0; m < metals.length; m++) {
            double Cm = metals[m].getThermalConstant();
            double weightedSum = 0.0;
            int neighborCount = 0;

            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] direction : directions) {
                int neighborRow = row + direction[0];
                int neighborCol = col + direction[1];

                if (neighborRow >= 0 && neighborRow < alloy.getHeight() && neighborCol >= 0 && neighborCol < alloy.getWidth()) {
                    double neighborTemp = alloy.getTemperature(neighborRow, neighborCol);
                    double neighborPercentage = alloy.getMetalPercentages(neighborRow, neighborCol)[m];
                    weightedSum += neighborTemp * neighborPercentage;
                    neighborCount++;
                }
            }

            if (neighborCount > 0) {
                weightedSum /= neighborCount;
            }

            newTemp += Cm * weightedSum;
        }



        // Apply influence factor
        double influenceFactor = 1.053; // Increased influence factor to amplify heat propagation
        newTemp = (newTemp * influenceFactor);

        return newTemp;
    }
}
