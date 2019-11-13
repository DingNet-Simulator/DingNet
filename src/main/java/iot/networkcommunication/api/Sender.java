package iot.networkcommunication.api;


import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.lora.RegionalParameter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Sender<P extends Packet> {

    Optional<LoraTransmission<LoraWanPacket>> send(P packet, Set<Receiver<P>> receiver);

    boolean isTransmitting();

    List<P> getSendingQueue();

    P getTransmittingMessage();

    void abort();

    Sender<P> setTransmissionPower(double transmissionPower);

    Sender<P> setRegionalParameter(RegionalParameter regionalParameter);

    RegionalParameter getRegionalParameter();

    double getTransmissionPower();

    void reset();
}
