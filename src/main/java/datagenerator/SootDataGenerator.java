package datagenerator;

import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

/**
 * A class representing a sensor for soot.
 */
public class SootDataGenerator implements SensorDataGenerator {

    private static final int DEFAULT_SEED = 1;
    private static Random random = new Random(DEFAULT_SEED);

    public double generateData(int x, int y) {
        if (x < 210 && y < 230)
            return (double) 97 - 10 + (x + y) / 250 + 30 * random.nextGaussian();
        else if (x < 1100 && y < 1100)
            return 98 - 10 + Math.log10((x + y) / 50) + 30 * random.nextGaussian();
        else if (x < 1400 && y < 1700)
            return 95 - 4 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 1.5 * Math.sin(Math.PI * (x + y) / (150 * 6)) + 30 * random.nextGaussian();
        else
            return 85 - 2 + (x + y) / 200 + 10 * random.nextGaussian();
    }
    public double nonStaticDataGeneration(Environment environment,GeoPosition position) {
        int x = (int) Math.round(environment.getMapHelper().toMapXCoordinate(position));
        int y = (int) Math.round(environment.getMapHelper().toMapYCoordinate(position));
        return generateData(x,y);
    }

    @Override
    public void reset() {
        random = new Random(DEFAULT_SEED);
    }

    /**
     * A function generating senor data for soot.
     * @param position The position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of soot at the given position and time.
     */
    @Override
    public byte[] generateData(Environment environment, GeoPosition position, LocalDateTime time) {
        int x = (int) Math.round(environment.getMapHelper().toMapXCoordinate(position));
        int y = (int) Math.round(environment.getMapHelper().toMapYCoordinate(position));
        double result = generateData(x,y);
        return new byte[]{(byte) Math.floorMod((int) Math.round(result), 255)};
    }
}
