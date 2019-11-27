package datagenerator.rangedsensor.pm10sensor;

import datagenerator.rangedsensor.abstractimpl.AbstractCell;

import java.beans.ConstructorProperties;

public class PM10Cell extends AbstractCell {

    @ConstructorProperties({"cellNumber", "fromTime", "level"})
    public PM10Cell(int cellNumber, double fromTime, String level) {
        super(cellNumber, fromTime, PM10Level.valueOf(level));
    }


}
