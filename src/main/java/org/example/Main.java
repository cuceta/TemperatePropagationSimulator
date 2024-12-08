package org.example;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {
        try {
            // Define the metals with their thermal constants
            Metal metal1 = new Metal(0.75);
            Metal metal2 = new Metal(1.0);
            Metal metal3 = new Metal(1.25);
            Metal[] metals = {metal1, metal2, metal3};

            // Create the alloy grid
            Alloy alloy = new Alloy(20, 5); // Width = 20, Height = 5 (at least 4 times wide as height)

            // Create the simulation with 4 threads
            TemperaturePropagationSimulator simulation = new TemperaturePropagationSimulator(alloy,  0.03, 1000, 4, metals); // Threshold = 0.01, Max iterations = 1000, 4 threads

            // Run the simulation
            simulation.runSimulation();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
