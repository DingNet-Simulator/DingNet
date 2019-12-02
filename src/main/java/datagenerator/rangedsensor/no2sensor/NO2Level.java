package datagenerator.rangedsensor.no2sensor;

import datagenerator.rangedsensor.api.RangeValue;

import java.nio.ByteBuffer;
import java.util.Random;

public enum NO2Level implements RangeValue {

    VERY_LOW(0, 50),
    LOW(50, 100),
    MEDIUM(100, 200),
    HIGH(200, 400),
    VERY_HIGH(400, 600);

    private final int lowerBound;
    private final int upperBound;
    private final Random random = new Random(1);

    NO2Level(int lowerBound, int upperBound) {
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
        var ret = new byte[2];
        ByteBuffer.wrap(ret).putShort((short) (getLowerBound() + random.nextInt(getUpperBound() - getLowerBound())));
        return ret;
    }
}
