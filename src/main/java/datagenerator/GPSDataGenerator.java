package datagenerator;

import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.MapHelper;
import util.Pair;

import java.time.LocalTime;

public class GPSDataGenerator implements SensorDataGenerator {
    private GeoPosition origin;

    public GPSDataGenerator() {
        this.origin = null;
    }

    public void setOrigin(GeoPosition origin) {
        this.origin = origin;
    }

    @Override
    public byte[] generateData(int x, int y, LocalTime time) {
        if (origin == null) {
            throw new IllegalStateException("Origin should be set first before using the GPS data generator");
        }
        return Converter.toByteArray(new GeoPosition(MapHelper.toLatitude(y, origin), MapHelper.toLongitude(x, origin)));
    }
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time){
        return this.generateData(pos.getLeft(), pos.getRight(), time);
    }
    public double nonStaticDataGeneration(double x, double y) {
        return 0.0;
    }

    @Override
    public void reset() {

    }
}
