package datagenerator.rangedsensor.api;

public interface RangeSensor {
    int getLowerBound();

    int getUpperBound();

    //TODO
    default byte[] getValue() {
        return new byte[] {0};
    }
}
