package datagenerator.rangedsensor.api;

public interface Cell {
    int getCellNumber();

    double getFromTime();

    RangeValue getLevel();
}
