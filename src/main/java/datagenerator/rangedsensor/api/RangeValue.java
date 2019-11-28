package datagenerator.rangedsensor.api;

public interface RangeValue {
    int getLowerBound();

    int getUpperBound();

    byte[] getValue();
}
