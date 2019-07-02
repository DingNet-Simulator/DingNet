package IotDomain;


import SelfAdaptation.Instrumentation.MoteProbe;

import java.util.Arrays;
import java.util.LinkedList;

/**
 *  A class representing a gateway in the network.
 */
public class Gateway extends NetworkEntity {

    private LinkedList<MoteProbe> subscribedMoteProbes;
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
        super(gatewayEUI , xPos, yPos, environment, transmissionPower, SF, 1.0);
        environment.addGateway(this);
        subscribedMoteProbes = new LinkedList<>();
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
        getEnvironment().getMQTTServer().publish(new LinkedList<>(Arrays.asList(packet)), designatedReceiver, senderEUI,getEUI());
        for (MoteProbe moteProbe : getSubscribedMoteProbes()){
            moteProbe.trigger(this,senderEUI);
        }

    }
}
