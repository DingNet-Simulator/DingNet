package iot.mqtt;

public class MQTTClientFactory {

    private enum MqttClientType {PAHO, MOCK}
    private static MqttClientType DEFAULT_INSTANCE_TYPE = MqttClientType.MOCK;
    private static MqttClientBasicApi clientBasicApi;

    public static MqttClientBasicApi getSingletonInstance() {
        if (clientBasicApi == null) {
            switch (DEFAULT_INSTANCE_TYPE) {
                case PAHO: {
                    clientBasicApi = createPahoClient();
                } break;
                case MOCK: {
                    clientBasicApi = createMockClient();
                } break;
            }
        }
        return clientBasicApi;
    }

    public static MqttMock createMockClient() {
        return new MqttMock();
    }

    public static PahoMqttClient createPahoClient() {
        return new PahoMqttClient();
    }
}
