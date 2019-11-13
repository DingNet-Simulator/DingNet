package iot.lora;

import java.nio.ByteBuffer;

public class BasicFrameHeader implements FrameHeader {

    private static final int SOURCE_ADDRESS_LENGTH = 4;

    private byte[] sourceAddress = new byte[0];
    private short FCnt = 0;


    public BasicFrameHeader setSourceAddress(byte[] sourceAddress) {
        this.sourceAddress = sourceAddress;
        return this;
    }

    public BasicFrameHeader setSourceAddress(long sourceAddress) {
        var address = new byte[SOURCE_ADDRESS_LENGTH];
        ByteBuffer.wrap(address, 0, SOURCE_ADDRESS_LENGTH).putInt((int)sourceAddress);
        return setSourceAddress(address);
    }

    public BasicFrameHeader setFCnt(short FCnt) {
        this.FCnt = FCnt;
        return this;
    }

    public BasicFrameHeader setFCnt(byte[] FCnt) {
        return setFCnt(ByteBuffer.wrap(FCnt).getShort());
    }

    @Override
    public byte[] getSourceAddress() {
        return sourceAddress;
    }

    @Override
    public byte getFCtrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getFCnt() {
        return new byte[0];
    }

    @Override
    public short getFCntAsShort() {
        return FCnt;
    }

    @Override
    public byte[] getFOpts() {
        return new byte[0];
    }
}
