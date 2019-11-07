package IotDomain.lora;

public interface RegionalParameter {

    int getDataRate();

    int getSpreadingFactor();

    int getBandwidth();

    int getBitRate();

    int getMaximumPayloadSize();
}
