package util;

public class MutableInteger {
    private int value;

    public MutableInteger(int value) {
        this.value = value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int intValue() {
        return this.value;
    }

}
