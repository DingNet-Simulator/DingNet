package IotDomain.gatewayresponsestrategy;

import IotDomain.Gateway;
import IotDomain.lora.LoraWanPacket;

import java.util.Optional;

public interface ResponseStrategy {

    ResponseStrategy init(Gateway gateway);

    Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI);
}
