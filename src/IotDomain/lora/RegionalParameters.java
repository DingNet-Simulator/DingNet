package IotDomain.lora;

public interface RegionalParameters {

    int getDataRate();

    int getSpreadingFactor();

    int getBandwidth();

    int getBitRate();

    int getMaximumPayloadSize();
}
