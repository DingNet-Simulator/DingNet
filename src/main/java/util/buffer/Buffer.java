package util.buffer;


public interface Buffer<T> {
    void add(T item);
    T retrieve();

    boolean isEmpty();
}

