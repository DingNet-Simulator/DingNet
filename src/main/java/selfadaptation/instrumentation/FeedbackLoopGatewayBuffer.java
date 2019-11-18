package selfadaptation.instrumentation;

import iot.lora.LoraTransmission;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import util.ListHelper;
import util.Pair;
import util.Statistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class FeedbackLoopGatewayBuffer {
    private HashMap<Mote,LinkedList<LinkedList<Pair<Gateway, LoraTransmission>>>> gatewayBuffer;

    public FeedbackLoopGatewayBuffer(){
        gatewayBuffer = new HashMap<>();
    }

    public void add(Mote mote, Gateway gateway){
        if(gatewayBuffer.containsKey(mote)){
            boolean contains = false;
            for (Pair<Gateway, LoraTransmission> pair : gatewayBuffer.get(mote).getLast()) {
                if (pair.getLeft() == gateway){
                    contains = true;
                    break;
                }
            }
            if (contains) {
                gatewayBuffer.get(mote).add(new LinkedList<>());
            }

            // FIXME this needs looking into, not sure how this is used in the actual simulation
            var transmissions = Statistics.getInstance().getAllReceivedTransmissions(gateway.getEUI(), gateway.getEnvironment().getNumberOfRuns() - 1);

            gatewayBuffer.get(mote).getLast().add(new Pair<>(gateway, ListHelper.getLast(transmissions)));
        } else {
            LinkedList<Pair<Gateway,LoraTransmission>> buffer = new LinkedList<>();
            var transmissions = Statistics.getInstance().getAllReceivedTransmissions(gateway.getEUI(), gateway.getEnvironment().getNumberOfRuns() - 1);

            buffer.add(new Pair<>(gateway, ListHelper.getLast(transmissions)));

            LinkedList<LinkedList<Pair<Gateway,LoraTransmission>>> buffers = new LinkedList<>();
            buffers.add(buffer);
            gatewayBuffer.put(mote,buffers);
        }
    }

    public boolean hasReceivedAllSignals(Mote mote){
        return gatewayBuffer.get(mote).size() > 1;
    }

    public LinkedList<LoraTransmission> getReceivedSignals(Mote mote){
        LinkedList<LoraTransmission> result = new LinkedList<>();

        if (hasReceivedAllSignals(mote)) {
            for (Pair<Gateway, LoraTransmission> pair : gatewayBuffer.get(mote).getFirst()){
                result.add(pair.getRight());
            }
            gatewayBuffer.get(mote).remove(0);
        }

        return result;
    }
}
