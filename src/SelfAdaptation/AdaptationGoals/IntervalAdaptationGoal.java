package SelfAdaptation.AdaptationGoals;

import be.kuleuven.cs.som.annotate.Basic;
import be.kuleuven.cs.som.annotate.Model;

/**
 * An adaptation goal with a lower and upper value.
 */
public class IntervalAdaptationGoal extends AdaptationGoal {

    /**
     * A double representing the lower value of the goal
     */
    @Model
    private final Double lowerBoundary;

    /**
     * A double representing the upper value of the goal
     */
    @Model
    private final Double upperBoundary;

    /**
     * Constructs a IntervalAdaptationGoal with a given lower and upper value.
     * @param lowerBoundary The lower boundary of the goal.
     * @param upperBoundary The upper boundary of the goal.
     */
    public IntervalAdaptationGoal( Double lowerBoundary, Double upperBoundary){
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
    }

    /**
     * Returns the lower value of the goal.
     * @return The lower value of the goal.
     */
    @Basic
    public Double getLowerBoundary() {
        return lowerBoundary;
    }

    /**
     * Returns the upper value of the goal.
     * @return The upper value of the goal.
     */
    @Basic
    public Double getUpperBoundary() {
        return upperBoundary;
    }
}
