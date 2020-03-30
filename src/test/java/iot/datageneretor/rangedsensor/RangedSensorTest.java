package iot.datageneretor.rangedsensor;

import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.api.RangeValue;
import datagenerator.rangedsensor.iaqsensor.AirQualityLevel;
import datagenerator.rangedsensor.iaqsensor.IAQDataGenerator;
import iot.Characteristic;
import iot.Environment;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.time.DoubleTime;
import util.time.Time;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class RangedSensorTest {

    private static final SensorDataGenerator instance;

    static {
        new Environment(new Characteristic[10][10], new GeoPosition(5, 5), 100, Map.of(), Map.of());
//        IAQDataGenerator.setConfigFilePath("/sensorsConfig/IAQSensorConfig.toml");
        instance = new IAQDataGenerator("/sensorsConfig/IAQSensorConfig.toml");
    }

    @Test
    public void defaultValue() {
        checkCell(1, 1, DoubleTime.fromSeconds(30), AirQualityLevel.GOOD); // in the file config GOOD is the default value
    }

    @Test
    public void changeValue() {
        checkCell(3, 4, DoubleTime.fromSeconds(1), AirQualityLevel.MODERATE);
        checkCell(3, 4, DoubleTime.fromSeconds(1).plusMinutes(2), AirQualityLevel.POOR);
    }

    private void checkCell(int x, int y, Time time, RangeValue expectedValue) {
        int value = instance.generateData(x, y, new GeoPosition(0, 0), time)[0];

        assertTrue(expectedValue.getLowerBound() <= value);
        assertTrue(expectedValue.getUpperBound() >= value);
    }
}
