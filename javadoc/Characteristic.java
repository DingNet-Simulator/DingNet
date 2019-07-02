import be.kuleuven.cs.som.annotate.*;

/**
 * A class representing the characteristics of a certain location.
 */
public class Characteristic {
    /**
     * A integer representing the mean path loss over a certain reference distance in a certain position.
     */
    private final Integer meanPathLoss;
    /**
     * A integer representing the path loss exponent in a certain position.
     */
    private final Integer pathLossExponent;
    /**
     * A integer representing the reference distance for the mean path loss in a certain position.
     */
    private final Integer referenceDistance;

    /**
     * A constructor generating a characteristic with a given mean path loss, path loss exponent and reference distance.
     * @param meanPathLoss  The mean path loss to set.
     * @param pathLossExponent  The path loss exponent to set.
     * @param referenceDistance The reference distance to set.
     */
    public Characteristic(Integer meanPathLoss, Integer pathLossExponent, Integer referenceDistance) {
        this.meanPathLoss = meanPathLoss;
        this.pathLossExponent = pathLossExponent;
        this.referenceDistance = referenceDistance;
    }

    /**
     *
     * @return
     */
    @Basic
    public Integer getMeanPathLoss() {
        return meanPathLoss;
    }

    public Integer getPathLossExponent() {
        return pathLossExponent;
    }

    public Integer getReferenceDistance() {
        return referenceDistance;
    }
}
