package datagenerator.rangedsensor.api;

/**
 * Configuration of a cell of the matrix in which the
 * environment is split with the starting time of validity
 */
public interface Cell {

    /**
     *
     * @return the number of the cell in the matrix
     */
    int getCellNumber();

    /**
     *
     * @return starting time of validity
     */
    double getFromTime();

    /**
     *
     * @return validity range of the value
     */
    RangeValue getLevel();
}
