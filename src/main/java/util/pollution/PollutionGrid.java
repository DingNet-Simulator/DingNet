package util.pollution;

import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;
import util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PollutionGrid {
    // TODO do we assume static motes here? otherwise this becomes more difficult
    //  -> i.e., including some notion of time, and making sure old measurements are dropped after a while
    private Map<Long, Pair<GeoPosition, PollutionLevel>> pollutionMeasurements;

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


    public void addMeasurement(long deviceEUI, GeoPosition position, PollutionLevel level) {
        this.pollutionMeasurements.put(deviceEUI, new Pair<>(position, level));
    }

    /**
     * Calculates the pollution level in a given position (approximate level based on available measurements)
     * @param position The position at which the pollution should be calculated.
     * @return The level of pollution at {@code position}.
     */
    public PollutionLevel getPollutionLevel(GeoPosition position) {
        var pollutionAtPosition = pollutionMeasurements.values().stream()
            .filter(o -> o.getLeft().equals(position))
            .map(Pair::getRight)
            .findFirst();
        if (pollutionAtPosition.isPresent()) {
            return pollutionAtPosition.get();
        }

        // Calculate some mean pollution based on the distance of other measurements
        // NOTE: this does not take the time of the measurement into account, only the distance
        List<Pair<Double, PollutionLevel>> distances = pollutionMeasurements.values().stream()
            .map(e -> new Pair<>(MapHelper.distance(e.getLeft(), position), e.getRight()))
            .collect(Collectors.toList());

        return PollutionLevel.getMediumPollution(distances);
    }
}
