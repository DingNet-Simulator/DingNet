package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.lora.LoraWanPacket;
import IotDomain.networkentity.Mote;

import java.util.List;

@FunctionalInterface
public interface ConsumePacketStrategy {

    void consume(Mote mote, List<LoraWanPacket> packets);
}
