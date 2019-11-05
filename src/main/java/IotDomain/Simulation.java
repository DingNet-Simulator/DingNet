package IotDomain;

import IotDomain.networkentity.Mote;
import IotDomain.networkentity.MoteSensor;
import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;
import SensorDataGenerators.SensorDataGenerator;
import be.kuleuven.cs.som.annotate.Basic;
import util.Pair;
import util.TimeHelper;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A class representing a simulation.
 */
public class Simulation {

    /** <Params>
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
    // </Params>

    // <Constructors>
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

    public Simulation(){
    }

     // </Constructors>

    // <GetterSetters>

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
    public InputProfile getInputProfile() {
        return inputProfile;
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
        if(getApproach()!= null) {
            getApproach().stop();
        }
        this.approach = approach;
        getApproach().start();
    }
    // <GetterSetters>

    void updateMotesLocation(Map<Mote, Pair<Integer,Integer>> locations)
    {
        List<Mote> motes = this.environment.getMotes();
        for (Mote mote : motes) {
            Pair<Integer,Integer> location = locations.get(mote);
            mote.setXPos(location.getLeft());
            mote.setYPos(location.getRight());
        }
    }
    /**
     * Gets the probability with which a mote should be active from the input profile of the current simulation.
     * If no probability is specified, the probability is set to one.
     * Then it performs a pseudo-random choice and sets the mote to active/inactive for the next run, based on that probability.
     */
    private void setupMotesActivationStatus() {
        LinkedList<Mote> motes = this.environment.getMotes();
        Set<Integer> moteProbabilities = this.inputProfile.getProbabilitiesForMotesKeys();
        for(int i = 0; i < motes.size(); i++) {
            Mote mote = motes.get(i);
            Double activityProbability = 1.0;
            if(moteProbabilities.contains(i))
                activityProbability = this.inputProfile.getProbabilityForMote(i);
            if(Math.random() >= 1.0 - activityProbability)
                mote.enable(true);
        }
    }

    /**
     * check that do all motes arrive at their destination
     */
    private Boolean areAllMotesAtDestination() {
        return this.environment.getMotes().stream().allMatch(m ->
                !m.isEnabled() || m.isArrivedToDestination());
    }


    /**
     * Simulate a single step in the simulator.
     */
    void simulateStep() {
        this.environment.getMotes().stream()
            .filter(Mote::isEnabled)
            .peek(Mote::consumePackets)
            .filter(mote -> mote.getPath().getWayPoints().size() > wayPointMap.get(mote))
            .filter(mote -> TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                TimeHelper.nanoToMili(this.environment.getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()))
            .filter(mote -> TimeHelper.nanoToMili(this.environment.getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))
            .peek(mote -> timeMap.put(mote, this.environment.getClock().getTime()))
            .forEach(mote -> {
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

        for (Mote mote : this.environment.getMotes()) {
            mote.initialize();

            timeMap.put(mote, this.environment.getClock().getTime());
            wayPointMap.put(mote,0);
            environment.getClock().addTrigger(LocalTime.ofSecondOfDay(mote.getStartSendingOffset()), () -> {
                mote.sendToGateWay(
                    mote.getSensors().stream()
                        .flatMap(s -> s.getValueAsList(mote.getPosInt(), this.environment.getClock().getTime()).stream())
                        .toArray(Byte[]::new),
                    new HashMap<>());
                return environment.getClock().getTime().plusSeconds(mote.getPeriodSendingPacket());
            });
        }

        this.continueSimulation = pred;
    }

    void setupSingleRun() {
        setupMotesActivationStatus();
        reset();
        this.setupSimulation((env) -> !areAllMotesAtDestination());
    }

    void setupTimedRun() {
        setupMotesActivationStatus();
        reset();
        var finalTime = environment.getClock().getTime()
            .plus(getInputProfile().getSimulationDuration(), getInputProfile().getTimeUnit());
        this.setupSimulation((env) -> env.getClock().getTime().isBefore(finalTime));
    }

    private void reset() {
        this.environment.reset();
        getEnvironment().getMotes().stream()
            .flatMap(m -> m.getSensors().stream())
            .map(MoteSensor::getSensorDataGenerator)
            .forEach(SensorDataGenerator::reset);
    }


    /**
     * A method for running the simulation multiple times, specified in the InputProfile.
     * @param fn A callback function used to track the progress of the runs : fn(Pair<>(currentIndex, totalRuns)).
     */
    void multipleRuns(Function<Pair<Integer, Integer>, Void> fn) {
        Thread t = new Thread(() -> {
            getEnvironment().reset();
            int nrOfRuns = getInputProfile().getNumberOfRuns();

            // Store the initial positions of the motes so that these can be reset after each simulation run
            Map<Mote, Pair<Integer, Integer>> initialLocationMap = new HashMap<>();
            getEnvironment().getMotes().forEach(m -> initialLocationMap.put(m, m.getPosInt()));

            for (int i = 0; i < getInputProfile().getNumberOfRuns(); i++) {
                setupMotesActivationStatus();
                getEnvironment().getClock().reset();    //why we add this
                this.setupSimulation((env) -> !areAllMotesAtDestination());

                if (fn != null) {
                    fn.apply(new Pair<>(i, nrOfRuns));
                }
                if (i != 0) {
                    getEnvironment().addRun();
                }

                while (!isFinished()) {
                    this.simulateStep();
                }

                updateMotesLocation(initialLocationMap);
            }
            if (fn != null) {
                fn.apply(new Pair<>(nrOfRuns, nrOfRuns));
            }
        });
        t.start();
    }
}
