package datagenerator.rangedsensor.iaqsensor;

import datagenerator.rangedsensor.api.RangeSensor;

public enum AirQualityLevel implements RangeSensor {
    // TODO maybe add 0 as well for perfect air quality
    GOOD(1, 0, 25),
    FAIR(2, 25, 50),
    MODERATE(3, 50, 75),
    POOR(4, 75, 100),
    VERY_POOR(5, 100, 125);

    private final byte cod;
    private final int lowerBound;
    private final int upperBound;

    AirQualityLevel(int cod, int lowerBound, int upperBound) {
        this.cod = (byte) cod;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public byte getCod() {
        return cod;
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
        return new byte[] {getCod()};
    }
}
