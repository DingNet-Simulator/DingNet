package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.networkcommunication.LoraWanPacket;
import IotDomain.networkentity.Mote;

@FunctionalInterface
public interface ConsumePacketStrategy {

    void consume(Mote mote, LoraWanPacket packet);
}
