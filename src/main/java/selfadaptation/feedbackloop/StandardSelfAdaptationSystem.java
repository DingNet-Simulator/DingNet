package selfadaptation.feedbackloop;

import iot.networkentity.Gateway;
import iot.networkentity.Mote;

public class StandardSelfAdaptationSystem extends GenericFeedbackLoop{

    /**
     * A constructor generating an adaptationApproach.
     *
     */
    public StandardSelfAdaptationSystem() {
        super("Standard");

    }

    @Override
    public void adapt(Mote mote, Gateway dataGateway) {

    }
}
