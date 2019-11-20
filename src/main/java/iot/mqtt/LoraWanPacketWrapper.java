package iot.mqtt;

import iot.lora.LoraWanPacket;

public class LoraWanPacketWrapper implements MqttMessageType {

    private final LoraWanPacket packet;

    public LoraWanPacketWrapper(LoraWanPacket packet) {
        this.packet = packet;
    }

    public LoraWanPacket getPacket() {
        return packet;
    }
}
