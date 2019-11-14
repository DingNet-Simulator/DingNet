package iot.mqtt;

import iot.lora.FrameHeader;
import iot.lora.MacCommand;
import util.Converter;

import java.util.LinkedList;
import java.util.List;

public class BasicMqttMessage implements MqttMessage {

    private final FrameHeader header;
    private final List<Byte> data;
    private final long deviceEUI;
    private final long applicationEUI;
    private List<MacCommand> macCommands;

    public BasicMqttMessage(FrameHeader header, List<Byte> data, long deviceEUI, long applicationEUI) {
        this(header, data, deviceEUI, applicationEUI, new LinkedList<>());
    }

    public BasicMqttMessage(FrameHeader header, List<Byte> data, long deviceEUI, long applicationEUI, List<MacCommand> macCommands) {
        this.header = header;
        this.data = data;
        this.deviceEUI = deviceEUI;
        this.applicationEUI = applicationEUI;
        this.macCommands = macCommands;
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

    public List<MacCommand> getMacCommands() {
        return macCommands;
    }

    public BasicMqttMessage setMacCommands(List<MacCommand> macCommands) {
        this.macCommands = macCommands;
        return this;
    }
}
