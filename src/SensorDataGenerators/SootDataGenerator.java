package SensorDataGenerators;

import java.time.LocalTime;
import java.util.Random;

/**
 * A class representing a sensor for soot.
 */
public class SootDataGenerator implements SensorDataGenerator {

    /**
     * A function generating senor data for soot.
     * @param x The x position of the measurement.
     * @param y The y position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of soot at the given position and time.
     */
    public Byte generateData(Integer x, Integer y, LocalTime time){
        Random random = new Random();
        if(x<210&&y< 230)
            return (byte)Math.floorMod((int) Math.round(97-10+(x+y)/250 +0.3*random.nextGaussian()),255);
        else if(x<1100&&y< 1100)
            return (byte)Math.floorMod((int) Math.round(98-10+Math.log10((x+y)/50)+0.3*random.nextGaussian()),255);
        else if(x<1400&&y< 1700)
            return (byte)Math.floorMod((int) Math.round(95 -4 +3*Math.cos(Math.PI*(x+y)/(150*8))+1.5*Math.sin(Math.PI*(x+y)/(150*6))+0.3*random.nextGaussian()),255);
        else
            return (byte)Math.floorMod((int) Math.round(85 -2 +(x+y)/200+0.1*random.nextGaussian()),255);
    }
}
