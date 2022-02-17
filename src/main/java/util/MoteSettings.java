package util;

public class MoteSettings {
    private final int transmissionPower;
    private final int expirationTime;
    private final int transmissionInterval;

    public MoteSettings(int transmissionPower, int expirationTime, int transmissionInterval) {
        this.transmissionPower = transmissionPower;
        this.expirationTime = expirationTime;
        this.transmissionInterval = transmissionInterval;
    }

    public static int amountOfSettings() {
        return 3;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public int getTransmissionInterval() {
        return transmissionInterval;
    }

    public int getTransmissionPower() {
        return transmissionPower;
    }
}
