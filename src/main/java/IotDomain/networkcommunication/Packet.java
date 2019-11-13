package IotDomain.networkcommunication;

public interface Packet {

    long getSenderEUI();

    long getReceiverEUI();

    byte[] getPayload();

    byte[] getHeader();
}
