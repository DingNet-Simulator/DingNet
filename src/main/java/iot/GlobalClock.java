package iot;

import util.TimeHelper;
import util.time.DoubleTime;
import util.time.Time;

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
    private Time time;

    private Map<Time, List<Trigger>> triggers;

    public GlobalClock() {
        time = DoubleTime.zero();
        triggers = new HashMap<>();
    }

    /**
     * Returns the current time.
     * @return The current time.
     */
    public Time getTime() {
        return time;
    }

    /**
     * Increases the time with a given amount of milliseconds.
     * @param milliSeconds
     * @post Increases the time with a given amount of milliseconds.
     */
    public void tick(long milliSeconds) {
        for (long i = milliSeconds; i > 0; i--) {
            this.time = this.time.plusMillis(1);
            fireTrigger();
        }
    }

    /**
     * Resets the time.
     * @post time is set to 0
     * @post all events are removed
     */
    public void reset() {
        this.time = DoubleTime.zero();
        triggers = new HashMap<>();
    }

    public boolean containsTriggers(Time time) {
        return triggers.containsKey(time);
    }

    public long addTrigger(Time time, Supplier<Time> trigger) {
        var trig = new Trigger(trigger);
        addTrigger(TimeHelper.roundToMilli(time), trig);
        return trig.getUid();
    }

    public long addTriggerOneShot(Time time, Runnable trigger) {
        return addTrigger(time, () -> {
            trigger.run();
            return DoubleTime.zero();
        });
    }

    /**
     *
     * @param startingTime time when the trigger is fired for the first time
     * @param period the period of the trigger in second
     * @param trigger the trigger
     * @return the trigger id
     */
    public long addPeriodicTrigger(Time startingTime, long period, Runnable trigger) {
        return addTrigger(startingTime, () -> {
            trigger.run();
            return getTime().plusSeconds(period);
        });
    }

    private void addTrigger(Time time, Trigger trigger) {
        if (containsTriggers(time)) {
            triggers.get(time).add(0, trigger);
        } else {
            List<Trigger> newTriggers = new ArrayList<>(List.of(trigger));
            triggers.put(time, newTriggers);
        }
    }

    public boolean removeTrigger(long triggerId) {
        for (Map.Entry<Time, List<Trigger>> e: triggers.entrySet()) {
            if (e.getValue().removeIf(p -> p.getUid() == triggerId)) {
                return true;
            }
        }
        return false;
    }

    private void fireTrigger() {
        var triggersToFire = triggers.get(getTime());
        if (triggersToFire != null) {
            //Here you have to leave the normal 'for' because you can remove element from the list during the iteration
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < triggersToFire.size(); i++) {
                var trigger = triggersToFire.get(i);
                Time newTime = trigger.getCallback().get();
                if (newTime.isAfter(getTime())) {
                    addTrigger(newTime, trigger);
                }
            }
            triggers.remove(getTime());
        }
    }

    private static class Trigger {

        private final long uid;
        private final Supplier<Time> callback;

        public Trigger(Supplier<Time> callback) {
            uid = nextTriggerUid++;
            this.callback = callback;
        }

        public long getUid() {
            return uid;
        }

        public Supplier<Time> getCallback() {
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
