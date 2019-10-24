package IotDomain.mqtt;

import java.util.List;

public class MqttMessage {

    private final long deviceEUI;
    private final long gatewayEUI;
    private final long applicationEUI;
    private final List<Byte> data;

    public MqttMessage(List<Byte> packet, long deviceEUI, long gatewayEUI, long applicationEUI) {
        this.deviceEUI = deviceEUI;
        this.gatewayEUI = gatewayEUI;
        this.applicationEUI = applicationEUI;
        data = packet;
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
}
