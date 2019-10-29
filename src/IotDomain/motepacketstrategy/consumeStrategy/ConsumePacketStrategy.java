package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.Mote;
import IotDomain.lora.LoraWanPacket;

import java.util.List;

@FunctionalInterface
public interface ConsumePacketStrategy {

    void consume(Mote mote, List<LoraWanPacket> packets);
}
