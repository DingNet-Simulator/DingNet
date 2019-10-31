package SensorDataGenerators;

import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.MapHelper;
import util.Pair;

import java.time.LocalTime;

public class GPSDataGenerator implements SensorDataGenerator {

    @Override
    public byte[] generateData(Integer x, Integer y, LocalTime time) {
        return Converter.toByteArray(new GeoPosition(MapHelper.getInstance().toLatitude(y), MapHelper.getInstance().toLongitude(x)));
    }
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time){
        return this.generateData(pos.getLeft(), pos.getRight(), time);
    }
    public double nonStaticDataGeneration(double x, double y) {
        return 0.0;
    }
}
