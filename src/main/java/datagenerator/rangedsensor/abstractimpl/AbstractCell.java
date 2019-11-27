package datagenerator.rangedsensor.abstractimpl;

import datagenerator.rangedsensor.api.Cell;
import datagenerator.rangedsensor.api.RangeSensor;

public abstract class AbstractCell implements Cell {
    private final int cellNumber;
    private final double fromTime;
    private final RangeSensor level;

    public AbstractCell(int cellNumber, double fromTime, RangeSensor level) {
        this.cellNumber = cellNumber;
        this.fromTime = fromTime;
        this.level = level;
    }

    @Override
    public int getCellNumber() {
        return cellNumber;
    }

    @Override
    public double getFromTime() {
        return fromTime;
    }

    @Override
    public RangeSensor getLevel() {
        return level;
    }
}
