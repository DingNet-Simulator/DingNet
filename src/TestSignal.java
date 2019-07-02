import IotDomain.Characteristic;
import IotDomain.Environment;
import IotDomain.Gateway;
import IotDomain.Mote;
import org.jxmapviewer.viewer.GeoPosition;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;

public class TestSignal {
    public static void main(String[] args)
    {
        Characteristic[][] map = new Characteristic[2000][2000];
        for(int i =0; i < 2000; i++){
            for(int j =0; j < 2000; j++){
                map[i][j] = Characteristic.Forest;
            }
        }
        Random random = new Random();
        GeoPosition mapzero = new GeoPosition(50.853718, 4.673155);
        Environment environment = new Environment(map, mapzero, new LinkedHashSet<>());
        new Gateway(random.nextLong(),100, 100,
                environment, 14,12);
        new Mote(random.nextLong(),0,
                0,
                environment, 14,12, new LinkedList<>(),0,new LinkedList<>(),10,0.5);
        Mote mote = environment.getMotes().get(0);
        mote.sendToGateWay(new Byte[0],new HashMap<>());
        System.out.println(mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns()-1).get(0).getTransmissionPower() + ", "+mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns()-1).get(1).getTransmissionPower());
    }
}


