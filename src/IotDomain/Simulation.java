package IotDomain;

import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;
import be.kuleuven.cs.som.annotate.Basic;
import util.Pair;
import util.TimeHelper;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    public void setApproach(GenericFeedbackLoop approach) {
        if(getApproach()!= null) {
            getApproach().stop();
        }
        this.approach = approach;
        getApproach().start();
    }
    // <GetterSetters>

    public void updateMotesLocation(Map<Mote, Pair<Integer,Integer>> locations)
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
        return this.environment.getMotes().stream().noneMatch(m ->
            m.isEnabled() && !m.getPath().isEmpty() && m.getPath().get(m.getPath().size()-1) != null &&
            !this.environment.toMapCoordinate(m.getPath().get(m.getPath().size()-1)).equals(m.getPos()));
    }


    /**
     *
     * @param predicate predicate to define the condition to terminate the simulation
     * @return the simulation result
     */
    private SimulationResult simulate(Predicate<Environment> predicate){
        LinkedList<Mote> motes = this.environment.getMotes();
        Map<Mote, Integer> wayPointMap = new HashMap<>();
        Map<Mote, LocalTime> timeMap = new HashMap<>();
        Map<Mote, List<Pair<Integer,Integer>>> locationHistoryMap = new HashMap<>();
        for(Mote mote : motes){
            timeMap.put(mote, this.environment.getClock().getTime());
            locationHistoryMap.put(mote, new LinkedList<>());
            List<Pair<Integer, Integer>> historyMap = locationHistoryMap.get(mote);
            historyMap.add(new Pair<>(mote.getXPos(), mote.getYPos()));
            locationHistoryMap.put(mote, historyMap);
            wayPointMap.put(mote,0);
            environment.getClock().addTrigger(LocalTime.ofSecondOfDay(mote.getStartSendingOffset()), () -> {
                mote.sendToGateWay(
                    mote.getSensors().stream()
                        .flatMap(s -> s.getValueAsList(mote.getPos(), this.environment.getClock().getTime()).stream())
                        .toArray(Byte[]::new),
                    new HashMap<>());
                return environment.getClock().getTime().plusSeconds(mote.getPeriodSendingPacket());
            });
        }

        while (predicate.test(environment)) {
            for(Mote mote : motes){
                if(mote.isEnabled() && mote.getPath().size() > wayPointMap.get(mote)) {
                    if (TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                            TimeHelper.nanoToMili(this.environment.getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()) &&
                            (TimeHelper.nanoToMili(this.environment.getClock().getTime().toNanoOfDay()) > TimeHelper.secToMili(Math.abs(mote.getStartMovementOffset())))) {
                        timeMap.put(mote, this.environment.getClock().getTime());
                        if (!this.environment.toMapCoordinate(mote.getPath().get(wayPointMap.get(mote))).equals(mote.getPos())) {
                            this.environment.moveMote(mote, mote.getPath().get(wayPointMap.get(mote)));
                            List<Pair<Integer, Integer>> historyMap = locationHistoryMap.get(mote);
                            historyMap.add(mote.getPos());
                            locationHistoryMap.put(mote, historyMap);
                        } else {wayPointMap.put(mote, wayPointMap.get(mote) + 1);}
                    }
                }
                mote.consumePackets();
            }
            this.environment.getClock().tick(1);
        }
        return new SimulationResult(locationHistoryMap);
    }

    /**
     * A method for running a run for a specified period of time (specified in the InputProfile) without visualisation.
     */
    public void timedRun() {
        setupMotesActivationStatus();
        this.environment.reset();
        var finalTime = environment.getClock().getTime()
            .plus(getInputProfile().getSimulationDuration(), getInputProfile().getTimeUnit());
        this.simulate(env -> env.getClock().getTime().isBefore(finalTime));
    }

    /**
     * A method for running a single run with visualisation.
     */
    SimulationResult singleRun() {
        setupMotesActivationStatus();
        this.environment.reset();
        return this.simulate(env -> !areAllMotesAtDestination());
    }

    /**
     * A method for running the simulation multiple times, specified in the InputProfile.
     * @param fn A callback function used to track the progress of the runs : fn(Pair<>(currentIndex, totalRuns)).
     */
    void multipleRuns(Function<Pair<Integer, Integer>, Void> fn) {
        Thread t = new Thread(() -> {
            getEnvironment().reset();
            int nrOfRuns = getInputProfile().getNumberOfRuns();

            for (int i = 0; i < getInputProfile().getNumberOfRuns(); i++) {
                setupMotesActivationStatus();
                getEnvironment().getClock().reset();    //why we add this

                if (fn != null) {
                    fn.apply(new Pair<>(i, nrOfRuns));
                }

                if(i != 0) {
                    getEnvironment().addRun();
                }
                SimulationResult result =  this.simulate(env -> !areAllMotesAtDestination());
                updateMotesLocation(result.getLocationMap());
            }
            if (fn != null) {
                fn.apply(new Pair<>(nrOfRuns, nrOfRuns));
            }
        });
        t.start();
    }


    public class SimulationResult {
        private Map<Mote, List<Pair<Integer,Integer>>> locationHistoryMap;

        SimulationResult(Map<Mote, List<Pair<Integer,Integer>>> locationHistoryMap){
            this.locationHistoryMap = locationHistoryMap;
        }

        public Map<Mote, Pair<Integer, Integer>> getLocationMap(){
            return this.locationHistoryMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().get(0)
            ));
        }

        public Map<Mote, List<Pair<Integer, Integer>>> getLocationHistoryMap(){
            return this.locationHistoryMap;
        }
    }


}
