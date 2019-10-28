package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PollutionGrid {
    // TODO do we assume static motes here? otherwise this becomes more difficult
    //  -> i.e., including some notion of time, and making sure old measurements are dropped after a while
    private Map<GeoPosition, PollutionLevel> pollutionMeasurements;

    private static PollutionGrid instance;

    public static PollutionGrid getInstance() {
        if (instance == null) {
            instance = new PollutionGrid();
        }
        return instance;
    }

    private PollutionGrid() {
        pollutionMeasurements = new HashMap<>();
    }


    public void addMeasurement(GeoPosition position, PollutionLevel level) {
        this.pollutionMeasurements.put(position, level);
    }

    /**
     * Calculates the pollution level in a given position (approximate level based on available measurements)
     * @param position The position at which the pollution should be calculated.
     * @return The level of pollution at {@code position}.
     */
    public PollutionLevel getPollutionLevel(GeoPosition position) {
        if (pollutionMeasurements.containsKey(position)) {
            return pollutionMeasurements.get(position);
        }

        // Calculate some mean pollution based on the distance of other measurements
        // NOTE: this does not take the time of the measurement into account, only the distance
        List<Pair<Double, PollutionLevel>> distances = pollutionMeasurements.entrySet().stream()
            .map(e -> new Pair<>(MapHelper.distance(e.getKey(), position), e.getValue()))
            .collect(Collectors.toList());

        return PollutionLevel.getMediumPollution(distances);
    }
}
