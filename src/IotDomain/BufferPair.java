package IotDomain;

/**
 * A class for represting pairs in the MQTT buffer.
 * @param <L>
 * @param <K>
 */
public class BufferPair<L,K> extends Pair<L,K>{
    /**
     * A constructor for creating a Pair with a given left and right value.
     *
     * @param left  The left value of the Pair.
     * @param right The right value of the Pair.
     */
    public BufferPair(L left, K right) { super( left, right);
    }

    @Override
    public int hashCode() { return getLeft().hashCode(); }

    /**
     * Determines if an object is equal to this BufferPair.
     * @param o The Object to check.
     * @return true if both are BufferPairs and their left value is equal.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BufferPair)) return false;
        BufferPair pairo = (BufferPair) o;
        return this.getLeft().equals(pairo.getLeft());
    }
}
