package iot.networkcommunication.impl;

import iot.Characteristic;
import iot.Environment;
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
    private final Random random = new Random();

    public SenderNoWaitPacket(NetworkEntity sender, Environment environment) {
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
            var stream = receivers.stream()
                .map(r -> new Pair<>(r,
                    new LoraTransmission(sender.getEUI(), r.getID(), sender.getPos(), moveTo(r.getReceiverPosition(), transmissionPower),
                        regionalParameter, timeOnAir, env.getClock().getTime(), packet)))
                .filter(p -> packetStrengthHighEnough(p.getRight().getTransmissionPower()));

            var filteredSet = stream.collect(Collectors.toSet());



            var ret = filteredSet.stream()
                .findFirst()
                .map(Pair::getRight);
            for (Pair<Receiver,LoraTransmission> pair : filteredSet) {
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

    /**
     * Moves a transmission to a given position, while adapting the transmission power.
     * @param pos the position of the receiver
     * @param transmissionPower the initial transmission power
     * @return
     */
    private double moveTo(GeoPosition pos, double transmissionPower) {
        return moveTo(env.getMapHelper().toMapXCoordinate(pos),env.getMapHelper().toMapYCoordinate(pos), transmissionPower);
    }

    /**
     * Moves a transmission to a given position, while adapting the transmission power.
     * @param xPos  The x-coordinate of the destination.
     * @param yPos  The y-coordinate of the destination.
     * @param transmissionPower the initial transmission power
     * @return the transmission
     */
    private double moveTo(double xPos, double yPos, double transmissionPower) {
        double senderX = env.getMapHelper().toMapXCoordinate(sender.getPos());
        double senderY = env.getMapHelper().toMapYCoordinate(sender.getPos());
        double xDist = Math.abs(xPos - senderX);
        double yDist = Math.abs(yPos - senderY);
        int xDir;
        int yDir;
        Characteristic characteristic = null;

        while (transmissionPower > -300 && xDist + yDist > 0) {
            xDist = (int) Math.round(Math.abs(xPos - senderX));
            yDist = (int) Math.round(Math.abs(yPos - senderY));
            xDir = (int) Math.signum(xPos - senderX);
            yDir = (int) Math.signum(yPos - senderY);
            characteristic = env.getCharacteristic(xPos, yPos);

            if (xDist + yDist > 1) {
                if (xDist >  2 * yDist || yDist >  2 * xDist) {
                    transmissionPower = transmissionPower - 10 * characteristic.getPathLossExponent() * (Math.log10(xDist + yDist) - Math.log10(xDist + yDist - 1));
                    if (xDist >  2 * yDist) {
                        xPos = xPos - xDir;
                    } else {
                        yPos = yPos - yDir;
                    }
                } else {
                    transmissionPower = transmissionPower - 10 * characteristic.getPathLossExponent() * (Math.log10(xDist + yDist) - Math.log10(xDist + yDist - Math.sqrt(2)));
                    xPos = xPos - xDir;
                    yPos = yPos - yDir;
                }
            } else if (xDist + yDist == 1) {
                if (xDist >  yDist) {
                    xPos = xPos - xDir;
                } else {
                    yPos = yPos - yDir;
                }
            }

        }
        return transmissionPower - random.nextGaussian() * ((characteristic == null) ? env.getCharacteristic(xPos, yPos) : characteristic).getShadowFading();
    }

    /**
     * Checks if a transmission is strong enough to be received.
     */
    private boolean packetStrengthHighEnough(double transmissionPower) {
        return transmissionPower > RxSensitivity.getReceiverSensitivity(regionalParameter);
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
