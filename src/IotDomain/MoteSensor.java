package IotDomain;

import SensorDataGenerators.*;
import util.Pair;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

/**
 * An enum representing sensors for the motes.
 */
public enum MoteSensor {

    SOOT(new SootDataGenerator()),OZONE(new OzoneDataGenerator()),CARBON_DIOXIDE(new CarbonDioxideDataGenerator()),PARTICULATE_MATTER(new ParticulateMatterDataGenerator());

    MoteSensor(SensorDataGenerator sensorDataGenerator){
        this.sensorDataGenerator = sensorDataGenerator;
    }

    private SensorDataGenerator sensorDataGenerator;

    public byte[] getValue(Integer xpos, Integer ypos, LocalTime time){
        return sensorDataGenerator.generateData(xpos,ypos,time);
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
        return sensorDataGenerator.generateData(pos,time);
    }
    public List<Byte> getValueAsList(Pair<Integer, Integer> pos, LocalTime time){
        var tmp = sensorDataGenerator.generateData(pos,time);
        var ret = new LinkedList<Byte>();
        for (byte b : tmp) {
            ret.add(b);
        }
        return ret;
    }
}
