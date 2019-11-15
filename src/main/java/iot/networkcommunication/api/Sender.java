package iot.networkcommunication.api;


import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.lora.RegionalParameter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Sender {

    Optional<LoraTransmission> send(LoraWanPacket packet, Set<Receiver> receiver);

    boolean isTransmitting();

    List<LoraWanPacket> getSendingQueue();

    LoraWanPacket getTransmittingMessage();

    void abort();

    Sender setTransmissionPower(double transmissionPower);

    Sender setRegionalParameter(RegionalParameter regionalParameter);

    RegionalParameter getRegionalParameter();

    double getTransmissionPower();

    void reset();
}
