package datagenerator.rangedsensor.no2sensor;

import datagenerator.rangedsensor.abstractimpl.AbstractCell;
import datagenerator.rangedsensor.api.RangeSensor;

import java.beans.ConstructorProperties;

public class NO2Cell extends AbstractCell {

    @ConstructorProperties({"cellNumber", "fromTime", "level"})
    public NO2Cell(int cellNumber, double fromTime, String level) {
        super(cellNumber, fromTime, level);
    }

    @Override
    protected RangeSensor deserializeRangeSensor(String levelName) {
        return NO2Level.valueOf(levelName);
    }
}
