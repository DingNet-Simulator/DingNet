package util.buffer;

import iot.GlobalClock;


import java.util.LinkedList;

public class ExpiringBuffer<T> implements Buffer<T>{
    private GlobalClock clock;
    private LinkedList<T> queue;
    private int expirationTime;

    public ExpiringBuffer(GlobalClock clock, int expirationTime){
        this.clock = clock;
        this.queue = new LinkedList<>();
        this.expirationTime = expirationTime;
    }
    @Override
    public void add(T item) {
        queue.add(item);
        clock.addTriggerOneShot(clock.getTime().plusSeconds(expirationTime),() ->{expire(item);});
    }

    @Override
    public T retrieve() {
        T answer = queue.removeFirst();
        return answer;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }


    public void expire(T item){
        queue.remove(item);
    }

    public void setExpirationTime(int value) {
        expirationTime = value;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public void clear() {
        this.queue.clear();
    }
}
