package datagenerator.rangedsensor.iaqsensor;

import datagenerator.rangedsensor.api.RangeValue;

import java.util.Random;

public enum AirQualityLevel implements RangeValue {

    GOOD(0, 25),
    FAIR(25, 50),
    MODERATE(50, 75),
    POOR(75, 100),
    VERY_POOR(100, 125);

    private final int lowerBound;
    private final int upperBound;
    private final Random random = new Random(1);

    AirQualityLevel(int lowerBound, int upperBound) {
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

    @Override
    public byte[] getValue() {
        return new byte[] { (byte) (getLowerBound() + random.nextInt(getUpperBound() - getLowerBound()))};
    }
}
