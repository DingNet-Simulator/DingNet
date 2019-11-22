package iot;

import be.kuleuven.cs.som.annotate.Basic;
import datagenerator.GPSDataGenerator;
import datagenerator.SensorDataGenerator;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.MoteSensor;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import util.TimeHelper;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;

/**
 * A class representing a simulation.
 */
public class Simulation {

    // region fields
    /**
     * The InputProfile used in the simulation.
     */
    private InputProfile inputProfile;
    /**
     * The Environment used in th simulation.
     */
    private Environment environment;
    /**
     * The GenericFeedbackLoop used in the simulation.
     */
    private GenericFeedbackLoop approach;

    /**
     * A condition which determines if the simulation should continue (should return {@code false} when the simulation is finished).
     */
    private Predicate<Environment> continueSimulation;

    /**
     * Intermediate parameters used during simulation
     */
    private Map<Mote, Integer> wayPointMap;
    private Map<Mote, LocalTime> timeMap;

    // endregion

    // region constructors
    /**
     * Constructs a simulation  with a given InputProfile, Environment, GenericFeedbackLoop and GUI.
     * @param inputProfile The InputProfile to use.
     * @param environment The Environment to use.
     * @param approach The GenericFeedbackLoop to use.
     */
    public Simulation(InputProfile inputProfile, Environment environment, GenericFeedbackLoop approach){
        this.environment = environment;
        this.inputProfile = inputProfile;
        this.approach = approach;
        this.continueSimulation = null;
        this.wayPointMap = new HashMap<>();
        this.timeMap = new HashMap<>();
    }

    public Simulation() {}

     // endregion

    // region getter/setters

    /**
     * Gets the Environment used in th simulation.
     * @return The Environment used in the simulation.
     */
    @Basic
    public Environment getEnvironment() {
        return environment;
    }
    /**
     * Sets the Environment used in th simulation.
     * @param environment  The Environment to use in the simulation.
     */
    @Basic
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Gets the InputProfile used in th simulation.
     * @return The InputProfile used in the simulation.
     */
    @Basic
    public Optional<InputProfile> getInputProfile() {
        return Optional.ofNullable(inputProfile);
    }
    /**
     * Sets the InputProfile used in th simulation.
     * @param inputProfile  The InputProfile to use in the simulation.
     */
    @Basic
    public void setInputProfile(InputProfile inputProfile) {
        this.inputProfile = inputProfile;
    }

    /**
     * Gets the GenericFeedbackLoop used in th simulation.
     * @return The GenericFeedbackLoop used in the simulation.
     */
    @Basic
    public GenericFeedbackLoop getAdaptationAlgorithm(){
        return approach;
    }
    /**
     * Sets the GenericFeedbackLoop used in th simulation.
     * @param approach  The GenericFeedbackLoop to use in the simulation.
     */
    @Basic
    public void setAdaptationAlgorithm(GenericFeedbackLoop approach){
        this.approach = approach;
    }


    public GenericFeedbackLoop getApproach() {
        return approach;
    }
    /**
     * Sets the GenericFeedbackLoop.
     * @param approach The GenericFeedbackLoop to set.
     */
    @Basic
    void setApproach(GenericFeedbackLoop approach) {
        if (getApproach()!= null) {
            getApproach().stop();
        }
        this.approach = approach;
        getApproach().start();
    }
    // endregion


    /**
     * Gets the probability with which a mote should be active from the input profile of the current simulation.
     * If no probability is specified, the probability is set to one.
     * Then it performs a pseudo-random choice and sets the mote to active/inactive for the next run, based on that probability.
     */
    private void setupMotesActivationStatus() {
        List<Mote> motes = this.environment.getMotes();
        Set<Integer> moteProbabilities = this.inputProfile.getProbabilitiesForMotesKeys();
        for (int i = 0; i < motes.size(); i++) {
            Mote mote = motes.get(i);
            double activityProbability = 1;
            if (moteProbabilities.contains(i))
                activityProbability = this.inputProfile.getProbabilityForMote(i);
            if (Math.random() >= 1 - activityProbability)
                mote.enable(true);
        }
    }

    /**
     * check that do all motes arrive at their destination
     */
    private boolean areAllMotesAtDestination() {
        return this.environment.getMotes().stream()
            .allMatch(m -> !m.isEnabled() || m.isArrivedToDestination());
    }


    /**
     * Simulate a single step in the simulator.
     */
    void simulateStep() {
        this.environment.getMotes().stream()
            .filter(Mote::isEnabled)
            .map(mote -> { mote.consumePackets(); return mote;}) //DON'T replace with peek because the filtered mote after this line will not do the consume packet
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.environment.getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.environment.getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .forEach(mote -> {
                timeMap.put(mote, this.environment.getClock().getTime());
                if (!this.environment.toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPosInt())) {
                    this.environment.moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                } else {wayPointMap.put(mote, wayPointMap.get(mote) + 1);}
            });
        this.environment.getClock().tick(1);
    }




    boolean isFinished() {
        return !this.continueSimulation.test(this.environment);
    }


    private void setupSimulation(Predicate<Environment> pred) {
        this.wayPointMap = new HashMap<>();
        this.timeMap = new HashMap<>();

        setupMotesActivationStatus();

        this.environment.getGateways().forEach(Gateway::reset);

        this.environment.getMotes().forEach(mote -> {
            // Reset all the sensors of the mote
            mote.getSensors().stream()
                .map(MoteSensor::getSensorDataGenerator)
                .forEach(SensorDataGenerator::reset);

            mote.getSensors().stream()
                .map(MoteSensor::getSensorDataGenerator)
                .filter(s -> s instanceof GPSDataGenerator)
                .forEach(s -> ((GPSDataGenerator) s).setOrigin(environment.getMapOrigin()));

            // Initialize the mote (e.g. reset starting position)
            mote.reset();

            timeMap.put(mote, this.environment.getClock().getTime());
            wayPointMap.put(mote,0);

            // Add initial triggers to the clock for mote data transmissions (transmit sensor readings)
            environment.getClock().addTrigger(LocalTime.ofSecondOfDay(mote.getStartSendingOffset()), () -> {
                mote.sendToGateWay(
                    mote.getSensors().stream()
                        .flatMap(s -> s.getValueAsList(mote.getPosInt(), this.environment.getClock().getTime()).stream())
                        .toArray(Byte[]::new),
                    new HashMap<>());
                return environment.getClock().getTime().plusSeconds(mote.getPeriodSendingPacket());
            });
        });

        this.continueSimulation = pred;
    }

    void setupSingleRun(boolean shouldResetHistory) {
        if (shouldResetHistory) {
            this.environment.resetHistory();
        }

        this.setupSimulation((env) -> !areAllMotesAtDestination());
    }

    void setupTimedRun() {
        this.environment.resetHistory();

        var finalTime = environment.getClock().getTime()
            .plus(inputProfile.getSimulationDuration(), inputProfile.getTimeUnit());
        this.setupSimulation((env) -> env.getClock().getTime().isBefore(finalTime));
    }
}
