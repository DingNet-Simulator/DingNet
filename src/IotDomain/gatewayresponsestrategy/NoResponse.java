package IotDomain.gatewayresponsestrategy;

import IotDomain.Gateway;
import IotDomain.LoraWanPacket;

import java.util.Optional;

public class NoResponse implements ResponseStrategy {
    @Override
    public ResponseStrategy init(Gateway gateway) {
        return this;
    }

    @Override
    public Optional<LoraWanPacket> retrieveResponse(Long applicationEUI, Long deviceEUI) {
        return Optional.empty();
    }
}
