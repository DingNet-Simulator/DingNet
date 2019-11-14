package iot.mqtt;

import iot.lora.FrameHeader;
import util.Converter;

import java.util.List;

public class BasicMqttMessage implements MqttMessage {

    private final long deviceEUI;
    private final long applicationEUI;
    private final List<Byte> data;
    private final FrameHeader header;

    public BasicMqttMessage(FrameHeader header, List<Byte> packet, long deviceEUI, long applicationEUI) {
        this.deviceEUI = deviceEUI;
        this.applicationEUI = applicationEUI;
        data = packet;
        this.header = header;
    }

    public long getDeviceEUI() {
        return deviceEUI;
    }

    public long getApplicationEUI() {
        return applicationEUI;
    }

    public List<Byte> getData() {
        return data;
    }

    public byte[] getDataAsArray() {
        return Converter.toRowType(data.toArray(new Byte[0]));
    }

    public FrameHeader getHeader() {
        return header;
    }
}
