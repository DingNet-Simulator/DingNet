package datagenerator.rangedsensor.abstractimpl;

import com.uchuhimo.konf.BaseConfig;
import com.uchuhimo.konf.Config;
import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.api.Cell;
import datagenerator.rangedsensor.api.RangeValue;
import datagenerator.rangedsensor.api.TimeUnit;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 *      -------------
 *      | 1 | 2 | 3 |
 *      -------------
 *      | 4 | 5 | 6 |
 *      -------------
 *      | 7 | 8 | 9 |
 *      -------------
 */
abstract public class RangeDataGenerator implements SensorDataGenerator {

    protected static SensorDataGenerator instance;
    private static String configFilePath;
    private final int row;
    private final int columns;
    private final int width;
    private final int height;
    private final RangeValue defaultLevel;
    private final TimeUnit timeUnit;
    private final Map<Integer, List<Cell>> map;

    public RangeDataGenerator(AbstractSensorConfigSpec<?, ?> sensorConfig) {
        width = Environment.getMapWidth();
        height = Environment.getMapHeight();
        Config config = new BaseConfig();
        config.addSpec(sensorConfig.SPEC);
        config = config.from().toml.inputStream(this.getClass().getResourceAsStream(getConfigFilePath()));
        row = config.get(sensorConfig.row);
        columns = config.get(sensorConfig.columns);
        defaultLevel = config.get(sensorConfig.defaultLevel);
        timeUnit = config.get(sensorConfig.timeUnit);
        map = config.get(sensorConfig.cells).stream().collect(Collectors.groupingBy(Cell::getCellNumber));
        map.forEach((e, v) -> v.sort((c1, c2) -> Double.compare(c2.getFromTime(), c1.getFromTime())));
    }

    private String getConfigFilePath() {
        return configFilePath != null ? configFilePath : getDefaultConfigFilePath();
    }

    public static void setConfigFilePath(String path) {
        if (instance != null) {
            throw new IllegalStateException("sensor instance already created");
        }
        configFilePath = path;
    }

    protected abstract String getDefaultConfigFilePath();

    @Override
    public byte[] generateData(int x, int y, GeoPosition graphPosition, LocalTime time) {
        return map.getOrDefault(calcSquare(x, y), new LinkedList<>()).stream()
            .filter(c -> c.getFromTime() < timeUnit.convertFromNano(time.toNanoOfDay()))
            .findFirst()// the list of cell is ordered for time
            .map(Cell::getLevel)
            .orElse(defaultLevel)
            .getValue();
    }

    @Override
    public byte[] generateData(Pair<Integer, Integer> pos, GeoPosition graphPosition, LocalTime time) {
        return generateData(pos.getLeft(), pos.getRight(), graphPosition, time);
    }

    @Override
    public double nonStaticDataGeneration(double x, double y) {
        return 0.0;
    }

    private int calcSquare(int x, int y) {
        //`(height - y)` because in the simulator environment the origin is in the bottom left corner
        int moteRow = (height - y) / (height / row);
        int moteCol = x / (width / columns);
        return moteRow * columns + moteCol;
    }

    @Override
    public void reset() {

    }
}
