package util.time;

public interface Time {

    Time as(TimeUnit timeUnit);

    double getAs(TimeUnit timeUnit);

    double asNano();

    double asMilli();

    double asSecond();

    double asMinute();

    double asHour();

    int getDay();

    Time plusNanos(double nanoSeconds);

    Time plusMillis(double milliSeconds);

    Time plusSeconds(double seconds);

    Time plusMinutes(double minutes);

    Time plusHours(double hours);

    boolean isAfter(Time other);

    boolean isBefore(Time other);
}
