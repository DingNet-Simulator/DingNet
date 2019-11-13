package iot.lora;

import java.util.Base64;

public interface FrameHeader {

    byte[] getSourceAddress();

    default String getSourceAddressAsString() {
        return Base64.getEncoder().encodeToString(getSourceAddress());
    }

    byte getFCtrl();

    byte[] getFCnt();

    short getFCntAsShort();

    byte[] getFOpts();

    default boolean hasOptions() {
        return getFOpts().length > 0;
    }
}
