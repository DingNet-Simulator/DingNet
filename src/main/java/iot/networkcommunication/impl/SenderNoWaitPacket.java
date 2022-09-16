package iot.networkcommunication.impl;

import iot.environment.Characteristic;
import iot.environment.Environment;
import iot.environment.WeatherMap;
import iot.lora.LoraTransmission;
import iot.lora.LoraWanPacket;
import iot.lora.RegionalParameter;
import iot.lora.RxSensitivity;
import iot.networkcommunication.api.Receiver;
import iot.networkcommunication.api.Sender;
import iot.networkentity.NetworkEntity;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import util.Pair;
import util.TimeHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class SenderNoWaitPacket implements Sender {

    private RegionalParameter regionalParameter;
    private double transmissionPower;
    private boolean isTransmitting;
    private LocalDateTime currentTransmittingTime;
    private final NetworkEntity sender;
    private final Environment env;
    /**
     * TODO thing if set the seed or put inside the moveTo method
     * A Random necessary for the gaussian in the model.
     */
    private final Random random;


    public SenderNoWaitPacket(NetworkEntity sender, Environment environment) {
        random = environment.getRandom();
        reset();
        this.env = environment;
        this.sender = sender;

    }


    @Override
    public Optional<LoraTransmission> send(@NotNull LoraWanPacket packet, @NotNull Set<Receiver> receivers) {
        if (!isTransmitting) {
            var payloadSize = packet.getPayload().length + packet.getFrameHeader().getFOpts().length;
            if (regionalParameter.getMaximumPayloadSize() < payloadSize) {
                throw new IllegalArgumentException("Payload size greater then the max size. Payload size: " + payloadSize + ", " +
                    "but max size allowed with this regional parameter is: " + regionalParameter.getMaximumPayloadSize());
            }
            var timeOnAir = computeTimeOnAir(packet);
            var stream = receivers.parallelStream()
                .map(r -> new Pair<>(r,
                    new LoraTransmission(sender.getEUI(), r.getID(), sender.getPos(), r.getReceiverPosition(), transmissionPower,
                        regionalParameter, timeOnAir, env.getClock().getTime(), packet)));

            var filteredSet = stream.collect(Collectors.toSet());



            var ret = filteredSet.stream()
                .findFirst()
                .map(Pair::getRight);
            for (Pair<Receiver,LoraTransmission> pair : filteredSet) {
                pair.getRight().moveTo(env);
                pair.getLeft().receive(pair.getRight());
            }

            isTransmitting = true;

            var clock = env.getClock();
            currentTransmittingTime = clock.getTime().plusNanos((long) TimeHelper.miliToNano(timeOnAir));
            clock.addTriggerOneShot(clock.getTime().plusNanos((long) TimeHelper.miliToNano(timeOnAir)),
                () -> {isTransmitting = false;
                    currentTransmittingTime = null;});
            return ret;
        } else {
            throw new IllegalStateException("impossible to send two packet at the same time");
        }
    }

    /**
     * https://docs.google.com/spreadsheets/d/1voGAtQAjC1qBmaVuP1ApNKs1ekgUjavHuVQIXyYSvNc/edit#gid=0
     * @return time on air in milliseconds
     */
    private double computeTimeOnAir(LoraWanPacket packet) {
        /*((Math.pow(2,getSpreadingFactor())/getBandwidth())*(
                (8+Math.max(Math.ceil(
                        (8*getContent().getPayload().length-4*getSpreadingFactor()+28+16 - 20*(getContent().hasHeader()? 1: 0))
                                /4*(getSpreadingFactor() -2*(getContent().hasLowDataRateOptimization()?0:1)))
                        *getContent().getCodingRate(),0))
                        +getContent().getAmountOfPreambleSymbols()*4.25))/10;
        */
        var sf = regionalParameter.getSpreadingFactor();
        var bandwidth = regionalParameter.getBandwidth();
        var tSym = Math.pow(2, sf) / bandwidth;
        var tPreamble = (packet.getAmountOfPreambleSymbols() + 4.25) * tSym;
        var payloadSymbNb = (8 * packet.getPayload().length - 4 * sf + (28 + 16) - 20 * (packet.hasHeader() ? 0 : 1)) /
            ((4 * (sf - (packet.hasLowDataRateOptimization() ? 2 : 0))) * 1.0);
        payloadSymbNb = Math.ceil(payloadSymbNb);
        payloadSymbNb = payloadSymbNb * packet.getCodingRate();
        payloadSymbNb = 8 + Math.max(payloadSymbNb, 0);
        var tPayload = payloadSymbNb * tSym;
        return tPayload + tPreamble;
    }

    @Override
    public boolean isTransmitting() {
        return isTransmitting;
    }

    @Override
    public LocalDateTime getCurrentTransmittingTime() {
        return currentTransmittingTime;
    }

    @Override
    public List<LoraWanPacket> getSendingQueue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LoraWanPacket getTransmittingMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abort() {

    }

    @Override
    public Sender setTransmissionPower(double transmissionPower) {
        this.transmissionPower = transmissionPower;
        return this;
    }

    @Override
    public Sender setRegionalParameter(RegionalParameter regionalParameter) {
        this.regionalParameter = regionalParameter;
        return this;
    }

    @Override
    public RegionalParameter getRegionalParameter() {
        return regionalParameter;
    }

    @Override
    public double getTransmissionPower() {
        return transmissionPower;
    }

    @Override
    public void reset() {
        isTransmitting = false;
        currentTransmittingTime = null;
    }
}
