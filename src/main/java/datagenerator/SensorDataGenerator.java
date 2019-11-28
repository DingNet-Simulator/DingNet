package datagenerator;

import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.time.LocalTime;

/**
 * An abstract class representing all sensor data generators
 */
public interface SensorDataGenerator {
    /**
     * Generates sensor data based on location and time.
     * @param x x-position of measurement.
     * @param y y-position of measurement.
     * @param graphPosition position of the mote inside the graph
     * @param time time of measurement.
     * @return sensor data based on location and time.
     */
    byte[] generateData(int x, int y, GeoPosition graphPosition, LocalTime time);
    byte[] generateData(Pair<Integer, Integer> pos, GeoPosition graphPosition, LocalTime time);
    double nonStaticDataGeneration(double x, double y);

    /**
     *
     * @return number of byte generated from the sensor
     */
    default int getAmountOfData() {
        return 1;
    }

    void reset();
}
