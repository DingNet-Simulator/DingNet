package IotDomain.application;

import IotDomain.mqtt.MqttClientBasicApi;
import IotDomain.mqtt.MqttMessage;
import IotDomain.mqtt.MqttMock;
import IotDomain.networkentity.Mote;
import IotDomain.networkentity.MoteSensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Application {
    protected MqttClientBasicApi mqttClient;

    Application(List<String> topics) {
        this.mqttClient = new MqttMock();
        this.mqttClient.connect();
        topics.forEach(t -> this.mqttClient.subscribe(t, this::consumePackets));
    }

    public abstract void consumePackets(String topicFilter, MqttMessage message);

    Map<MoteSensor, Byte[]> retrieveSensorData(Mote mote, List<Byte> messageBody) {
        Map<MoteSensor, Byte[]> sensorData = new HashMap<>();

        for (var sensor : mote.getSensors()) {
            int amtBytes = sensor.getAmountOfData();
            sensorData.put(sensor, messageBody.subList(0, amtBytes).toArray(Byte[]::new));
            messageBody = messageBody.subList(amtBytes, messageBody.size());
        }

        return sensorData;
    }
}
