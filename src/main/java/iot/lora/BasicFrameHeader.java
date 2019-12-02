package iot.lora;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Basic implementation of {@link FrameHeader}
 */
public class BasicFrameHeader implements FrameHeader {

    private static final int SOURCE_ADDRESS_LENGTH = 4;

    private byte[] sourceAddress = new byte[SOURCE_ADDRESS_LENGTH];
    private short fCnt = 0;
    private byte[] fOpts = new byte[0];
    private byte fCtrl = -1;


    public BasicFrameHeader setSourceAddress(byte[] sourceAddress) {
        this.sourceAddress = sourceAddress;
        return this;
    }

    public BasicFrameHeader setSourceAddress(long sourceAddress) {
        var address = new byte[SOURCE_ADDRESS_LENGTH];
        ByteBuffer.wrap(address, 0, SOURCE_ADDRESS_LENGTH).putInt((int)sourceAddress);
        return setSourceAddress(address);
    }

    public BasicFrameHeader setFCnt(short fCnt) {
        this.fCnt = fCnt;
        return this;
    }

    public BasicFrameHeader setFCnt(byte[] fCnt) {
        return setFCnt(ByteBuffer.wrap(fCnt).getShort());
    }

    public BasicFrameHeader setFOpts(byte[] fOpts) {
        this.fOpts = fOpts;
        return this;
    }

    public BasicFrameHeader setFCtrl(byte fCtrl) {
        this.fCtrl = fCtrl;
        return this;
    }

    @Override
    public byte[] getSourceAddress() {
        return sourceAddress;
    }

    @Override
    public byte getFCtrl() {
        return fCtrl;
    }

    @Override
    public byte[] getfCnt() {
        var data = new byte[Short.BYTES];
        ByteBuffer.wrap(data).putShort(getFCntAsShort());
        return data;
    }

    @Override
    public short getFCntAsShort() {
        return fCnt;
    }

    @Override
    public byte[] getFOpts() {
        return fOpts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicFrameHeader that = (BasicFrameHeader) o;
        return getFCntAsShort() == that.getFCntAsShort();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFCntAsShort());
    }
}
