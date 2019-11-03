package SelfAdaptation.AdaptationGoals;

/**
 * An adaptation goal with a threshold.
 */
public class ThresholdAdaptationGoal extends AdaptationGoal {
    /**
     * The threshold of the goal.
     */
    private final Double threshold;

    /**
     * A constructor generating a ThresholdAdaptationGoal with given threshold.
     * @param threshold
     */
    public ThresholdAdaptationGoal(Double threshold){
        this.threshold = threshold;
    }

    /**
     * Returns the threshhold.
     * @return
     */
    public Double getThreshold() {
        return threshold;
    }
}
