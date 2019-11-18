package application;

import iot.mqtt.MQTTClientFactory;
import iot.mqtt.MqttClientBasicApi;
import iot.mqtt.TransmissionWrapper;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Application {
    MqttClientBasicApi mqttClient;

    Application(List<String> topics) {
        this.mqttClient = MQTTClientFactory.getSingletonInstance();
        topics.forEach(t -> this.mqttClient.subscribe(t, TransmissionWrapper.class, this::consumePackets));
    }

    public abstract void consumePackets(String topicFilter, TransmissionWrapper message);

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
