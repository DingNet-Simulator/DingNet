package SensorDataGenerators;

import java.time.LocalTime;
import java.util.Random;

/**
 * A class representing a sensor for carbon dioxide.
 */
public class CarbonDioxideDataGenerator implements SensorDataGenerator {

    /**
     * A function generating senor data for carbon dioxide.
     * @param x The x position of the measurement.
     * @param y The y position of the measurement.
     * @param time The time of the measurement.
     * @return A measurement of carbon dioxide at the given position and time.
     */
    public Byte generateData(Integer x, Integer y, LocalTime time){
        Random random = new Random();
        if(x<200&&y< 230)
            return (byte)Math.floorMod((int) Math.round(97-20+(x+y)/250 +0.3*random.nextGaussian()),255);
        else if(x<1000&&y< 1000)
            return (byte)Math.floorMod((int) Math.round(90-20+Math.log10((x+y)/50)+0.3*random.nextGaussian()),255);
        else if(x<1400&&y< 1400)
            return (byte)Math.floorMod((int) Math.round(95 -20 +3*Math.cos(Math.PI*(x+y)/(150*8))+1.5*Math.sin(Math.PI*(x+y)/(150*6))+0.3*random.nextGaussian()),255);
        else
            return (byte)Math.floorMod((int) Math.round(85 -17.5 +(x+y)/200+0.1*random.nextGaussian()),255);
    }
}
