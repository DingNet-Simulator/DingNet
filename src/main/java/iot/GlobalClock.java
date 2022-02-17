package iot;

import util.TimeHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

/**
 * N.B. This clock store the triggers in a stack stack (list LIFO based on trigger uid)
 */
public class GlobalClock {

    private static long nextTriggerUid = 0;

    /**
     * A representation of time.
     */
    private LocalDateTime time;

    private Map<LocalDateTime, List<Trigger>> triggers;

    public GlobalClock() {
        reset();
    }

    /**
     * Returns the current time.
     * @return The current time.
     */
    public LocalDateTime getTime() {
        return time;
    }

    /**
     * Increases the time with a given amount of milliseconds.
     * @param milliSeconds
     * @post Increases the time with a given amount of milliseconds.
     */
    public void tick(long milliSeconds) {
        for (long i = milliSeconds; i > 0; i--) {
            this.time = this.time.plus(1, ChronoUnit.MILLIS);
            fireTrigger();

        }
    }

    /**
     * Resets the time.
     * @post time is set to 0
     * @post all events are removed
     */
    public void reset() {
        time = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        triggers = Collections.synchronizedMap(new HashMap<>());
    }

    public boolean containsTriggers(LocalDateTime time) {
        return triggers.containsKey(time);
    }

    public long addTrigger(LocalDateTime time, Supplier<LocalDateTime> trigger) {
        var trig = new Trigger(trigger);
        addTrigger(LocalDateTime.of(time.toLocalDate(),TimeHelper.roundToMilli(time.toLocalTime())), trig);
        return trig.getUid();
    }

    public long addTriggerOneShot(LocalDateTime time, Runnable trigger) {

        return addTrigger(time, () -> {
            trigger.run();
            return LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        });
    }

    private void addTrigger(LocalDateTime time, Trigger trigger) {
        synchronized (triggers) {
            if (containsTriggers(time)) {
                triggers.get(time).add(0, trigger);
            } else {
                List<Trigger> newTriggers = new ArrayList<>(List.of(trigger));
                triggers.put(time, newTriggers);
            }
        }
    }

    public void removeTrigger(long triggerId) {
        synchronized (triggers) {
            for (Map.Entry<LocalDateTime, List<Trigger>> e: triggers.entrySet()) {
                e.getValue().forEach(p -> {
                    if(p.getUid() == triggerId){
                        p.callback = () -> LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
                    }
                });
            }
        }
    }

    private void fireTrigger() {
        synchronized (triggers) {
            var triggersToFire = triggers.get(getTime());
            if (triggersToFire != null) {
                //Here you have to leave the normal 'for' because you can remove element from the list during the iteration
                //noinspection ForLoopReplaceableByForEach
                triggersToFire = new ArrayList<>(triggersToFire);
                for (int i = 0; i < triggersToFire.size(); i++) {
                    var trigger = triggersToFire.get(i);
                    LocalDateTime newTime = trigger.getCallback().get();
                    if (newTime.isAfter(getTime())) {
                        addTrigger(newTime, trigger);
                    }
                }
                triggers.remove(getTime());
            }
        }
    }

    private static class Trigger {

        private final long uid;
        private Supplier<LocalDateTime> callback;

        public Trigger(Supplier<LocalDateTime> callback) {
            uid = nextTriggerUid++;
            this.callback = callback;
        }

        public long getUid() {
            return uid;
        }

        public Supplier<LocalDateTime> getCallback() {
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
