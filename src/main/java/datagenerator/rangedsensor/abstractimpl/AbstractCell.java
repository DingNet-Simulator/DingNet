package datagenerator.rangedsensor.abstractimpl;

import datagenerator.rangedsensor.api.Cell;
import datagenerator.rangedsensor.api.RangeSensor;

public abstract class AbstractCell implements Cell {
    private final int cellNumber;
    private final double fromTime;
    private final RangeSensor level;

    public AbstractCell(int cellNumber, double fromTime, String levelName) {
        this.cellNumber = cellNumber;
        this.fromTime = fromTime;
        this.level = deserializeRangeSensor(levelName);
    }

    protected abstract RangeSensor deserializeRangeSensor(String levelName);

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
