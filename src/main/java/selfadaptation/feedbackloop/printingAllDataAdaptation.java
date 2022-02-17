package selfadaptation.feedbackloop;

import iot.networkentity.Gateway;
import iot.networkentity.Mote;


import org.jfree.data.json.impl.JSONObject;

public class printingAllDataAdaptation extends GenericFeedbackLoop{
    private final JSONObject outputfile;

    /**
     * A constructor generating an adaptationApproach.
     */
    public printingAllDataAdaptation(JSONObject  outputfile) {
        super("printingData");
        this.outputfile = outputfile;
    }

    @Override
    public void adapt(Mote mote, Gateway dataGateway) {
        JSONObject currentObject = new JSONObject();
        currentObject.put(mote.getEUI(),mote.getEnergyLevel());
        outputfile.put("15",currentObject);

    }
}
