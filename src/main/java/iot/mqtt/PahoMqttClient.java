package iot.mqtt;

import com.google.gson.*;
import iot.lora.BasicFrameHeader;
import iot.lora.EU868ParameterByDataRate;
import iot.lora.FrameHeader;
import iot.lora.RegionalParameter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class PahoMqttClient implements MqttClientBasicApi{

    private MqttClient mqttClient;
    private Gson gson;

    public PahoMqttClient() {
        this("tcp://mqtt.eclipse.org:1883", "testFenomeno1995");
    }

    public PahoMqttClient(@NotNull String address, @NotNull String clientId) {
        gson = addAdapters(new GsonBuilder()).create();
        try {
            mqttClient = new MqttClient(address, clientId, new MemoryPersistence());
            connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private GsonBuilder addAdapters(GsonBuilder builder) {

        builder.registerTypeAdapter(FrameHeader.class, (JsonSerializer<FrameHeader>) (header, type, context) -> {
            var obj = new JsonObject();
            obj.addProperty("sourceAddress", Base64.getEncoder().encodeToString(header.getSourceAddress()));
            obj.addProperty("fCtrl", header.getFCtrl());
            obj.addProperty("fCnt", header.getFCntAsShort());
            obj.addProperty("fOpts", Base64.getEncoder().encodeToString(header.getFOpts()));
            return obj;
        });

        builder.registerTypeAdapter(FrameHeader.class, (JsonDeserializer<FrameHeader>) (jsonElement, type, jsonDeserializationContext) -> {
            var header = new BasicFrameHeader();
            header
                .setSourceAddress(Base64.getDecoder().decode(((JsonObject) jsonElement).get("sourceAddress").getAsString()))
                .setFCnt(((JsonObject) jsonElement).get("fCnt").getAsShort())
                .setfCtrl(((JsonObject) jsonElement).get("fCtrl").getAsByte())
                .setFOpts(Base64.getDecoder().decode(((JsonObject) jsonElement).get("fOpts").getAsString()));
            return header;
        });

        builder.registerTypeAdapter(RegionalParameter.class,
            (JsonDeserializer<RegionalParameter>) (element, type, context) -> EU868ParameterByDataRate.valueOf(element.getAsString()));

        return builder;
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
            if(!mqttClient.isConnected()) {
                connect();
            }
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
