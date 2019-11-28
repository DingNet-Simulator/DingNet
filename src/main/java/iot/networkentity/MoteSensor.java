package iot.networkentity;

import datagenerator.*;
import datagenerator.rangedsensor.iaqsensor.IAQDataGeneratorSingleton;
import datagenerator.rangedsensor.no2sensor.NO2DataGeneratorSingleton;
import datagenerator.rangedsensor.pm10sensor.PM10DataGeneratorSingleton;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

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

    public byte[] getValue(int xpos, int ypos, GeoPosition graphPosition, LocalTime time) {
        return sensorDataGenerator.generateData(xpos,ypos, graphPosition, time);
    }

    public double getValue(double xpos, double ypos) {
        return sensorDataGenerator.nonStaticDataGeneration(xpos,ypos);
    }

    public List<Byte> getValueAsList(int xpos, int ypos, GeoPosition graphPosition, LocalTime time) {
        var tmp = sensorDataGenerator.generateData(xpos, ypos, graphPosition, time);
        var ret = new LinkedList<Byte>();
        for (byte b : tmp) {
            ret.add(b);
        }
        return ret;
    }

    public byte[] getValue(Pair<Integer, Integer> pos, GeoPosition graphPosition, LocalTime time) {
        return getValue(pos.getLeft(), pos.getRight(), graphPosition, time);
    }

    public List<Byte> getValueAsList(Pair<Integer, Integer> pos, GeoPosition graphPosition, LocalTime time) {
        return getValueAsList(pos.getLeft(), pos.getRight(), graphPosition, time);
    }

    public SensorDataGenerator getSensorDataGenerator() {
        return sensorDataGenerator;
    }

    public int getAmountOfData() {
        return getSensorDataGenerator().getAmountOfData();
    }
}
