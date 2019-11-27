package datagenerator.rangedsensor.api;

public interface RangeSensor {
    int getLowerBound();

    int getUpperBound();

    byte[] getValue();
}
