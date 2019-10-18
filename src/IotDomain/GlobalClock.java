package IotDomain;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Supplier;

public class GlobalClock {

    public GlobalClock(){
        time = LocalTime.of(0,0);
        triggers = new HashMap<>();
    }

    /**
     * A representation of time.
     */
    private LocalTime time;

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
            trigger();
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

    public void addTrigger(LocalTime time,Supplier<LocalTime> trigger){
        if(containsTriggers(time)){
            triggers.get(time).add(trigger);
        }
        else {
            LinkedList newtriggers = new LinkedList();
            newtriggers.add(trigger);
            triggers.put(time,newtriggers);
        }
    }

    private void trigger() {
        if(triggers.get(getTime()) != null) {
            for (Supplier<LocalTime> trigger : triggers.get(getTime())) {
                LocalTime newTime = trigger.get();
                if (newTime.compareTo(getTime()) > 0) {
                    addTrigger(newTime, trigger);
                }

            }
            triggers.remove(time);
        }
    }

    private HashMap<LocalTime, LinkedList<Supplier<LocalTime>>> triggers;
}
