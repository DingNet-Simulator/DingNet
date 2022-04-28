package iot.networkentity;

import iot.lora.BasicFrameHeader;
import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.mqtt.*;

import java.util.*;
import java.util.function.BinaryOperator;

public class NetworkServer {

    // Map moteId -> (Map gatewayId -> lastTransmission)
    private final Map<Long, Map<Long, LoraTransmission>> transmissionReceived;
    private final Map<Long, History<LoraTransmission>> historyMote;
    private final MqttClientBasicApi mqttClient;
    private BinaryOperator<Map.Entry<Long, LoraTransmission>> chooseGatewayStrategy = this::chooseByTransmissionPower;
    private short frameCounter;

    public NetworkServer(MqttClientBasicApi mqttClient) {
        this.mqttClient = mqttClient;
        transmissionReceived = new HashMap<>();
        historyMote = new HashMap<>();
        subscribeToGateways();
        subscribeToApps();
        frameCounter = 0;
    }

    public void reset() {
        transmissionReceived.clear();
        historyMote.clear();
        frameCounter = 0;
    }

    /**
     * Setter to set the strategy to use to choose the {@link Gateway} to send a packet to a {@link Mote}
     * @param strategy
     * @return
     */
    public NetworkServer setChooseGatewayStrategy(BinaryOperator<Map.Entry<Long, LoraTransmission>> strategy) {
        chooseGatewayStrategy = strategy;
        return this;
    }

    private void subscribeToGateways() {
        mqttClient.subscribe(this, Topics.getGatewayToNetServer("+", "+", "+"),
            TransmissionWrapper.class,
            (topic, msg) -> {
                var transmission = msg.getTransmission();
                //get mote id
                var moteId = Topics.getMote(topic);
                //get gateway id
                var gatewayId = Topics.getGateway(topic);
                //add trans to map with all check
                if (!transmissionReceived.containsKey(moteId)) {
                    transmissionReceived.put(moteId, new HashMap<>());
                    historyMote.put(moteId, new History<>(200));
                }
                transmissionReceived.get(moteId).put(gatewayId, transmission);
                //check if packet is duplicated (is not send to app)
                if (historyMote.get(moteId).getList().stream().noneMatch(t -> t.equals(transmission))) {
                    mqttClient.publish(Topics.getNetServerToApp(transmission.getContent().getReceiverEUI(), moteId), msg);
                }
                historyMote.get(moteId).add(transmission);
            });
    }

    private void subscribeToApps() {
        mqttClient.subscribe(this, Topics.getAppToNetServer("+", "+"),
            BasicMqttMessage.class,
            (topic, msg) -> {
                //get mote id
                var moteId = Topics.getMote(topic);
                //find best gateway
                if (transmissionReceived.containsKey(moteId)) {
                    var gatewayId = transmissionReceived.get(moteId)
                        .entrySet()
                        .stream()
                        .reduce((e1, e2) -> chooseGatewayStrategy.apply(e1, e2))
                        .map(Map.Entry::getKey)
                        .orElseThrow(() -> new IllegalStateException("no gateway available for the mote: " + moteId));
                    //send to best gateway
                    var packet = new LoraWanPacket(gatewayId, moteId, msg.getDataAsArray(),
                        new BasicFrameHeader().setFCnt(incrementFrameCounter()), msg.getMacCommands());
                    mqttClient.publish(Topics.getNetServerToGateway(Topics.getApp(topic), gatewayId, moteId),
                        new LoraWanPacketWrapper(packet));
                }
            });
    }

    private short incrementFrameCounter() {
        return frameCounter++;
    }

    private Map.Entry<Long, LoraTransmission> chooseByTransmissionPower(
            Map.Entry<Long, LoraTransmission> e1,
            Map.Entry<Long, LoraTransmission> e2) {
        return e1.getValue().getTransmissionPower() >= e2.getValue().getTransmissionPower() ? e1 : e2;
    }

    public void reconnect() {
        this.mqttClient.disconnect();
        this.mqttClient.connect();

        subscribeToGateways();
        subscribeToApps();
    }

    private class History<T>{
        private int size;
        private int currentIndex;
        private ArrayList<T> list;
        boolean firstIteration;

        public History(int size){
            this.size = size;
            this.currentIndex = 0;
            this.list = new ArrayList<>(size);
            this.firstIteration = true;
        }
        public void add(T elem){
            if(firstIteration){
                list.add(elem);
            }else {
                list.add(currentIndex,elem);
            }
            currentIndex ++;
            if(currentIndex == size){
                currentIndex = 0;
                if(firstIteration){
                    firstIteration = false;
                }
            }
        }

        public List<T> getList(){
            return list;
        }
    }
}
