package datagenerator;

import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

/**
 * A class representing a sensor for carbon dioxide.
 */
public class CarbonDioxideDataGenerator implements SensorDataGenerator {

    private static final int DEFAULT_SEED = 1;
    private static Random random = new Random(DEFAULT_SEED);

    @Override
    public void reset() {
        random = new Random(DEFAULT_SEED);
    }

    public double generateData(int x, int y) {
        if (x < 200 && y < 230)
            return (double) 97 - 20 + (x + y) / 250 + 0.3 * random.nextGaussian();
        else if (x < 1000 && y < 1000)
            return 90 - 20 + Math.log10((x + y) / 50) + 0.3 * random.nextGaussian();
        else if (x < 1400 && y < 1400)
            return 95 - 20 + 3 * Math.cos(Math.PI * (x + y) / (150 * 8)) + 1.5 * Math.sin(Math.PI * (x + y) / (150 * 6)) + 0.3 * random.nextGaussian();
        else
            return 85 - 17.5 + (x + y) / 200 + 0.1 * random.nextGaussian();
    }
    public double nonStaticDataGeneration(Environment environment, GeoPosition position) {
        int x = (int) Math.round(environment.getMapHelper().toMapXCoordinate(position));
        int y = (int) Math.round(environment.getMapHelper().toMapYCoordinate(position));
        return generateData(x,y);
    }
    /**
     * A function generating senor data for carbon dioxide.
     * @param position The position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of carbon dioxide at the given position and time.
     */
    @Override
    public byte[] generateData(Environment environment, GeoPosition position, LocalDateTime time) {
        int x = (int) Math.round(environment.getMapHelper().toMapXCoordinate(position));
        int y = (int) Math.round(environment.getMapHelper().toMapYCoordinate(position));
        double result = generateData(x,y);
        return new byte[]{(byte) Math.floorMod((int) Math.round(result), 255)};
    }
}
