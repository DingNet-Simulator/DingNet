package IotDomain;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

public class GlobalClock {

    /**
     * A representation of time.
     */
    private LocalTime time;

    private HashMap<LocalTime, Set<Trigger>> triggers;

    public GlobalClock(){
        time = LocalTime.of(0,0);
        triggers = new HashMap<>();
    }

    /**
     * Returns the current time.
     * @return The current time.
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * Increases the time with a given amount of milliseconds.
     * @param milliSeconds
     * @post Increases the time with a given amount of milliseconds.
     */
    public void tick(long milliSeconds) {
        for(long i = milliSeconds; i>0; i--){
            this.time= this.time.plus(1, ChronoUnit.MILLIS);
            fireTrigger();
        }
    }

    /**
     * Resets the time.
     * @post time is set to 0
     * @post all events are removed
     */
    public void reset(){
        this.time =  LocalTime.of(0,0);
        triggers = new HashMap<>();
    }

    public Boolean containsTriggers (LocalTime time){
        return triggers.containsKey(time);
    }

    public String addTrigger(LocalTime time,Supplier<LocalTime> trigger){
        var trig = new Trigger(trigger);
        addTrigger(time, trig);
        return trig.getUid();
    }

    private void addTrigger(LocalTime time,Trigger trigger) {
        if(containsTriggers(time)){
            triggers.get(time).add(trigger);
        }
        else {
            Set<Trigger> newTriggers = Set.of(trigger);
            triggers.put(time,newTriggers);
        }
    }

    public boolean removeTrigger(String triggerId) {
        for (Map.Entry<LocalTime, Set<Trigger>> e: triggers.entrySet()) {
            if (e.getValue().removeIf(p -> p.getUid().equals(triggerId))) {
                return true;
            }
        }
        return false;
    }

    private void fireTrigger() {
        triggers.remove(getTime()).forEach(trigger -> {
            LocalTime newTime = trigger.getCallback().get();
            if (newTime.isAfter(getTime())) {
                addTrigger(newTime, trigger);
            }
        });
    }

    private class Trigger {

        private final String uid;
        private final Supplier<LocalTime> callback;

        public Trigger(Supplier<LocalTime> callback) {
            uid = UUID.randomUUID().toString();
            this.callback = callback;
        }

        public String getUid() {
            return uid;
        }

        public Supplier<LocalTime> getCallback() {
            return callback;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Trigger trigger = (Trigger) o;
            return Objects.equals(getUid(), trigger.getUid());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUid());
        }
    }
}
