package iot.networkentity;

import datagenerator.*;
import datagenerator.rangedsensor.iaqsensor.IAQDataGeneratorSingleton;
import datagenerator.rangedsensor.no2sensor.NO2DataGeneratorSingleton;
import datagenerator.rangedsensor.pm10sensor.PM10DataGeneratorSingleton;
import iot.Environment;
import org.jxmapviewer.viewer.GeoPosition;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

/**
 * An enum representing sensors for the motes.
 */
public enum MoteSensor {

    SOOT(new SootDataGenerator()),
    OZONE(new OzoneDataGenerator()),
    CARBON_DIOXIDE(new CarbonDioxideDataGenerator()),
    PARTICULATE_MATTER(new ParticulateMatterDataGenerator()),
    GPS(new GPSDataGenerator()),
    IAQ(IAQDataGeneratorSingleton.getInstance()),
    PM10(PM10DataGeneratorSingleton.getInstance()),
    NO2(NO2DataGeneratorSingleton.getInstance());


    private final SensorDataGenerator sensorDataGenerator;

    MoteSensor(SensorDataGenerator sensorDataGenerator) {
        this.sensorDataGenerator = sensorDataGenerator;
    }

    public double getValue(Environment environment, GeoPosition position) {
        return sensorDataGenerator.nonStaticDataGeneration(environment, position);
    }

    public List<Byte> getValueAsList(Environment environment, GeoPosition graphPosition, LocalDateTime time) {
        var tmp = sensorDataGenerator.generateData(environment, graphPosition, time);
        var ret = new LinkedList<Byte>();
        for (byte b : tmp) {
            ret.add(b);
        }
        return ret;
    }

    public byte[] getValue(GeoPosition graphPosition, LocalTime time) {
        return getValue(graphPosition, time);
    }

    public SensorDataGenerator getSensorDataGenerator() {
        return sensorDataGenerator;
    }

    public int getAmountOfData() {
        return getSensorDataGenerator().getAmountOfData();
    }
}
