package datagenerator.rangedsensor.pm10sensor;

import datagenerator.rangedsensor.api.RangeSensor;

public enum PM10Level implements RangeSensor {

    VERY_LOW(0, 25),
    LOW(25, 50),
    MEDIUM(50, 90),
    HIGH(90, 180),
    VERY_HIGH(180, 255);

    private final int lowerBound;
    private final int upperBound;

    PM10Level(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public int getLowerBound() {
        return lowerBound;
    }

    @Override
    public int getUpperBound() {
        return upperBound;
    }
}
