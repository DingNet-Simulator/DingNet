package iot.strategy.consume;

import iot.lora.LoraWanPacket;
import iot.networkentity.Mote;

@FunctionalInterface
public interface ConsumePacketStrategy {

    void consume(Mote mote, LoraWanPacket packet);
}
