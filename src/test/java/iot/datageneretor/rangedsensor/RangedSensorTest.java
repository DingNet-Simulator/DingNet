package iot.datageneretor.rangedsensor;

import static org.junit.jupiter.api.Assertions.*;

import datagenerator.SensorDataGenerator;
import datagenerator.rangedsensor.api.RangeValue;
import datagenerator.rangedsensor.iaqsensor.AirQualityLevel;
import datagenerator.rangedsensor.iaqsensor.IAQDataGeneratorSingleton;
import iot.Characteristic;
import iot.Environment;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import java.time.LocalTime;
import java.util.Map;


public class RangedSensorTest {

    private static final SensorDataGenerator instance;

    static {
        new Environment(new Characteristic[10][10], new GeoPosition(5, 5), 100, Map.of(), Map.of());
        IAQDataGeneratorSingleton.setConfigFilePath("/sensorsConfig/IAQSensorConfig.toml");
        instance = IAQDataGeneratorSingleton.getInstance();
    }

    @Test
    public void defaultValue() {
        checkCell(1, 1, LocalTime.of(0, 0, 30), AirQualityLevel.GOOD); // in the file config GOOD is the default value
    }

    @Test
    public void changeValue() {
        checkCell(3, 4, LocalTime.of(0, 0, 1), AirQualityLevel.MODERATE);
        checkCell(3, 4, LocalTime.of(0, 2, 1), AirQualityLevel.POOR);
    }

    private void checkCell(int x, int y, LocalTime time, RangeValue expectedValue) {
        int value = instance.generateData(x, y, new GeoPosition(0, 0), time)[0];

        assertTrue(expectedValue.getLowerBound() <= value);
        assertTrue(expectedValue.getUpperBound() >= value);
    }
}
