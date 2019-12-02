package datagenerator.rangedsensor.abstractimpl;

import datagenerator.rangedsensor.api.Cell;
import datagenerator.rangedsensor.api.RangeValue;

public abstract class AbstractCell implements Cell {
    private final int cellNumber;
    private final double fromTime;
    private final RangeValue level;

    public AbstractCell(int cellNumber, double fromTime, String levelName) {
        this.cellNumber = cellNumber;
        this.fromTime = fromTime;
        this.level = deserializeRangeSensor(levelName);
    }

    protected abstract RangeValue deserializeRangeSensor(String levelName);

    @Override
    public int getCellNumber() {
        return cellNumber;
    }

    @Override
    public double getFromTime() {
        return fromTime;
    }

    @Override
    public RangeValue getLevel() {
        return level;
    }
}
