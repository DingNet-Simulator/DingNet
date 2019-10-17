package SensorDataGenerators;

import util.MapHelper;
import util.Pair;

import java.nio.ByteBuffer;
import java.time.LocalTime;

public class GPSDataGenerator implements SensorDataGenerator {

    @Override
    public byte[] generateData(Integer x, Integer y, LocalTime time) {
        byte[] data= new byte[16];
        ByteBuffer.wrap(data, 0, 8).putDouble(MapHelper.getInstance().toLatitude(y));
        ByteBuffer.wrap(data, 8, 8).putDouble(MapHelper.getInstance().toLongitude(x));
        return data;
    }
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time){
        return this.generateData(pos.getLeft(), pos.getRight(), time);
    }
}
