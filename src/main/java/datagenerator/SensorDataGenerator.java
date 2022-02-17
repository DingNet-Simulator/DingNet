package datagenerator;

import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * An abstract class representing all sensor data generators
 */
public interface SensorDataGenerator {
    /**
     * Generates sensor data based on location and time.
     * @param graphPosition position of the mote inside the graph
     * @param time time of measurement.
     * @return sensor data based on location and time.
     */
    byte[] generateData(Environment environment, GeoPosition graphPosition, LocalDateTime time);
    double nonStaticDataGeneration(Environment environment, GeoPosition position);

    /**
     *
     * @return number of byte generated from the sensor
     */
    default int getAmountOfData() {
        return 1;
    }

    void reset();
}
