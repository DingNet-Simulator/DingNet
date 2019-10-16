package IotDomain.mqtt;

import java.util.LinkedList;
import java.util.List;

public class MqttMessage {

    private final long deviceEUI;
    private final long gatewayEUI;
    private final long applicationEUI;
    private final double latitude;
    private final double longitude;
    private final List<Integer> sensedData;

    public MqttMessage(List<Byte> packet, long deviceEUI, long gatewayEUI, long applicationEUI) {
        this.deviceEUI = deviceEUI;
        this.gatewayEUI = gatewayEUI;
        this.applicationEUI = applicationEUI;
        //TODO decode packet
        latitude = 0;
        longitude = 0;
        sensedData = new LinkedList<>();
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<Integer> getSensedData() {
        return sensedData;
    }
}
