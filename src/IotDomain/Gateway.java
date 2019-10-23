package IotDomain;


import IotDomain.gatewayresponsestrategy.NoResponse;
import IotDomain.gatewayresponsestrategy.ResponseStrategy;
import IotDomain.mqtt.MqttClientBasicApi;
import IotDomain.mqtt.MqttMessage;
import IotDomain.mqtt.MqttMock;
import SelfAdaptation.Instrumentation.MoteProbe;

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
     * @param gatewayEUI gateway identifier.
     * @param xPos  The x-coordinate of the gateway on the map.
     * @param yPos  The y-coordinate of the gateway on the map.
     * @param environment   The map of the environment.
     * @param transmissionPower   The transmission power of the gateway.
     * @Effect creates a gateway with a given name, xPos, yPos, environment and transmission power.
     */
    public Gateway(Long gatewayEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower, Integer SF) {
        this(new NoResponse(), gatewayEUI, xPos, yPos, environment, transmissionPower, SF);
    }
    /**
     * A construtor creating a gateway with a given xPos, yPos, environment and transmission power.
     * @param responseStrategy strategy to enable response to mote
     * @param gatewayEUI gateway identifier.
     * @param xPos  The x-coordinate of the gateway on the map.
     * @param yPos  The y-coordinate of the gateway on the map.
     * @param environment   The map of the environment.
     * @param transmissionPower   The transmission power of the gateway.
     * @Effect creates a gateway with a given name, xPos, yPos, environment and transmission power.
     */
    public Gateway(ResponseStrategy responseStrategy, Long gatewayEUI, Integer xPos, Integer yPos, Environment environment, Integer transmissionPower, Integer SF) {
        super(gatewayEUI , xPos, yPos, environment, transmissionPower, SF, 1.0);
        environment.addGateway(this);
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
        if(!getSubscribedMoteProbes().contains(moteProbe)) {
            subscribedMoteProbes.add(moteProbe);
        }
    }

    /**
     * Sends a received packet directly to the MQTT server.
     * @param packet The received packet.
     * @param senderEUI The EUI of the sender
     * @param designatedReceiver The EUI designated receiver for the packet.
     */
    @Override
    protected void OnReceive(Byte[] packet, Long senderEUI, Long designatedReceiver) {
        //manage the message only if it is of a mote
        if (getEnvironment().getMotes().stream().anyMatch(m -> m.getEUI().equals(senderEUI))) {
            var message = new MqttMessage(new LinkedList<>(Arrays.asList(packet)), designatedReceiver, senderEUI, getEUI());
            mqttClient.publish(getTopic(designatedReceiver, senderEUI), message);
            for (MoteProbe moteProbe : getSubscribedMoteProbes()) {
                moteProbe.trigger(this, senderEUI);
            }
            responseStrategy.retrieveResponse(designatedReceiver, senderEUI).ifPresent(this::loraSend);
        }
    }

    private String getTopic(Long applicationEUI, Long deviceEUI) {
        return new StringBuilder()
            .append("application/")
            .append(applicationEUI)
            .append("/node/")
            .append(deviceEUI)
            .append("/rx")
            .toString();
    }
}
