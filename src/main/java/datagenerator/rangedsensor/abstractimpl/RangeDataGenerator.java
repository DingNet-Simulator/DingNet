package datagenerator.rangedsensor.abstractimpl;

import com.uchuhimo.konf.BaseConfig;
import com.uchuhimo.konf.Config;
import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.api.Cell;
import datagenerator.rangedsensor.api.RangeValue;
import iot.Environment;
import org.apache.commons.math3.analysis.interpolation.TricubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.TricubicInterpolator;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;
import util.time.DoubleTime;
import util.time.Time;
import util.time.TimeUnit;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 *  Base range data generator for an environment split in a matrix as follow:
 *      -------------
 *      | 1 | 2 | 3 |
 *      -------------
 *      | 4 | 5 | 6 |
 *      -------------
 *      | 7 | 8 | 9 |
 *      -------------
 *
 *  This generator load the sensor configuration from a file and generate a tricubic spline
 *  with the following coordinates: the two matrix's coordinates and the time, producing a
 *  spatio-temporal function to produce sensed values
 */
abstract public class RangeDataGenerator implements SensorDataGenerator {

    private final int row;
    private final int columns;
    private final int width;
    private final int height;
    private final double samplesTime;
    private final double finalTime;
    private final RangeValue defaultLevel;
    private final TimeUnit timeUnit;
    private final Map<Integer, List<Cell>> map;
    private final TricubicInterpolatingFunction function;

    public RangeDataGenerator(AbstractSensorConfigSpec<?, ?> sensorConfig, @NotNull String configFilePath) {
        width = Environment.getMapWidth();
        height = Environment.getMapHeight();
        Config config = new BaseConfig();
        config.addSpec(sensorConfig.SPEC);
        config = config.from().toml.inputStream(this.getClass().getResourceAsStream(configFilePath));
        row = config.get(sensorConfig.row);
        columns = config.get(sensorConfig.columns);
        defaultLevel = config.get(sensorConfig.defaultLevel);
        timeUnit = config.get(sensorConfig.timeUnit);
        samplesTime = config.get(sensorConfig.samplesTime);
        finalTime = config.get(sensorConfig.finalTime);
        map = config.get(sensorConfig.cells).stream().collect(Collectors.groupingBy(Cell::getCellNumber));
        map.forEach((e, v) -> v.sort((c1, c2) -> Double.compare(c2.getFromTime(), c1.getFromTime())));
        function = initFunction();
    }

    private TricubicInterpolatingFunction initFunction() {
        var random = new Random(1);
        var xval = new double[row];
        IntStream.rangeClosed(1, row).forEach(i -> xval[i - 1] = i);
        var yval = new double[columns];
        IntStream.rangeClosed(1, columns).forEach(i -> yval[i - 1] = i);
        var times = map.entrySet().stream()
            .flatMap(e -> e.getValue().stream())
            .map(Cell::getFromTime)
            .collect(Collectors.toSet());
        for (double i = 0; i <= finalTime; i += samplesTime) {
            times.add(i);
        }
        var zval = new double[times.size()];
        var orderedTimes = times.stream().sorted().collect(Collectors.toList());
        IntStream.range(0, orderedTimes.size()).forEach(i -> zval[i] = orderedTimes.get(i));
        var fval = new double[xval.length][yval.length][zval.length];
        for (int x = 0; x < xval.length; x++) {
            for (int y = 0; y < yval.length; y++) {
                for (int z = 0; z < zval.length; z++) {
                    var level = getCellLevel(xval[x], yval[y], DoubleTime.zero().plusSeconds(timeUnit.convertTo(zval[z], TimeUnit.SECONDS)));
                    fval[x][y][z] = level.getLowerBound() + (level.getUpperBound() - level.getLowerBound()) * random.nextDouble();
                }
            }
        }
        return new TricubicInterpolator().interpolate(xval, yval, zval, fval);
    }

    private double getFunValue(double x, double y, Time time) {
        return function.value(x, y, time.getAs(timeUnit));
    }

    private byte[] convertFunValue(double val) {
        switch (getAmountOfData()) {
            case 1: return new byte[] {(byte) val};
            case 2: {
                var ret = new byte[2];
                ByteBuffer.wrap(ret).putShort((short) val);
                return ret;
            }
            case 4: {
                var ret = new byte[4];
                ByteBuffer.wrap(ret).putInt((int) val);
                return ret;
            }
            default: throw new IllegalStateException("unable to manage this amount of data");
        }
    }

    private RangeValue getCellLevel(double x, double y, Time time) {
        return map.getOrDefault((int)(x * columns + y), new LinkedList<>()).stream()
            .filter(c -> timeUnit.convertTo(c.getFromTime(), TimeUnit.MILLIS) <= time.asMilli())
            .findFirst()// the list of cell is ordered for time
            .map(Cell::getLevel)
            .orElse(defaultLevel);
    }

    @Override
    public byte[] generateData(int x, int y, GeoPosition graphPosition, Time time) {
        return convertFunValue(getFunValue(getRow(y), getColumns(x), time));
    }

    @Override
    public byte[] generateData(Pair<Integer, Integer> pos, GeoPosition graphPosition, Time time) {
        return generateData(pos.getLeft(), pos.getRight(), graphPosition, time);
    }

    @Override
    public double nonStaticDataGeneration(double x, double y) {
        return 0.0;
    }

    private int getRow(int y) {
        return (height - y) / (height / row);
    }

    private int getColumns(int x) {
        return x / (width / columns);
    }

    @Override
    public void reset() {

    }
}
