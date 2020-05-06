package iot.networkentity;

import datagenerator.*;
import datagenerator.rangedsensor.iaqsensor.IAQDataGenerator;
import datagenerator.rangedsensor.no2sensor.NO2DataGenerator;
import datagenerator.rangedsensor.pm10sensor.PM10DataGenerator;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;
import util.time.Time;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An enum representing sensors for the motes.
 */
public enum MoteSensor {

    SOOT(SootDataGenerator::new),
    OZONE(OzoneDataGenerator::new),
    CARBON_DIOXIDE(CarbonDioxideDataGenerator::new),
    PARTICULATE_MATTER(ParticulateMatterDataGenerator::new),
    GPS(GPSDataGenerator::new),
    PM10(PM10DataGenerator::new),
    IAQ(IAQDataGenerator::new),
    NO2(NO2DataGenerator::new);

    private SensorDataGenerator sensorDataGenerator;
    private final Supplier<SensorDataGenerator> constructor;

    MoteSensor(Supplier<SensorDataGenerator> constructor) {
        this.constructor = constructor;
    }

    //init function is called every time a configuration is loaded
    public void init() {
        sensorDataGenerator = constructor.get();
    }

    public byte[] getValue(int xpos, int ypos, GeoPosition graphPosition, Time time) {
        return sensorDataGenerator.generateData(xpos, ypos, graphPosition, time);
    }

    public double getValue(double xpos, double ypos) {
        return sensorDataGenerator.nonStaticDataGeneration(xpos, ypos);
    }

    public List<Byte> getValueAsList(int xpos, int ypos, GeoPosition graphPosition, Time time) {
        var tmp = sensorDataGenerator.generateData(xpos, ypos, graphPosition, time);
        var ret = new LinkedList<Byte>();
        for (byte b : tmp) {
            ret.add(b);
        }
        return ret;
    }

    public byte[] getValue(Pair<Integer, Integer> pos, GeoPosition graphPosition, Time time) {
        return getValue(pos.getLeft(), pos.getRight(), graphPosition, time);
    }

    public List<Byte> getValueAsList(Pair<Integer, Integer> pos, GeoPosition graphPosition, Time time) {
        return getValueAsList(pos.getLeft(), pos.getRight(), graphPosition, time);
    }

    public SensorDataGenerator getSensorDataGenerator() {
        return sensorDataGenerator;
    }

    public int getAmountOfData() {
        return getSensorDataGenerator().getAmountOfData();
    }
}
