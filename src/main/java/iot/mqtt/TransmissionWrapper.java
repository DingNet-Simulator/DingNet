package iot.mqtt;

import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;

public class TransmissionWrapper implements MqttMessage {

    private final LoraTransmission<LoraWanPacket> transmission;

    public TransmissionWrapper(LoraTransmission<LoraWanPacket> transmission) {
        this.transmission = transmission;
    }

    public LoraTransmission<LoraWanPacket> getTransmission() {
        return transmission;
    }
}
