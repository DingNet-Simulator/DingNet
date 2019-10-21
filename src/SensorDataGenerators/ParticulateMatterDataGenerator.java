package SensorDataGenerators;

import util.Pair;

import java.time.LocalTime;
import java.util.Random;
/**
 * A class representing a sensor for particulate matter.
 */
public class ParticulateMatterDataGenerator implements SensorDataGenerator{

    public static Double generateData(double x, double y) {
        Random random = new Random();
        if (x < 250 && y < 250)
            return (double) 97 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 750 && y < 750)
            return 90 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1250 && y < 1250)
            return 95 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 1.5 * Math.sin(Math.PI * (x + y) / (150 * 6)) + 0.3 * random.nextGaussian();
        else
            return 85 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }

    /**
     * A function generating senor data for particulate matter.
     * @param x The x position of the measurement.
     * @param y The y position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of particulate matter at the given position and time.
     */
    public byte[] generateData(Integer x, Integer y, LocalTime time){
        Double result = ParticulateMatterDataGenerator.generateData((double)x,(double)y);
        return new byte[]{(byte)Math.floorMod((int) Math.round(result),255)};
    }
    public byte[] generateData(Pair<Integer, Integer> pos, LocalTime time){
        return this.generateData(pos.getLeft(), pos.getRight(), time);
    }
}
