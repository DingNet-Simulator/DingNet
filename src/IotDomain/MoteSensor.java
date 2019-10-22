package IotDomain;

import SensorDataGenerators.*;
import SensorDataGenerators.IAQSensor.IAQDataGeneratorSingleton;
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
    IAQ(IAQDataGeneratorSingleton.getInstance());

    MoteSensor(SensorDataGenerator sensorDataGenerator){
        this.sensorDataGenerator = sensorDataGenerator;
    }

    private SensorDataGenerator sensorDataGenerator;

    public byte[] getValue(Integer xpos, Integer ypos, LocalTime time){
        return sensorDataGenerator.generateData(xpos,ypos,time);
    }
    public double getValue(double xpos, double ypos){
        return sensorDataGenerator.nonStaticDataGeneration(xpos,ypos);
    }
    public List<Byte> getValueAsList(Integer xpos, Integer ypos, LocalTime time){
        var tmp = sensorDataGenerator.generateData(xpos,ypos,time);
        var ret = new LinkedList<Byte>();
        for (byte b : tmp) {
            ret.add(b);
        }
        return ret;
    }
    public byte[] getValue(Pair<Integer, Integer> pos, LocalTime time){
        return getValue(pos.getLeft(), pos.getRight(), time);
    }
    public List<Byte> getValueAsList(Pair<Integer, Integer> pos, LocalTime time){
        return getValueAsList(pos.getLeft(), pos.getRight(), time);
    }
}
