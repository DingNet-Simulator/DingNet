package util.time;

import util.TimeHelper;

import java.util.Objects;

/**
 * Immutable class, default unit of measure is milliseconds
 */
public class DoubleTime implements Time{

    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLIS;
    private double time;
    private final TimeUnit timeUnit;

    // region constructor
    public DoubleTime() {
        this(0);
    }

    public DoubleTime(double time) {
        this(time, DEFAULT_TIME_UNIT);
    }

    private DoubleTime(double time, TimeUnit timeUnit) {
        this.time = time;
        this.timeUnit = timeUnit;
    }
    // endregion

    static public Time zero() {
        return new DoubleTime(0);
    }

    static public Time fromSeconds(double time) {
        return new DoubleTime(time * 1e3);
    }

    @Override
    public double asNano() {
        return TimeHelper.miliToNano(asMilli());
    }

    @Override
    public double asMilli() {
        return time;
    }

    @Override
    public double asSecond() {
        return asMilli() / 1e3;
    }

    @Override
    public double asMinute() {
        return asSecond() / 60;
    }

    @Override
    public double asHour() {
        return asMinute() / 60;
    }

    @Override
    public int getDay() {
        return (int)asMinute() % 24;
    }

    @Override
    public Time plusNanos(double nanoSeconds) {
        return plusMillis(TimeHelper.nanoToMili(nanoSeconds));
    }

    @Override
    public Time plusMillis(double milliSeconds) {
        return new DoubleTime(asMilli() + milliSeconds);
    }

    @Override
    public Time plusSeconds(double seconds) {
        return plusMillis(TimeHelper.secToMili(seconds));
    }

    @Override
    public Time plusMinutes(double minutes) {
        return plusSeconds(minutes * 60);
    }

    @Override
    public boolean isAfter(Time other) {
        return asMilli() > other.asMilli();
    }

    @Override
    public boolean isBefore(Time other) {
        return asMilli() < other.asMilli();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleTime that = (DoubleTime) o;
        return Double.compare(that.time, time) == 0 &&
            timeUnit == that.timeUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, timeUnit);
    }

    @Override
    public String toString() {
        return "DoubleTime[" +
            "time=" + time +
            ", timeUnit=" + timeUnit +
            ']';
    }
}
