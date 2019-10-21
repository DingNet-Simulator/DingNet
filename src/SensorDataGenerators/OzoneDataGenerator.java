package SensorDataGenerators;

import util.Pair;

import java.time.LocalTime;
import java.util.Random;

/**
 * A class representing a sensor for ozone.
 */
public class OzoneDataGenerator implements SensorDataGenerator {

    public static Double generateData(double x, double y) {
        Random random = new Random();
        if (x < 200 && y < 200)
            return (double) 97 - 30 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 1000 && y < 1000)
            return 98 - 30 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1200 && y < 1200)
            return 95 - 24.5 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 0.3 * random.nextGaussian();
        else
            return 85 - 24 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }

    /**
     * A function generating senor data for ozone.
     * @param x The x position of the measurement.
     * @param y The y position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of ozone at the given position and time.
     */
    public byte[] generateData(Integer x, Integer y, LocalTime time){
        Double result = OzoneDataGenerator.generateData((double)x, (double) y);
        return new byte[]{(byte)Math.floorMod((int) Math.round(result),255)};
    }
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time){
        return this.generateData(pos.getLeft(), pos.getRight(), time);
    }
}
