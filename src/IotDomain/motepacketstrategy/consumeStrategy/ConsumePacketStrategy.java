package IotDomain.motepacketstrategy.consumeStrategy;

import IotDomain.LoraWanPacket;
import IotDomain.Mote;

import java.util.List;

@FunctionalInterface
public interface ConsumePacketStrategy {

    void consume(Mote mote, List<LoraWanPacket> packets);
}
