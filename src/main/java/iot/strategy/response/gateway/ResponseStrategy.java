package iot.strategy.response.gateway;


import iot.networkcommunication.LoraWanPacket;
import iot.networkentity.Gateway;

import java.util.Optional;

public interface ResponseStrategy {

    ResponseStrategy init(Gateway gateway);

    Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI);
}
