package SensorDataGenerators;

import java.time.LocalTime;
import java.util.Random;

/**
 * A class representing a sensor for ozone.
 */
public class OzoneDataGenerator implements SensorDataGenerator {

    /**
     * A function generating senor data for ozone.
     * @param x The x position of the measurement.
     * @param y The y position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of ozone at the given position and time.
     */
    public Byte generateData(Integer x, Integer y, LocalTime time){
        Random random = new Random();
        if(x<200&&y< 200)
            return (byte)Math.floorMod((int) Math.round(97-30+(x+y)/250 +0.3*random.nextGaussian()),255);
        else if(x<1000&&y< 1000)
            return (byte)Math.floorMod((int) Math.round(98-30+Math.log10((x+y)/50)+0.3*random.nextGaussian()),255);
        else if(x<1200&&y< 1200)
            return (byte)Math.floorMod((int) Math.round(95 -24.5 +3*Math.cos(Math.PI*(x+y)/(150*8))+0.3*random.nextGaussian()),255);
        else
            return (byte)Math.floorMod((int) Math.round(85 -24 +(x+y)/200+0.1*random.nextGaussian()),255);
    }
}
