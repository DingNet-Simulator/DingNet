package iot.datageneretor.rangedsensor;

import static org.junit.jupiter.api.Assertions.*;

import datagenerator.rangedsensor.iaqsensor.AirQualityLevel;
import datagenerator.rangedsensor.iaqsensor.IAQDataGeneratorSingleton;
import iot.Characteristic;
import iot.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;

import java.time.LocalTime;
import java.util.Map;


public class RangedSensorTest {

    @Test
    public void defaultValue() {
        Characteristic[][] characteristics = new Characteristic[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                characteristics[i][j] = Characteristic.Plain;
            }
        }
        var env = new Environment(characteristics, new GeoPosition(5, 5), 100, Map.of(), Map.of());
        IAQDataGeneratorSingleton.setConfigFilePath("/sensorsConfig/IAQSensorConfig.toml");
        var instance = IAQDataGeneratorSingleton.getInstance();

        int value = instance.generateData(1, 1, new GeoPosition(0, 0), LocalTime.of(0, 0, 30))[0];

        var def = AirQualityLevel.GOOD; // in the file config GOOD is the default value
        assertTrue(def.getLowerBound() <= value);
        assertTrue(def.getUpperBound() >= value);
    }
}
