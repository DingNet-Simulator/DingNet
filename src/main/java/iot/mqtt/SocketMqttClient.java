package iot.mqtt;

import com.google.gson.*;
import iot.lora.BasicFrameHeader;
import iot.lora.EU868ParameterByDataRate;
import iot.lora.FrameHeader;
import iot.lora.RegionalParameter;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class SocketMqttClient implements MqttClientBasicApi {

    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader incoming;
    private PrintWriter outgoing;
    private Gson gson;

    private Map<String, List<MqttMessageConsumer>> subscribed = new HashMap<>();

    public SocketMqttClient(){
        gson = addAdapters(new GsonBuilder()).create();
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
                .setFCtrl(((JsonObject) jsonElement).get("fCtrl").getAsByte())
                .setFOpts(Base64.getDecoder().decode(((JsonObject) jsonElement).get("fOpts").getAsString()));
            return header;
        });

        builder.registerTypeAdapter(RegionalParameter.class,
            (JsonDeserializer<RegionalParameter>) (element, type, context) -> EU868ParameterByDataRate.valueOf(element.getAsString()));

        return builder;
    }
    @Override
    public void connect() {
        try {
            serverSocket = new ServerSocket(4032);
            socket = serverSocket.accept();
            incoming = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outgoing = new PrintWriter(socket.getOutputStream(), true);
            Thread sent = new Thread(() -> {

                    while(true){
                        try {
                            String in = incoming.readLine();
                            String message_topic = in.split("\\|")[0];
                            for (String topic : subscribed.keySet()){
                                boolean identical = false;
                                int i = 0;
                                String[] message_topic_array = message_topic.split("/");
                                String[] chosen_topic_array = topic.split("/");
                                while( i < message_topic_array.length  && i < chosen_topic_array.length ){
                                    if(chosen_topic_array[i].equals("+") || message_topic_array[i].equals(chosen_topic_array[i])){
                                        identical = true;
                                    }
                                    else {
                                        identical = false;
                                    }
                                    i ++;
                                }
                                if (identical) {
                                    subscribed.get(topic).forEach(c -> c.accept(message_topic, in.split("\\|")[1]));
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            });
            sent.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean publish(String messagetopic, MqttMessageType message) {
        outgoing.println(messagetopic + "|" +gson.toJson(message));
        for (String topic : subscribed.keySet()) {
            boolean identical = false;
            int i = 0;
            String[] message_topic_array = messagetopic.split("/");
            String[] chosen_topic_array = topic.split("/");
            while (i < message_topic_array.length && i < chosen_topic_array.length) {
                if (chosen_topic_array[i].equals("+") || message_topic_array[i].equals(chosen_topic_array[i])) {
                    identical = true;
                } else {
                    identical = false;
                }
                i++;
            }
            if (identical) {
                subscribed.get(topic).forEach(c -> c.accept(messagetopic,
                    gson.toJson(message)));
            }
        }
        return false;
    }

    @Override
    public <T extends MqttMessageType> void subscribe(Object subscriber, String topicFilter, Class<T> classMessage, BiConsumer<String, T> messageConsumer) {
        if (!subscribed.containsKey(topicFilter)) {
            subscribed.put(topicFilter, new LinkedList<>());
        }
        subscribed.get(topicFilter).add(new MqttMessageConsumer<T>(subscriber, messageConsumer, classMessage));


    }

    @Override
    public void unsubscribe(Object subscriber, String topicFilter) {

    }

    private class MqttMessageConsumer<T extends MqttMessageType> {

        private final Object subscriber;
        private final BiConsumer<String, T> consumer;
        private final Class<T> clazz;

        public MqttMessageConsumer(Object subscriber, BiConsumer<String, T> consumer, Class<T> clazz) {
            this.consumer = consumer;
            this.clazz = clazz;
            this.subscriber = subscriber;
        }

        public void accept(String t, String message) {
            consumer.accept(t, gson.fromJson(message, clazz));
        }

        public Object getSubscriber() {
            return subscriber;
        }
    }
}
