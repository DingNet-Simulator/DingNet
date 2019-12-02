package datagenerator;

import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.Pair;

import java.time.LocalTime;

public class GPSDataGenerator implements SensorDataGenerator {

    public GPSDataGenerator() {}

    @Override
    public byte[] generateData(int x, int y, GeoPosition graphPosition, LocalTime time) {
        return Converter.toByteArray(graphPosition);
    }

    @Override
    public byte[] generateData(Pair<Integer, Integer> pos, GeoPosition graphPosition, LocalTime time) {
        return this.generateData(pos.getLeft(), pos.getRight(), graphPosition, time);
    }

    public double nonStaticDataGeneration(double x, double y) {
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
