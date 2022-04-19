package iot;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Interface used to provide a callback during simulation.
 * The granularity of the callback is decided by the function which is simulating the simulator step-wise.
 */
public interface SimulationUpdateListener {
    void update();
    void onEnd();

    void update(LocalDateTime time, long simulationDuration, ChronoUnit timeUnit);
}
