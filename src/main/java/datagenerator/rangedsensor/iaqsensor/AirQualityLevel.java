package datagenerator.rangedsensor.iaqsensor;

import datagenerator.rangedsensor.api.RangeValue;

import java.util.Arrays;
import java.util.Random;

public enum AirQualityLevel implements RangeValue {

    GOOD(1, 0, 25),
    FAIR(2, 25, 50),
    MODERATE(3, 50, 75),
    POOR(4, 75, 100),
    VERY_POOR(5, 100, 125);

    private final byte cod;
    private final int lowerBound;
    private final int upperBound;
    private final Random random = new Random(1);

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
        return new byte[] { (byte) (getLowerBound() + random.nextInt(getUpperBound()-getLowerBound()))};
    }

    public static RangeValue getByValue(int value) {
        return Arrays.stream(values())
            .filter(v -> v.getLowerBound() <= value)
            .filter(v -> value < v.getUpperBound())
            .findFirst()
            .orElseThrow();
    }

    public static byte getCodByValue(int value) {
        return ((AirQualityLevel) getByValue(value)).getCod();
    }
}
