package SelfAdaptation.Instrumentation;

import IotDomain.Gateway;
import IotDomain.LoraTransmission;
import IotDomain.Mote;
import IotDomain.Pair;

import java.util.HashMap;
import java.util.LinkedList;

public class FeedbackLoopGatewayBuffer {
    private HashMap<Mote,LinkedList<LinkedList<Pair<Gateway, LoraTransmission>>>> gatewayBuffer;

    public FeedbackLoopGatewayBuffer(){
        gatewayBuffer = new HashMap<>();
    }

    public void add(Mote mote, Gateway gateway){
        if(gatewayBuffer.keySet().contains(mote)){
            Boolean contains = false;
            for(Pair<Gateway, LoraTransmission> pair : gatewayBuffer.get(mote).getLast()){
                if (pair.getLeft() == gateway){
                    contains = true;
                }
            }
            if(contains){
                gatewayBuffer.get(mote).add(new LinkedList<>());
            }
            gatewayBuffer.get(mote).getLast().add(new Pair<>(gateway, gateway.getReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).getLast()));
        }
        else {
            LinkedList<Pair<Gateway,LoraTransmission>> buffer = new LinkedList<>();
            buffer.add(new Pair<>(gateway, gateway.getReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).getLast()));
            LinkedList<LinkedList<Pair<Gateway,LoraTransmission>>> buffers = new LinkedList<>();
            buffers.add(buffer);
            gatewayBuffer.put(mote,buffers);
        }
    }

    public boolean hasReceivedAllSignals(Mote mote){
        if(gatewayBuffer.get(mote).size()>1){
            return true;
        }
        else{
            return false;
        }
    }

    public LinkedList<LoraTransmission> getReceivedSignals(Mote mote){
        LinkedList<LoraTransmission> result = new LinkedList();
        if(hasReceivedAllSignals(mote)){

            for(Pair<Gateway, LoraTransmission> pair : gatewayBuffer.get(mote).getFirst()){
                result.add(pair.getRight());
            }
            gatewayBuffer.get(mote).remove(0);
        }
        return result;
    }
}
