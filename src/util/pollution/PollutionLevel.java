package util.pollution;

import util.Pair;

import java.util.Comparator;
import java.util.List;

public class PollutionLevel {
    // Indicates the air quality on a scale of 0 (good) to 1 (bad)
    private double level;

    public PollutionLevel(double level) {
        this.level = level;
    }

    public double getPollutionFactor() {
        return level;
    }

    /**
     * Calculates the medium pollution level based on the provided air quality measurements
     * and their distance to the desired position.
     * @param measurements A list of air quality measurements and their distances to the desired position.
     * @return A pollution level for the position.
     */
    static PollutionLevel getMediumPollution(List<Pair<Double, PollutionLevel>> measurements) {
        if (measurements.isEmpty()) {
            // In case no measurements are present yet, choose a default value of 0
            return new PollutionLevel(0);
        }

        measurements.sort(Comparator.comparing(Pair::getLeft));
        if (measurements.size() > 3) {
            measurements = measurements.subList(0, 3);
        }

        /*
         Formula used (by example):
            Point 1 -> distance = 100, pollutionLevel = 0.9
            Point 2 -> distance = 500, pollutionLevel = 0.3
            Point 3 -> distance = 400, pollutionLevel = 0.2

            Resulting pollutionLevel =
                0.9 * (1 / 100) / ((1/100) + (1/500) + (1/400)) +
                0.3 * (1 / 500) / ((1/100) + (1/500) + (1/400)) +
                0.2 * (1 / 400) / ((1/100) + (1/500) + (1/400))
                                     ~= 0.68
         */

        double totalInverted = measurements.stream()
            .mapToDouble(o -> 1 / o.getLeft())
            .sum();

        double resultingPollutionLevel = measurements.stream()
            .mapToDouble(e -> (1 / e.getLeft()) / totalInverted  * e.getRight().getPollutionFactor())
            .sum();

        return new PollutionLevel(resultingPollutionLevel);
    }
}
