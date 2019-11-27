package iot.networkentity;

import datagenerator.*;
import datagenerator.iaqsensor.IAQDataGeneratorSingleton;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

/**
 * An enum representing sensors for the motes.
 */
public enum MoteSensor {

    SOOT(new SootDataGenerator(), 1),
    OZONE(new OzoneDataGenerator(), 1),
    CARBON_DIOXIDE(new CarbonDioxideDataGenerator(), 1),
    PARTICULATE_MATTER(new ParticulateMatterDataGenerator(), 1),
    GPS(new GPSDataGenerator(),8),
    IAQ(IAQDataGeneratorSingleton.getInstance(), 1);


    private final SensorDataGenerator sensorDataGenerator;
    private final int amountOfData;

    MoteSensor(SensorDataGenerator sensorDataGenerator, int amountOfData) {
        this.sensorDataGenerator = sensorDataGenerator;
        this.amountOfData = amountOfData;
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
        return amountOfData;
    }
}
