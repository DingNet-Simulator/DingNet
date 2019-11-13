package iot;

import application.AStarRouter;
import application.PollutionMonitor;
import application.RoutingApplication;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;
import selfadaptation.feedbackLoop.GenericFeedbackLoop;
import selfadaptation.feedbackLoop.ReliableEfficientDistanceGateway;
import selfadaptation.feedbackLoop.SignalBasedAdaptation;
import selfadaptation.instrumentation.MoteEffector;
import selfadaptation.instrumentation.MoteProbe;
import util.MutableInteger;
import util.Pair;
import util.pollution.PollutionGrid;
import util.xml.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class SimulationRunner {
    private static SimulationRunner instance = null;

    private List<InputProfile> inputProfiles;
    private Simulation simulation;
    private List<GenericFeedbackLoop> algorithms;
    private List<MoteProbe> moteProbe;
    private QualityOfService QoS;
    private RoutingApplication routingApplication;
    private PollutionMonitor pollutionMonitor;


    public static SimulationRunner getInstance() {
        if (instance == null) {
            instance = new SimulationRunner();
        }

        return instance;
    }

    private SimulationRunner() {
        QoS = new QualityOfService(new HashMap<>());
        QoS.putAdaptationGoal("reliableCommunication", new IntervalAdaptationGoal(0.0, 0.0));
        QoS.putAdaptationGoal("energyConsumption", new ThresholdAdaptationGoal(0.0));
        QoS.putAdaptationGoal("collisionBound", new ThresholdAdaptationGoal(0.0));

        simulation = new Simulation();
        inputProfiles = loadInputProfiles();


        /*
         * Loading all the algorithms
         */
        GenericFeedbackLoop noAdaptation = new GenericFeedbackLoop("No Adaptation") {
            @Override
            public void adapt(Mote mote, Gateway gateway) {

            }
        };

        algorithms = new ArrayList<>();
        algorithms.add(noAdaptation);

        SignalBasedAdaptation signalBasedAdaptation = new SignalBasedAdaptation(QoS);
        algorithms.add(signalBasedAdaptation);

        ReliableEfficientDistanceGateway reliableEfficientDistanceGateway = new ReliableEfficientDistanceGateway();
        algorithms.add(reliableEfficientDistanceGateway);


        /*
         * Setting the mote probes
         */
        moteProbe = new LinkedList<>();
        List<MoteEffector> moteEffector = new LinkedList<>();
        for (int i = 0; i < algorithms.size(); i++) {
            moteProbe.add(new MoteProbe());
            moteEffector.add(new MoteEffector());
        }
        for (GenericFeedbackLoop feedbackLoop : algorithms) {
            feedbackLoop.setMoteProbe(moteProbe.get(algorithms.indexOf(feedbackLoop)));
            feedbackLoop.setMoteEffector(moteEffector.get(algorithms.indexOf(feedbackLoop)));
        }
    }


    public Environment getEnvironment() {
        return simulation.getEnvironment();
    }

    public List<InputProfile> getInputProfiles() {
        return inputProfiles;
    }

    public List<GenericFeedbackLoop> getAlgorithms() {
        return algorithms;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public QualityOfService getQoS() {
        return QoS;
    }


    public void setApproach(String name) {
        var selectedAlgorithm = algorithms.stream()
            .filter(o -> o.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("Could not load approach with name %s", name)));

        simulation.setApproach(selectedAlgorithm);
    }

    public void updateQoS(QualityOfService QoS) {
        this.QoS.updateAdaptationGoals(QoS);
    }




    public void setupSingleRun() {
        simulation.setupSingleRun();

        PollutionGrid.getInstance().clean();
        routingApplication.clean();
    }

    public void setupTimedRun() {
        simulation.setupTimedRun();
    }

    private boolean isSimulationFinished() {
        return simulation.isFinished();
    }

    public void simulate(MutableInteger updateFrequency, SimulationUpdateListener listener) {
        new Thread(() -> {
            long simulationStep = 0;
            while (!this.isSimulationFinished()) {
                this.simulateStep();

                // Visualize every x seconds
                if (simulationStep++ % (updateFrequency.intValue() * 1000) == 0) {
                    listener.update();
                }
            }

            // Restore the initial positions after the run
            listener.update();
            listener.onEnd();
        }).start();
    }

    private void simulateStep() {
        simulation.simulateStep();
    }


    @SuppressWarnings("unused")
    public void totalRun() {
        totalRun(null);
    }

    public void totalRun(Consumer<Pair<Integer, Integer>> fn) {
        simulation.multipleRuns(fn);
    }






    void updateInputProfilesFile() {
        InputProfilesWriter.updateInputProfilesFile(inputProfiles);
    }

    private List<InputProfile> loadInputProfiles() {
        return InputProfilesReader.readInputProfiles();
    }

    public void loadConfigurationFromFile(File file) {
        ConfigurationReader.loadConfiguration(file, simulation);

        for (Gateway gateway : simulation.getEnvironment().getGateways()) {
            for (int i = 0; i < algorithms.size(); i++) {
                gateway.addSubscription(moteProbe.get(i));
            }
        }

        // Setup of user applications
        this.pollutionMonitor = new PollutionMonitor(this.getEnvironment());
        this.routingApplication = new RoutingApplication(new AStarRouter());
    }


    public void saveConfigurationToFile(File file) {
        ConfigurationWriter.saveConfigurationToFile(file, simulation);
    }


    public void saveSimulationToFile(File file) {
        SimulationWriter.saveSimulationToFile(file, simulation);
    }

    public RoutingApplication getRoutingApplication() {
        return routingApplication;
    }
}
