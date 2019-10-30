package IotDomain.lora;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Optional;

public class LoraWanPacket implements Serializable{
    private static final long serialVersionUID = 1L;
    /**
     * The payload of a packet.
     */
    private final Byte[] payload;
    /**
     * if the packet has low data rate optimization.
     */
    private final Boolean lowDataRateOptimization;
    /**
     * The coding rate of the packet
     */
    private final double codingRate;
    /**
     * The amount of preamble symbols of the packet
     */
    private final Integer amountOfPreambleSymbols;
    /**
     * The length in symbols of the packet.
     */
    private final Integer length;
    /**
     * The MAC commands included in the packet.
     */
    private final LinkedList<MacCommand> macCommands;
    /**
     * The EUI of designated receiver.
     */
    private final Long designatedReceiverEUI;
    /**
     * The EUI of the sender.
     */
    private final Long senderEUI;

    private final Optional<FrameHeader> header;

    //region constructor
    /**
     * A constructor generating a packet with a given payload, header, lowDataRateOptimization, amountOfPreambleSymbols,
     * codingRate and macCommands.
     * @param senderEUI
     * @param designatedReceiverEUI
     * @param payload
     * @param header
     * @param lowDataRateOptimization
     * @param amountOfPreambleSymbols
     * @param codingRate
     * @param macCommands
     */
    public LoraWanPacket(Long senderEUI, Long designatedReceiverEUI, Byte[] payload, @Nullable FrameHeader header, Boolean lowDataRateOptimization,
                         Integer amountOfPreambleSymbols, double codingRate, LinkedList<MacCommand> macCommands){
        this.senderEUI = senderEUI;
        this.designatedReceiverEUI = designatedReceiverEUI;
        this.amountOfPreambleSymbols = amountOfPreambleSymbols;
        this.codingRate = codingRate;
        this.header = Optional.ofNullable(header);
        this.lowDataRateOptimization = lowDataRateOptimization;
        this.payload = payload;
        this.macCommands = macCommands;
        this.length = payload.length + 13;
    }

    /**
     * A constructor generating a packet with a given payload and macCommands;
     * @param senderEUI
     * @param designatedReceiverEUI
     * @param payload
     * @param header
     * @param macCommands
     */
    public LoraWanPacket(Long senderEUI, Long designatedReceiverEUI, Byte[] payload, @Nullable FrameHeader header, LinkedList<MacCommand> macCommands){
        this(senderEUI, designatedReceiverEUI, payload, header,false,8,0.8,macCommands);
    }

    /**
     * A constructor generating a packet with a given payload and macCommands;
     * @param senderEUI
     * @param designatedReceiverEUI
     * @param payload
     * @param macCommands
     */
    public LoraWanPacket(Long senderEUI, Long designatedReceiverEUI, Byte[] payload, LinkedList<MacCommand> macCommands) {
        this(senderEUI, designatedReceiverEUI, payload, new BasicFrameHeader(), false, 8, 0.8, macCommands);
    }

    public static LoraWanPacket createEmptyPacket(Long senderEUI, Long designatedReceiverEUI) {
        return new LoraWanPacket(senderEUI, designatedReceiverEUI, new Byte[0], new LinkedList<>());
    }

    //endregion

    public Optional<FrameHeader> getHeader() {
        return header;
    }

    /**
     * Returns the MAC commands
     * @return the MAC commands.
     */
    public LinkedList<MacCommand> getMacCommands(){
        return macCommands;
    }

    /**
     * Returns the payload
     * @return the payload
     */
    public Byte[] getPayload() {
        return payload;
    }

    /**
     * Returns the EUI of designated receiver.
     * @return The EUI of designated receiver.
     */
    public Long getDesignatedReceiverEUI() {
        return designatedReceiverEUI;
    }

    /**
     * Returns the EUI of sender.
     * @return The EUI of sender.
     */
    public Long getSenderEUI() {
        return senderEUI;
    }

    /**
     * Returns if the packet has an explicit header.
     * @return if the packet has an explicit header.
     */
    public Boolean hasHeader() {
        return header.isPresent();
    }

    /**
     * Returns if the packet has low data rate optimization.
     * @return if the packet has low data rate optimization.
     */
    public Boolean hasLowDataRateOptimization() {
        return lowDataRateOptimization;
    }

    /**
     * Returns the coding rate.
     * @return the coding rate.
     */
    public double getCodingRate() {
        return codingRate;
    }

    /**
     * Returns the amount of preamble symbols.
     * @return the amount of preamble symbols.
     */
    public Integer getAmountOfPreambleSymbols() {
        return amountOfPreambleSymbols;
    }

    /**
     * Returns the length in symbols.
     * @return the length in symbols.
     */
    public Integer getLength(){
        return length;
    }
}
