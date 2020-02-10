package util.time;

public enum TimeUnit {

    NANOS("Nano", 1),
    MICROS("Micro", NANOS.v * 1e3),
    MILLIS("Milli", MICROS.v * 1e3),
    SECONDS("Second", MILLIS.v * 1e3),
    MINUTES("Minute", SECONDS.v * 60),
    HOURS("Hours", MINUTES.v * 60);

    private final String name;
    private final double v;

    TimeUnit(String name, double v) {
        this.name = name;
        this.v = v;
    }

    public double convertTo(double toConvert, TimeUnit newTimeUnit) {
        return toConvert * v / newTimeUnit.v;
    }

    public double convertFromNano(long nanoSeconds) {
        return nanoSeconds / v;
    }

    public double convertFromNano(double nanoSeconds) {
        return nanoSeconds / v;
    }
}
