package iot.networkcommunication.api;

public interface Packet {

    long getSenderEUI();

    long getReceiverEUI();

    byte[] getPayload();

    byte[] getHeader();

    int getLength();
}
