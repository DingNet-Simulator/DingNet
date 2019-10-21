package IotDomain;

import SelfAdaptation.AdaptationGoals.AdaptationGoal;

import java.util.HashMap;
import java.util.Set;

/**
 * A class representing a requested Quality Of Service.
 */
public class QualityOfService {
    /**
     * Construct a Quality Of Service with given adaptationGoals
     * @param adaptationGoals the adaptation goals of the Quality Of Service.
     */

    public QualityOfService(HashMap<String,AdaptationGoal> adaptationGoals){
        this.adaptationGoals = adaptationGoals;
    }

    /**
     * The adaptation goals in the quality of service.
     */
    private HashMap<String,AdaptationGoal> adaptationGoals;

    /**
     * Returns the AdaptationGoal with the given name.
     * @param name The name of the AdaptationGoal.
     * @return The AdaptationGoal with the given name.
     */
    public AdaptationGoal getAdaptationGoal(String name) {
        return adaptationGoals.get(name);
    }

    public Set<String> getNames(){
        return adaptationGoals.keySet();
    }

    /**
     * Puts the AdaptationGoal with the given name in the HashMap.
     * @param name The name of the AdaptationGoal.
     * @param adaptationGoal The AdaptationGoal to put in the HashMap.
     */
    public void putAdaptationGoal(String name, AdaptationGoal adaptationGoal) {
        this.adaptationGoals.put(name,adaptationGoal);
    }
}
