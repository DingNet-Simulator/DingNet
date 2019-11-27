package datagenerator.rangedsensor.pm10sensor;

import datagenerator.rangedsensor.abstractimpl.AbstractCell;
import datagenerator.rangedsensor.api.RangeSensor;

import java.beans.ConstructorProperties;

public class PM10Cell extends AbstractCell {

    @ConstructorProperties({"cellNumber", "fromTime", "level"})
    public PM10Cell(int cellNumber, double fromTime, String level) {
        super(cellNumber, fromTime, level);
    }

    @Override
    protected RangeSensor deserializeRangeSensor(String levelName) {
        return PM10Level.valueOf(levelName);
    }
}
