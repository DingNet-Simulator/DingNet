package iot.networkcommunication;

public interface Packet {

    long getSenderEUI();

    long getReceiverEUI();

    byte[] getPayload();

    byte[] getHeader();

    int getLength();
}
