package util.time;

import java.util.Objects;

/**
 * Immutable class, default unit of measure is milliseconds
 */
public class DoubleTime implements Time {

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
    public Time as(TimeUnit timeUnit) {
        return new DoubleTime(this.timeUnit.convertTo(time, timeUnit), timeUnit);
    }

    @Override
    public double getAs(TimeUnit timeUnit) {
        return this.timeUnit.convertTo(time, timeUnit);
    }

    @Override
    public double asNano() {
        return getAs(TimeUnit.NANOS);
    }

    @Override
    public double asMilli() {
        return getAs(TimeUnit.MILLIS);
    }

    @Override
    public double asSecond() {
        return getAs(TimeUnit.SECONDS);
    }

    @Override
    public double asMinute() {
        return getAs(TimeUnit.MINUTES);
    }

    @Override
    public double asHour() {
        return getAs(TimeUnit.HOURS);
    }

    @Override
    public int getDay() {
        return (int)asHour() % 24;
    }

    private DoubleTime plus(double value, TimeUnit timeUnit) {
        return new DoubleTime(time + timeUnit.convertTo(value, this.timeUnit), this.timeUnit);
    }

    @Override
    public Time plusNanos(double nanoSeconds) {
        return plus(nanoSeconds, TimeUnit.NANOS);
    }

    @Override
    public Time plusMillis(double milliSeconds) {
        return plus(milliSeconds, TimeUnit.MILLIS);
    }

    @Override
    public Time plusSeconds(double seconds) {
        return plus(seconds, TimeUnit.SECONDS);
    }

    @Override
    public Time plusMinutes(double minutes) {
        return plus(minutes, TimeUnit.MINUTES);
    }

    @Override
    public Time plusHours(double hours) {
        return plus(hours, TimeUnit.HOURS);
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
