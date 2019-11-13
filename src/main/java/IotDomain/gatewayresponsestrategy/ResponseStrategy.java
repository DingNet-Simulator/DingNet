package IotDomain.gatewayresponsestrategy;

import IotDomain.networkcommunication.LoraWanPacket;
import IotDomain.networkentity.Gateway;

import java.util.Optional;

public interface ResponseStrategy {

    ResponseStrategy init(Gateway gateway);

    Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI);
}
