package util;

public class TimeHelper {
    public static double nanoToMili(double nanoTime){
        return  nanoTime / 1e6;
    }
    public static double miliToNano(double miliTime) {return miliTime * 1e6;}
    public static long miliToNano(long miliTime) {return miliTime * (long) 1e6;}
    public static double secToMili(double secTime){
        return  secTime * 1e3;
    }
}
