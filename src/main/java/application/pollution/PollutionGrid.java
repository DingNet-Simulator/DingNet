package application.pollution;

import org.jxmapviewer.viewer.GeoPosition;

public interface PollutionGrid {
    /**
     * Add a measurement to the pollution grid
     * @param deviceEUI The device from which the measurement originated.
     * @param position The position at which the measurement was taken.
     * @param level The pollution level measured by the device in the given position.
     */
    void addMeasurement(long deviceEUI, GeoPosition position, PollutionLevel level);

    /**
     * Calculates the pollution level in a given position (approximate level based on available measurements)
     * @param position The position at which the pollution should be calculated.
     * @return The level of pollution at {@code position}.
     */
    double getPollutionLevel(GeoPosition position);

    /**
     * Removes all stored pollution measurements.
     */
    void clean();
}
