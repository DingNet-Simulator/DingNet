package iot.environment;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherMap {

    List<List<WeatherType>> weather;
    int maxXPos;
    int maxYPos;
    long zoneWidth;
    long zoneHeight;
    long amountOfColumns;
    long amountOfRows;
    WeatherType defaultWeatherType = WeatherType.Clear;


    public WeatherMap(int maxXPos, int maxYPos, long numberOfColumns, long numberOfRows){
        this.maxXPos = maxXPos;
        this.maxYPos = maxYPos;
        this.zoneWidth = maxXPos / numberOfColumns;
        this.zoneHeight = maxYPos / numberOfRows;
        this.amountOfColumns= numberOfColumns;
        this.amountOfRows = numberOfRows;
        weather = new ArrayList<>();
        for(int i = 0; i <= numberOfColumns;i++){
            weather.add(new ArrayList<>());
            for(int j = 0; j <= numberOfRows;j++) {
                weather.get(i).add(defaultWeatherType);
            }
        }




    }

    public void setNumberOfzones(int numberOfColumns, int numberOfRows){

        this.zoneWidth = maxXPos / numberOfColumns;
        this.zoneHeight = maxYPos / numberOfRows;
        weather = new ArrayList<>();
        for(int i = 0; i <= numberOfColumns;i++){
            weather.add(new ArrayList<>());
            for(int j = 0; j <= numberOfRows;j++) {
                weather.get(i).add(defaultWeatherType);
            }
        }
    }

    public void eval(WeatherType characteristic, int zoneColumn, int zoneRow){
        weather.get(zoneColumn).remove(zoneRow);
        weather.get(zoneColumn).add(zoneRow,characteristic);
    }
    public void randomChange(Random random, int zoneColumn, int zoneRow){
        int id = random.nextInt(WeatherType.values().length);
        weather.get(zoneColumn).remove(zoneRow);
        weather.get(zoneColumn).add(zoneRow,WeatherType.values()[id]);
    }

    public WeatherType getCharacteristic(double x, double y){
        int columnNumber = (int) Math.floor(x / zoneWidth);
        int rowNumber = (int) Math.floor(y / zoneHeight);
        return weather.get(columnNumber).get(rowNumber);
    }

    public int getMaxXPos() {
        return maxXPos;
    }

    public int getMaxYPos() {
        return maxYPos;
    }

    /**
     * A class representing the transmission characteristics of a certain location.
     */
    public enum WeatherType {

        //HeavyRain(0, new Color(0, 73, 102, 168)),
        Clear(0.7104, new Color(59, 174, 183, 168)),
        Thunderstorm(7.8, new Color(42, 69, 84, 168));
        /**
         * An integer representing the path loss exponent in a certain position.
         */
        private final double pathLossExponent;
        /**
         * The color of the characteristic.
         */
        private final Color color;

        /**
         * A constructor generating a characteristic with a given mean path loss, path loss exponent, reference distance
         * and shadow fading.
         * @param pathLossExponent  The path loss exponent to set.
         * @param color The color of the characteristic.
         */
        WeatherType(double pathLossExponent, Color color) {
            this.color = color;
            this.pathLossExponent = pathLossExponent;
        }


        /**
         *  Returns the path loss exponent of this position.
         * @return The path loss exponent of this position.
         */
        public double getPathLossExponent() {
            return pathLossExponent;
        }


        /**
         * Returns the color.
         * @return the color.
         */
        public Color getColor() {
            return color;
        }

    }

    public long getAmountOfColumns() {
        return amountOfColumns;
    }

    public long getAmountOfRows() {
        return amountOfRows;
    }
}
