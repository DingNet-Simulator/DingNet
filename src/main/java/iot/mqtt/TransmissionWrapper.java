package iot.mqtt;

import iot.lora.LoraTransmission;

public class TransmissionWrapper implements MqttMessageType {

    private final LoraTransmission transmission;

    public TransmissionWrapper(LoraTransmission transmission) {
        this.transmission = transmission;
    }

    public LoraTransmission getTransmission() {
        return transmission;
    }
}
