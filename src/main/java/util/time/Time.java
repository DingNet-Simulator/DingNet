package util.time;

public interface Time {

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
}
