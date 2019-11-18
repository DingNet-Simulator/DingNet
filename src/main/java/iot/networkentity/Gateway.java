package iot.networkentity;


import iot.Environment;
import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.mqtt.MQTTClientFactory;
import iot.mqtt.MqttClientBasicApi;
import iot.mqtt.Topics;
import iot.mqtt.TransmissionWrapper;
import iot.strategy.response.gateway.ResponseStrategy;
import iot.strategy.response.gateway.SendPacketImmediately;
import selfadaptation.instrumentation.MoteProbe;

import java.util.LinkedList;

/**
 *  A class representing a gateway in the network.
 */
public class Gateway extends NetworkEntity {

    private LinkedList<MoteProbe> subscribedMoteProbes;
    private final MqttClientBasicApi mqttClient;
    private final ResponseStrategy responseStrategy;

    /**
     * A construtor creating a gateway with a given xPos, yPos, environment and transmission power.
     * @param gatewayEUI        gateway identifier.
     * @param xPos              The x-coordinate of the gateway on the map.
     * @param yPos              The y-coordinate of the gateway on the map.
     * @param environment       The map of the environment.
     * @param transmissionPower The transmission power of the gateway.
     * @Effect creates a gateway with a given name, xPos, yPos, environment and transmission power.
     */
    public Gateway(long gatewayEUI, int xPos, int yPos, Environment environment, int transmissionPower, int SF) {
        this(gatewayEUI, xPos, yPos, environment, transmissionPower, SF, new SendPacketImmediately());
    }

    /**
     * A construtor creating a gateway with a given xPos, yPos, environment and transmission power.
     * @param gatewayEUI        gateway identifier.
     * @param xPos              The x-coordinate of the gateway on the map.
     * @param yPos              The y-coordinate of the gateway on the map.
     * @param environment       The map of the environment.
     * @param transmissionPower The transmission power of the gateway.
     * @param responseStrategy  strategy to enable response to mote
     * @Effect creates a gateway with a given name, xPos, yPos, environment and transmission power.
     */
    public Gateway(long gatewayEUI, int xPos, int yPos, Environment environment, int transmissionPower, int SF, ResponseStrategy responseStrategy) {
        super(gatewayEUI, xPos, yPos, environment, transmissionPower, SF, 1.0);
        subscribedMoteProbes = new LinkedList<>();
        mqttClient = MQTTClientFactory.getSingletonInstance();
        this.responseStrategy = responseStrategy.init(this);
    }

    /**
     * Returns the subscribed MoteProbes.
     * @return The subscribed MoteProbes.
     */
    public LinkedList<MoteProbe> getSubscribedMoteProbes() {
        return subscribedMoteProbes;
    }

    public void addSubscription(MoteProbe moteProbe) {
        if (!getSubscribedMoteProbes().contains(moteProbe)) {
            subscribedMoteProbes.add(moteProbe);
        }
    }

    /**
     * Sends a received transmission directly to the MQTT server.
     * @param transmission The received transmission.
     */
    @Override
    protected void OnReceive(LoraTransmission transmission) {
        var packet = transmission.getContent();
        //manage the message only if it is of a mote
        if (getEnvironment().getMotes().stream().anyMatch(m -> m.getEUI() == packet.getSenderEUI())) {
            var message = new TransmissionWrapper(transmission);
            mqttClient.publish(Topics.getGatewayToNetServer(packet.getReceiverEUI(), getEUI(), packet.getSenderEUI()), message);
            for (MoteProbe moteProbe : getSubscribedMoteProbes()) {
                moteProbe.trigger(this, packet.getSenderEUI());
            }
            responseStrategy.retrieveResponse(packet.getReceiverEUI(), packet.getSenderEUI()).ifPresent(this::send);
        }
    }

    public void sendToDevice(LoraWanPacket packet) { send(packet);}

    @Override
    boolean filterLoraSend(NetworkEntity networkEntity, LoraWanPacket packet) {
        return networkEntity.getEUI() == packet.getReceiverEUI();
    }

    @Override
    public void initialize() {}

    public MqttClientBasicApi getMqttClient() {
        return mqttClient;
    }
}
