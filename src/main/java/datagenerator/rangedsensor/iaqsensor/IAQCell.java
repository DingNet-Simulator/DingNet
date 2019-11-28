package datagenerator.rangedsensor.iaqsensor;

import datagenerator.rangedsensor.abstractimpl.AbstractCell;
import datagenerator.rangedsensor.api.RangeValue;

import java.beans.ConstructorProperties;

public class IAQCell extends AbstractCell {

    @ConstructorProperties({"cellNumber", "fromTime", "level"})
    public IAQCell(int cellNumber, double fromTime, String level) {
        super(cellNumber, fromTime, level);
    }

    @Override
    protected RangeValue deserializeRangeSensor(String levelName) {
        return AirQualityLevel.valueOf(levelName);
    }
}
