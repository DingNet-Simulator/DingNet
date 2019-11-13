package iot.strategy.consume;

import iot.networkcommunication.LoraWanPacket;
import iot.networkentity.Mote;

@FunctionalInterface
public interface ConsumePacketStrategy {

    void consume(Mote mote, LoraWanPacket packet);
}
