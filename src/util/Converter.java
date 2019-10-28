package util;

public class Converter {

    static public byte[] toRowType(Byte[] data) {
        var ret = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            ret[i] = data[i];
        }
        return ret;
    }

    static public Byte[] toObjectType(byte[] data) {
        var ret = new Byte[data.length];
        for (int i = 0; i < data.length; i++) {
            ret[i] = data[i];
        }
        return ret;
    }
}
