package iot;

import util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CharacteristicsMap {

    List<List<Characteristic>> characteristics;
    int maxXPos;
    int maxYPos;
    double zoneWidth;
    double zoneHeight;


    public CharacteristicsMap(int maxXPos, int maxYPos, long numberOfColumns, long numberOfRows,
                              Characteristic defaultCharacteristic){
        this.maxXPos = maxXPos;
        this.maxYPos = maxYPos;
        this.zoneWidth = maxXPos / numberOfColumns;
        this.zoneHeight = maxYPos / numberOfRows;
        characteristics = new ArrayList<>();
        for(int i = 0; i <= numberOfColumns;i++){
            characteristics.add(new ArrayList<>());
            for(int j = 0; j <= numberOfRows;j++) {
                characteristics.get(i).add(defaultCharacteristic);
            }
        }




    }

    public void setCharacterstics(Characteristic characteristic, int zoneColumn, int zoneRow){
        characteristics.get(zoneColumn).remove(zoneRow);
        characteristics.get(zoneColumn).add(zoneRow,characteristic);
    }

    public Characteristic getCharacteristic(double x, double y){
        int columnNumber = (int) Math.floor(x / zoneWidth);
        int rowNumber = (int) Math.floor(y / zoneHeight);
        return characteristics.get(columnNumber).get(rowNumber);
    }

    public int getMaxXPos() {
        return maxXPos;
    }

    public int getMaxYPos() {
        return maxYPos;
    }

}
