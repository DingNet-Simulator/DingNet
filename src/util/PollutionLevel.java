package util;

import java.util.Comparator;
import java.util.List;

public class PollutionLevel {
    // Indicates the air quality on a scale of 0 (good) to 1 (bad)
    private double level;

    public PollutionLevel(double level) {
        this.level = level;
    }

    private double getLevel() {
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
            throw new IllegalArgumentException("The list of air quality measurements should not be empty.");
        }

        measurements.sort(Comparator.comparing(Pair::getLeft));
        if (measurements.size() > 3) {
            measurements = measurements.subList(0, 3);
        }

        double totalInverted = measurements.stream()
            .mapToDouble(o -> 1 / o.getLeft())
            .sum();

        double resultingPollutionLevel = measurements.stream()
            .mapToDouble(e -> (1 / e.getLeft()) / totalInverted  * e.getRight().getLevel())
            .sum();

        return new PollutionLevel(resultingPollutionLevel);
    }
}
