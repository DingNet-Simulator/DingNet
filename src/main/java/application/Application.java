package application;

import iot.mqtt.BasicMqttMessage;
import iot.mqtt.MqttClientBasicApi;
import iot.mqtt.MqttMessage;
import iot.mqtt.MqttMock;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Application {
    MqttClientBasicApi mqttClient;

    Application(List<String> topics) {
        this.mqttClient = new MqttMock();
        topics.forEach(t -> this.mqttClient.subscribe(t, this::consumePackets));
    }

    private void consumePackets(String topicFilter, MqttMessage message) {
        consumePackets(topicFilter, mqttClient.convertMessage(message, BasicMqttMessage.class));
    }

    public abstract void consumePackets(String topicFilter, BasicMqttMessage message);

    Map<MoteSensor, Byte[]> retrieveSensorData(Mote mote, List<Byte> messageBody) {
        Map<MoteSensor, Byte[]> sensorData = new HashMap<>();

        for (var sensor : mote.getSensors()) {
            int amtBytes = sensor.getAmountOfData();
            sensorData.put(sensor, messageBody.subList(0, amtBytes).toArray(Byte[]::new));
            messageBody = messageBody.subList(amtBytes, messageBody.size());
        }

        return sensorData;
    }

    public void destruct() {
        this.mqttClient.disconnect();
    }
}
