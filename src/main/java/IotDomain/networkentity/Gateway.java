package IotDomain.networkentity;


import IotDomain.Environment;
import IotDomain.gatewayresponsestrategy.ResponseStrategy;
import IotDomain.gatewayresponsestrategy.SendNewestPacket;
import IotDomain.mqtt.MqttClientBasicApi;
import IotDomain.mqtt.MqttMessage;
import IotDomain.mqtt.MqttMock;
import IotDomain.networkcommunication.LoraWanPacket;
import SelfAdaptation.Instrumentation.MoteProbe;
import util.Converter;

import java.util.Arrays;
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
        this(new SendNewestPacket(), gatewayEUI, xPos, yPos, environment, transmissionPower, SF);
    }

    /**
     * A construtor creating a gateway with a given xPos, yPos, environment and transmission power.
     * @param responseStrategy  strategy to enable response to mote
     * @param gatewayEUI        gateway identifier.
     * @param xPos              The x-coordinate of the gateway on the map.
     * @param yPos              The y-coordinate of the gateway on the map.
     * @param environment       The map of the environment.
     * @param transmissionPower The transmission power of the gateway.
     * @Effect creates a gateway with a given name, xPos, yPos, environment and transmission power.
     */
    public Gateway(ResponseStrategy responseStrategy, long gatewayEUI, int xPos, int yPos, Environment environment, int transmissionPower, int SF) {
        super(gatewayEUI, xPos, yPos, environment, transmissionPower, SF, 1.0);
        subscribedMoteProbes = new LinkedList<>();
        mqttClient = new MqttMock();
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
     * Sends a received packet directly to the MQTT server.
     * @param packet The received packet.
     */
    @Override
    protected void OnReceive(LoraWanPacket packet) {
        //manage the message only if it is of a mote
        if (getEnvironment().getMotes().stream().anyMatch(m -> m.getEUI() == packet.getSenderEUI())) {
            var message = new MqttMessage(packet.getFrameHeader(), new LinkedList<>(Arrays.asList(Converter.toObjectType(packet.getPayload()))), packet.getSenderEUI(), getEUI(), packet.getReceiverEUI());
            mqttClient.publish(getTopic(packet.getReceiverEUI(), packet.getSenderEUI()), message);
            for (MoteProbe moteProbe : getSubscribedMoteProbes()) {
                moteProbe.trigger(this, packet.getSenderEUI());
            }
            responseStrategy.retrieveResponse(packet.getReceiverEUI(), packet.getSenderEUI()).ifPresent(this::send);
        }
    }

    @Override
    boolean filterLoraSend(NetworkEntity networkEntity, LoraWanPacket packet) {
        return networkEntity.getEUI() == packet.getReceiverEUI();
    }

    @Override
    public void initialize() {}

    private String getTopic(Long applicationEUI, Long deviceEUI) {
        return new StringBuilder()
            .append("application/")
            .append(applicationEUI)
            .append("/node/")
            .append(deviceEUI)
            .append("/rx")
            .toString();
    }

    public MqttClientBasicApi getMqttClient() {
        return mqttClient;
    }
}
