package SensorDataGenerators;

import java.time.LocalTime;

/**
 * An abstract class representing all sensor data generators
 */
public interface SensorDataGenerator {
    /**
     * Generates sensor data based on location and time.
     * @param x x-position of measurement.
     * @param y y-position of measurement.
     * @param time time of measurement.
     * @return sensor data based on location and time.
     */
    Byte generateData(Integer x, Integer y, LocalTime time);
}
