package application.pollution;

import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;
import util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Pollution grid implementation based on the nearest sensed value
 */
public class PollutionGridImpl implements PollutionGrid {
    // FIXME synchronized is necessary here, otherwise concurrent modification exceptions are thrown
    //  (even though the GUI updating should happen synchronously with invokeAndWait)

    // The pollution measurements: for each device, the most recent measurement (location + pollution level) is stored
    private Map<Long, Pair<GeoPosition, PollutionLevel>> pollutionMeasurements;


    public PollutionGridImpl() {
        pollutionMeasurements = new HashMap<>();
    }


    @Override
    public void addMeasurement(long deviceEUI, GeoPosition position, PollutionLevel level) {
        synchronized (this) {
            this.pollutionMeasurements.put(deviceEUI, new Pair<>(position, level));
        }
    }

    @Override
    public double getPollutionLevel(GeoPosition position) {
        synchronized (this) {
            var pollutionAtPosition = pollutionMeasurements.values().stream()
                .filter(o -> o.getLeft().equals(position))
                .map(Pair::getRight)
                .findFirst();
            if (pollutionAtPosition.isPresent()) {
                return pollutionAtPosition.get().getPollutionFactor();
            }

            // Calculate some mean pollution based on the distance of other measurements
            // NOTE: this does not take the time of the measurement into account, only the distance
            List<Pair<Double, PollutionLevel>> distances = pollutionMeasurements.values().stream()
                .map(e -> new Pair<>(MapHelper.distance(e.getLeft(), position), e.getRight()))
                .collect(Collectors.toList());

            return PollutionLevel.getMediumPollution(distances).getPollutionFactor();
        }
    }

    @Override
    public void clean() {
        pollutionMeasurements = new HashMap<>();
    }
}
