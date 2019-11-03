package IotDomain.mqtt;

import IotDomain.lora.FrameHeader;

import java.util.List;

public class MqttMessage {

    private final long deviceEUI;
    private final long gatewayEUI;
    private final long applicationEUI;
    private final List<Byte> data;
    private final FrameHeader header;

    public MqttMessage(FrameHeader header, List<Byte> packet, long deviceEUI, long gatewayEUI, long applicationEUI) {
        this.deviceEUI = deviceEUI;
        this.gatewayEUI = gatewayEUI;
        this.applicationEUI = applicationEUI;
        data = packet;
        this.header = header;
    }

    public long getDeviceEUI() {
        return deviceEUI;
    }

    public long getGatewayEUI() {
        return gatewayEUI;
    }

    public long getApplicationEUI() {
        return applicationEUI;
    }

    public List<Byte> getData() {
        return data;
    }

    public Byte[] getDataAsArray() {
        return data.toArray(new Byte[0]);
    }

    public FrameHeader getHeader() {
        return header;
    }
}
