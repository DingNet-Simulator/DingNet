package iot.mqtt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class PahoMqttClient implements MqttClientBasicApi{

    private MqttClient mqttClient;
    private final Gson gson;

    public PahoMqttClient() {
        this("tcp://test.mosquitto.org:1883", "testFenomeno");
    }

    public PahoMqttClient(@NotNull String address, @NotNull String clientId) {
        gson = new GsonBuilder().create();
        try {
            this.mqttClient = new MqttClient(address, clientId, new MemoryPersistence());
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connect() {
        var opt = new MqttConnectOptions();
        opt.setCleanSession(true);
        try {
            mqttClient.connect(opt);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(String topic, MqttMessage message) {
        var msg = new org.eclipse.paho.client.mqttv3.MqttMessage(gson.toJson(message).getBytes(US_ASCII));
        try {
            mqttClient.publish(topic, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <T extends MqttMessage> void subscribe(String topicFilter, Class<T> classMessage, BiConsumer<String, T> messageListener) {
        try {
            mqttClient.subscribe(topicFilter, (topic, msg) -> {
                T message = gson.fromJson(msg.toString(), classMessage);
                messageListener.accept(topic, message);
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unsubscribe(String topicFilter) {
        try {
            mqttClient.unsubscribe(topicFilter);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
