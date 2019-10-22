package SensorDataGenerators;

import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.nio.ByteBuffer;
import java.time.LocalTime;

public class RequestPathSensor implements SensorDataGenerator {

    private final GeoPosition destination = new GeoPosition(1,1);
    private final LocalTime whenAskPath = LocalTime.of(0, 15);
    private boolean alreadyRequested = false;

    @Override
    public byte[] generateData(Integer x, Integer y, LocalTime time) {
        if (!alreadyRequested && whenAskPath.isBefore(time)) {
            alreadyRequested = true;
            byte[] data= new byte[8];
            ByteBuffer.wrap(data, 0, 4).putFloat((float)destination.getLatitude());
            ByteBuffer.wrap(data, 4, 4).putFloat((float)destination.getLongitude());
            return data;
        }
        return new byte[0];
    }

    @Override
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time) {
        return generateData(pos.getLeft(), pos.getRight(), time);
    }

    @Override
    public double nonStaticDataGeneration(double x, double y) {
        return 0;
    }
}
