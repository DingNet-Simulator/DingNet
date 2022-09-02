package datagenerator;

import iot.environment.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;

import java.time.LocalDateTime;

public class GPSDataGenerator implements SensorDataGenerator {

    public GPSDataGenerator() {}

    @Override
    public byte[] generateData(Environment environment, GeoPosition graphPosition, LocalDateTime time) {
        return Converter.toByteArray(graphPosition);
    }

    public double nonStaticDataGeneration(Environment environment, GeoPosition position) {
        return 0.0;
    }

    @Override
    public int getAmountOfData() {
        return Float.BYTES * 2;
    }

    @Override
    public void reset() {

    }
}
