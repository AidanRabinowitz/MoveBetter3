package com.example.movebetter3;

public class LiftLogic {
    private static final double ARC_THRESHOLD = 0.2; // Threshold for detecting an arc
    private static final int TOP_PERCENT = 20; // Percentage of top acceleration values to consider

    // Method to calculate the average acceleration of the top 20 percent of an arc
    public static double calculateTopArcAcceleration(double[] accelerometerData) {
        int dataSize = accelerometerData.length;
        int startIndex = -1;
        int endIndex = -1;

        // Find the start and end indices of the arc
        for (int i = 1; i < dataSize - 1; i++) {
            double prevValue = accelerometerData[i - 1];
            double currentValue = accelerometerData[i];
            double nextValue = accelerometerData[i + 1];

            // Check for an arc pattern (increase then decrease in acceleration within ARC_THRESHOLD seconds)
            if (currentValue > prevValue && currentValue > nextValue &&
                    nextValue < currentValue * (1 - ARC_THRESHOLD)) {
                startIndex = i;
                break;
            }
        }

        if (startIndex != -1) {
            for (int i = startIndex + 1; i < dataSize - 1; i++) {
                double prevValue = accelerometerData[i - 1];
                double currentValue = accelerometerData[i];
                double nextValue = accelerometerData[i + 1];

                // Check for the end of the arc pattern
                if (currentValue < prevValue && currentValue < nextValue &&
                        prevValue > currentValue * (1 - ARC_THRESHOLD)) {
                    endIndex = i;
                    break;
                }
            }
        }

        // Calculate the top 20 percent of the arc
        if (startIndex != -1 && endIndex != -1) {
            int arcSize = endIndex - startIndex + 1;
            int topPercentSize = (int) Math.ceil((double) arcSize * TOP_PERCENT / 100);

            double[] topValues = new double[topPercentSize];
            System.arraycopy(accelerometerData, startIndex, topValues, 0, topPercentSize);

            // Calculate the average of the top values
            double sum = 0;
            for (double value : topValues) {
                sum += value;
            }
            return sum / topPercentSize;
        } else {
            return 0; // No arc found
        }
    }
}
