package IotDomain;

import GUI.MainGUI;
import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;
import be.kuleuven.cs.som.annotate.Basic;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A class representing a simulation.
 */
public class Simulation implements Runnable {
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
     * Sets the GenericFeedbackLoop used in th simulation.
     * @param approach  The GenericFeedbackLoop to use in the simulation.
     */
    @Basic
    public void setAdaptationAlgorithm(GenericFeedbackLoop approach){
        this.approach = approach;
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
     * A method for running a single run with visualisation.
     * @param speed
     */
    public void singleRun(Integer speed) {
        //Check if a mote can participate in this run.
        calculateIfMotesAreActiveBasedOnInputProfile();
        // reset the environment.
        getEnvironment().reset();

        Boolean arrived = true;
        HashMap<Mote,Integer> waypoinMap = new HashMap<>();
        HashMap<Mote,LocalTime> timemap = new HashMap<>();
        HashMap<Mote,Pair<Integer,Integer>> locationmap = new HashMap<>();
        HashMap<Mote,LinkedList<Pair<Integer,Integer>>> locationhistorymap = new HashMap<>();
        for(Mote mote : getEnvironment().getMotes()){
            timemap.put(mote, getEnvironment().getTime());
            locationmap.put(mote,new Pair<>(mote.getXPos(),mote.getYPos()));
            locationhistorymap.put(mote, new LinkedList<>());
            LinkedList historyMap = locationhistorymap.get(mote);
            historyMap.add(new Pair<>(mote.getXPos(),mote.getYPos()));
            locationhistorymap.put(mote,historyMap);
            if(mote.getPath().size() != 0) {
                if (moteIsOnNextWayPoint(mote)) {
                    arrived = arrived && false;
                }
            }
            waypoinMap.put(mote,0);
        }

        while (!arrived) {

            for(Mote mote : getEnvironment().getMotes()){
                if(mote.isEnabled()) {
                    if (Integer.signum(mote.getPath().size() - waypoinMap.get(mote)) > 0) {

                        if (1 / mote.getMovementSpeed() * 1000 < (getEnvironment().getTime().toNanoOfDay() - timemap.get(mote).toNanoOfDay()) / 100000 &&
                                Long.signum(getEnvironment().getTime().toNanoOfDay() / 100000 - Math.abs(mote.getStartOffset()) * 100000) > 0) {
                            timemap.put(mote, getEnvironment().getTime());
                            if (Integer.signum(mote.getXPos() - getEnvironment().toMapXCoordinate(mote.getPath().get(waypoinMap.get(mote)))) != 0 ||
                                    Integer.signum(mote.getYPos() - getEnvironment().toMapYCoordinate(mote.getPath().get(waypoinMap.get(mote)))) != 0) {
                                getEnvironment().moveMote(mote, mote.getPath().get(waypoinMap.get(mote)));
                                LinkedList historymap = locationhistorymap.get(mote);
                                historymap.add(new Pair<>(mote.getXPos(), mote.getYPos()));
                                locationhistorymap.put(mote, historymap);
                                if (mote.shouldSend()) {
                                    LinkedList<Byte> data = new LinkedList<>();
                                    for (MoteSensor sensor : mote.getSensors()) {
                                        data.add(sensor.getValue(mote.getXPos(), mote.getYPos(), getEnvironment().getTime()));
                                    }
                                    Byte[] dataByte = new Byte[data.toArray().length];
                                    data.toArray(dataByte);
                                    mote.sendToGateWay(dataByte, new HashMap<>());
                                }
                            } else waypoinMap.put(mote, waypoinMap.get(mote) + 1);
                        }
                    }
                }

            }

            arrived = true;
            for(Mote mote : environment.getMotes()){
                if(mote.isEnabled()) {
                    if (mote.getPath().size() != 0) {
                        if (Integer.signum(mote.getXPos() - environment.toMapXCoordinate(mote.getPath().getLast())) != 0 ||
                                Integer.signum(mote.getYPos() - environment.toMapYCoordinate(mote.getPath().getLast())) != 0) {
                            arrived = arrived && false;
                        }
                    }
                }
            }
            environment.tick(1);
        }

        for(Mote mote : environment.getMotes()){
            Pair<Integer,Integer> location = locationmap.get(mote);
            mote.setXPos(location.getLeft());
            mote.setYPos(location.getRight());
        }

        Timer timer = new Timer();
        AnimationTimerTask animationTimerTask = new AnimationTimerTask(locationhistorymap);
        timer.schedule(animationTimerTask,0,75/(1*speed));
        for(Mote mote : environment.getMotes()){
            Pair<Integer,Integer> location = locationmap.get(mote);
            mote.setXPos(location.getLeft());
            mote.setYPos(location.getRight());
        }
    }

    /**
     * Gets the probability with which a mote should be active from the input profile of the current simulation.
     * If no probability is specified, the probability is set to one.
     * Then it performs a pseudo-random choice and sets the mote to active/inactive for the next run, based on that probability.
     */

    private void calculateIfMotesAreActiveBasedOnInputProfile() {
        for(Mote mote: getEnvironment().getMotes()){
            Double activityProbability;
            if(getInputProfile().getProbabilitiesForMotesKeys().contains(getEnvironment().getMotes().indexOf(mote)))
                activityProbability = getInputProfile().getProbabilityForMote(getEnvironment().getMotes().indexOf(mote));
            else
                activityProbability = 1.0;
            mote.enable(Math.random() >= 1.0 - activityProbability);
        }
    }

    /**
     * A method for running the simulation as described in the inputProfile.
     */
    public void run(){

        getEnvironment().reset();

        calculateIfMotesAreActiveBasedOnInputProfile();


        for(int i =0; i< getInputProfile().getNumberOfRuns();i++) {

            gui.setProgress(i,getInputProfile().getNumberOfRuns());
            if(i != 0)
                getEnvironment().addRun();

            Boolean arrived = true;
            HashMap<Mote, Integer> waypoinMap = new HashMap<>();
            HashMap<Mote, LocalTime> timemap = new HashMap<>();
            HashMap<Mote, Pair<Integer, Integer>> locationmap = new HashMap<>();
            arrived = calculateMotesLocation(arrived, waypoinMap, timemap, locationmap);

            while (!arrived) {

                for (Mote mote : getEnvironment().getMotes()) {
                    if(mote.isEnabled()) {
                        if (Integer.signum(mote.getPath().size() - waypoinMap.get(mote)) > 0) {

                            if (1 / mote.getMovementSpeed() * 1000 < (getEnvironment().getTime().toNanoOfDay() - timemap.get(mote).toNanoOfDay()) / 100000 &&
                                    Long.signum(getEnvironment().getTime().toNanoOfDay() / 100000 - Math.abs(mote.getStartOffset()) * 100000) > 0) {
                                timemap.put(mote, getEnvironment().getTime());
                                if (Integer.signum(mote.getXPos() - getEnvironment().toMapXCoordinate(mote.getPath().get(waypoinMap.get(mote)))) != 0 ||
                                        Integer.signum(mote.getYPos() - getEnvironment().toMapYCoordinate(mote.getPath().get(waypoinMap.get(mote)))) != 0) {
                                    getEnvironment().moveMote(mote, mote.getPath().get(waypoinMap.get(mote)));
                                    if (mote.shouldSend()) {
                                        LinkedList<Byte> data = new LinkedList<>();
                                        for (MoteSensor sensor : mote.getSensors()) {
                                            data.add(sensor.getValue(mote.getXPos(), mote.getYPos(), getEnvironment().getTime()));
                                        }
                                        Byte[] dataByte = new Byte[data.toArray().length];
                                        data.toArray(dataByte);
                                        mote.sendToGateWay(dataByte, new HashMap<>());
                                    }
                                } else waypoinMap.put(mote, waypoinMap.get(mote) + 1);
                            }
                        }
                    }

                }

                arrived = true;
                for (Mote mote : environment.getMotes()) {
                    if(mote.isEnabled()) {
                        if (mote.getPath().getLast() != null) {
                            if (Integer.signum(mote.getXPos() - environment.toMapXCoordinate(mote.getPath().getLast())) != 0 ||
                                    Integer.signum(mote.getYPos() - environment.toMapYCoordinate(mote.getPath().getLast())) != 0) {
                                arrived = arrived && false;
                            }
                        }
                    }
                }
                environment.tick(1);
            }

            gui.setProgress(getInputProfile().getNumberOfRuns(),getInputProfile().getNumberOfRuns());
            for (Mote mote : environment.getMotes()) {
                Pair<Integer, Integer> location = locationmap.get(mote);
                mote.setXPos(location.getLeft());
                mote.setYPos(location.getRight());
            }
        }

    }

    /**
     *  Based on the Simulation information, calculates wether or not
     *  the motes have arrived or not arrived their next waypoint on the path.
     *  It also adds the information of the next timestamp and location point to the maps
     */

    private Boolean calculateMotesLocation(Boolean arrived, HashMap<Mote, Integer> waypoinMap, HashMap<Mote, LocalTime> timemap, HashMap<Mote, Pair<Integer, Integer>> locationmap) {
        for (Mote mote : getEnvironment().getMotes()) {
            timemap.put(mote, getEnvironment().getTime());
            locationmap.put(mote, new Pair<>(mote.getXPos(), mote.getYPos()));
            if (mote.getPath().size() != 0) {
                if (moteIsOnNextWayPoint(mote)) {
                    arrived = arrived && false;
                }
            }
            waypoinMap.put(mote, 0);
        }
        return arrived;
    }

    /**
     * checks if a mote is on its next waypoint based on coordinates.
     * @param mote
     * @return
     */

    private boolean moteIsOnNextWayPoint(Mote mote) {
        return Integer.signum(mote.getXPos() - getEnvironment().toMapXCoordinate(mote.getPath().getLast())) != 0 ||
                Integer.signum(mote.getYPos() - getEnvironment().toMapYCoordinate(mote.getPath().getLast())) != 0;
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
