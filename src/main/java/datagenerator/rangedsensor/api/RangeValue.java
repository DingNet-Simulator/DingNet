package datagenerator.rangedsensor.api;

/**
 * Validity range of the value of a {@link Cell}
 */
public interface RangeValue {

    /**
     *
     * @return lower bound
     */
    int getLowerBound();

    /**
     *
     * @return upper bound
     */
    int getUpperBound();

    /**
     *
     * @return a value in the range converted in byte
     */
    byte[] getValue();
}
