package IotDomain;

import GUI.MainGUI;
import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;
import be.kuleuven.cs.som.annotate.Basic;
import util.Pair;
import util.TimeHelper;

import java.time.LocalTime;
import java.util.*;

/**
 * A class representing a simulation.
 */
public class Simulation implements Runnable {

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
     * The GUI on which the simulation is running.
     */
    private MainGUI gui;
    // </Params>

    // <Constructors>
    /**
     * Constructs a simulation  with a given InputProfile, Environment, GenericFeedbackLoop and GUI.
     * @param inputProfile The InputProfile to use.
     * @param environment The Environment to use.
     * @param approach The GenericFeedbackLoop to use.
     * @param gui The MainGUI to use.
     */
    public Simulation(InputProfile inputProfile, Environment environment, GenericFeedbackLoop approach, MainGUI gui){
        this.environment = environment;
        this.inputProfile = inputProfile;
        this.approach = approach;
        this.gui = gui;
    }

    public Simulation(MainGUI gui){
        this.gui = gui;
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

    private void updateMotesLocation(HashMap<Mote, Pair<Integer,Integer>> locations)
    {
        LinkedList<Mote> motes = this.environment.getMotes();
        for(Mote mote : motes){
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
                m.isEnabled() &&
                !m.getPath().getWayPoints().isEmpty() &&
                m.getPath().getWayPoints().getLast() != null &&
                !this.environment.toMapCoordinate(m.getPath().getWayPoints().getLast()).equals(m.getPos()));
    }

    private void animate(HashMap<Mote, Pair<Integer,Integer>> locationMap, HashMap<Mote,LinkedList<Pair<Integer,Integer>>> locationHistoryMap, Integer speed){
        updateMotesLocation(locationMap);
        Timer timer = new Timer();
        AnimationTimerTask animationTimerTask = new AnimationTimerTask(locationHistoryMap);
        timer.schedule(animationTimerTask,0,75/speed);
        updateMotesLocation(locationMap);
    }

    private SimualtionResult simulate(){
        LinkedList<Mote> motes = this.environment.getMotes();
        HashMap<Mote,Integer> wayPointMap = new HashMap<>();
        HashMap<Mote,LocalTime> timeMap = new HashMap<>();
        HashMap<Mote, Pair<Integer,Integer>> locationMap = new HashMap<>();
        HashMap<Mote,LinkedList<Pair<Integer,Integer>>> locationHistoryMap = new HashMap<>();
        for(Mote mote : motes){
            timeMap.put(mote, this.environment.getClock().getTime());
            locationMap.put(mote,new Pair<>(mote.getXPos(), mote.getYPos()));
            locationHistoryMap.put(mote, new LinkedList<>());
            LinkedList historyMap = locationHistoryMap.get(mote);
            historyMap.add(new Pair<>(mote.getXPos(), mote.getYPos()));
            locationHistoryMap.put(mote, historyMap);
            wayPointMap.put(mote,0);
        }


        while (!areAllMotesAtDestination()) {
            for(Mote mote : motes){
                if(mote.isEnabled() && mote.getPath().getWayPoints().size() > wayPointMap.get(mote)) {
                    //? What is the offset for the mote? Is in second, mili second or what? Why multiplies to 1e6?
                    if (TimeHelper.secToMili( 1 / mote.getMovementSpeed()) <
                            TimeHelper.nanoToMili(this.environment.getClock().getTime().toNanoOfDay() - timeMap.get(mote).toNanoOfDay()) &&
                            (this.environment.getClock().getTime().toNanoOfDay() / 100000 > Math.abs(mote.getStartOffset()) * 100000)) {
                        timeMap.put(mote, this.environment.getClock().getTime());
                        if (!this.environment.toMapCoordinate(mote.getPath().getWayPoints().get(wayPointMap.get(mote))).equals(mote.getPos())) {
                            this.environment.moveMote(mote, mote.getPath().getWayPoints().get(wayPointMap.get(mote)));
                            LinkedList historyMap = locationHistoryMap.get(mote);
                            historyMap.add(mote.getPos());
                            locationHistoryMap.put(mote, historyMap);
                            if (mote.shouldSend()) {
                                Byte[] dataByte = mote.getSensors().stream()
                                    .flatMap(s -> s.getValueAsList(mote.getPos(), this.environment.getClock().getTime()).stream())
                                    .toArray(Byte[]::new);
                                mote.sendToGateWay(dataByte, new HashMap<>());
                            }
                        } else {wayPointMap.put(mote, wayPointMap.get(mote) + 1);}
                    }
                }

            }
            this.environment.getClock().tick(1);
        }
        return new SimualtionResult(locationMap, locationHistoryMap);
    }

    /**
     * A method for running a single run with visualisation.
     * @param speed
     */
    public void singleRun(Integer speed) {
        setupMotesActivationStatus();
        this.environment.reset();
        SimualtionResult result = this.simulate();
        this.animate(result.getLocationMap(), result.getLocationHistoryMap(), speed);
    }

    /**
     * A method for running the simulation as described in the inputProfile.
     */
    public void run(){
        getEnvironment().reset();
        for(int i =0; i< getInputProfile().getNumberOfRuns();i++) {
            setupMotesActivationStatus();
            getEnvironment().getClock().reset();    //why we add this
            gui.setProgress(i,getInputProfile().getNumberOfRuns());
            if(i != 0) {
                getEnvironment().addRun();
            }
            SimualtionResult result =  this.simulate();
            gui.setProgress(getInputProfile().getNumberOfRuns(),getInputProfile().getNumberOfRuns());
            updateMotesLocation(result.getLocationMap());
        }
    }

    private class SimualtionResult{
        private HashMap<Mote, Pair<Integer,Integer>> locationMap;
        private HashMap<Mote,LinkedList<Pair<Integer,Integer>>> locationHistoryMap;
        SimualtionResult(HashMap<Mote, Pair<Integer,Integer>> locationMap, HashMap<Mote,LinkedList<Pair<Integer,Integer>>> locationHistoryMap){
            this.locationMap = locationMap;
            this.locationHistoryMap = locationHistoryMap;
        }
        public HashMap getLocationMap(){
            return this.locationMap;
        }
        public HashMap getLocationHistoryMap(){
            return this.locationHistoryMap;
        }
    }

    /**
     * An animation task needed for the visualisation.
     */
    private class AnimationTimerTask extends TimerTask {

        HashMap<Mote,Integer> timeMap = new HashMap<>();
        Boolean arrived = false;
        HashMap<Mote,Integer> waypointMap = new HashMap<>();
        HashMap<Mote,LinkedList<Pair<Integer,Integer>>> locationHistoryMap;
        int i;
        public AnimationTimerTask(HashMap<Mote,LinkedList<Pair<Integer,Integer>>> locationHistoryMap){
            i =0;
            for (Mote mote: environment.getMotes()){
                timeMap.put(mote,i);
                waypointMap.put(mote,0);
            }
            this.locationHistoryMap = locationHistoryMap;
        }


        @Override
        public void run() {
            Boolean moved = false;
            arrived = true;
            for (Mote mote : getEnvironment().getMotes()){
                if(waypointMap.get(mote) < locationHistoryMap.get(mote).size()) {
                    arrived  = false;
                    if(i-timeMap.get(mote)> 1 / mote.getMovementSpeed() *100){
                        timeMap.put(mote, i);
                        mote.setXPos(locationHistoryMap.get(mote).get(waypointMap.get(mote)).getLeft());
                        mote.setYPos(locationHistoryMap.get(mote).get(waypointMap.get(mote)).getRight());
                        moved = true;
                        waypointMap.put(mote,waypointMap.get(mote)+25);
                    }
                }
            }
            if(arrived){
                for(Mote mote : environment.getMotes()){
                    Pair<Integer,Integer> location = locationHistoryMap.get(mote).getFirst();
                    mote.setXPos(location.getLeft());
                    mote.setYPos(location.getRight());
                }
                gui.refreshMap();

                cancel();
            }
            if(moved) {
                gui.refreshMap();
            }
            i = i+50;

        }
    }
}
