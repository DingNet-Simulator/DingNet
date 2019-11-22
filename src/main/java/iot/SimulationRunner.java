package iot;

import application.pollution.PollutionGrid;
import application.pollution.PollutionMonitor;
import application.routing.AStarRouter;
import application.routing.RoutingApplication;
import iot.mqtt.MQTTClientFactory;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.NetworkServer;
import org.jetbrains.annotations.NotNull;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;
import selfadaptation.feedbackloop.GenericFeedbackLoop;
import selfadaptation.feedbackloop.ReliableEfficientDistanceGateway;
import selfadaptation.feedbackloop.SignalBasedAdaptation;
import selfadaptation.instrumentation.MoteEffector;
import selfadaptation.instrumentation.MoteProbe;
import util.MutableInteger;
import util.Pair;
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
    private List<GenericFeedbackLoop> algorithms;
    private QualityOfService QoS;

    private Simulation simulation;
    private List<MoteProbe> moteProbe;

    private RoutingApplication routingApplication;
    private PollutionMonitor pollutionMonitor;
    private NetworkServer networkServer;



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

        // Loading all the algorithms
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

        networkServer = new NetworkServer(MQTTClientFactory.getSingletonInstance());
    }


    // region getters/setters

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

    public RoutingApplication getRoutingApplication() {
        return routingApplication;
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

    // endregion


    // region setup simulations

    public void setupSingleRun() {
        this.setupSingleRun(true);
    }

    public void setupSingleRun(boolean startFresh) {
        simulation.setupSingleRun(startFresh);

        this.setupSimulationRunner();
    }

    public void setupTimedRun() {
        simulation.setupTimedRun();

        this.setupSimulationRunner();
    }

    /**
     * Setup of applications/servers/clients before each run
     */
    private void setupSimulationRunner() {
        // Remove previous pollution measurements
        PollutionGrid.getInstance().clean();
        routingApplication.clean();

        // Reset received transmissions in the networkServer
        this.networkServer.reset();
    }

    // endregion


    // region simulations

    private boolean isSimulationFinished() {
        return simulation.isFinished();
    }

    public void simulate(MutableInteger updateFrequency, SimulationUpdateListener listener) {
        new Thread(() -> {
            long simulationStep = 0;
            while (!this.isSimulationFinished()) {
                this.simulation.simulateStep();

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


    @SuppressWarnings("unused")
    public void totalRun() {
        this.totalRun(o -> {});
    }

    public void totalRun(@NotNull Consumer<Pair<Integer, Integer>> fn) {
        int nrOfRuns = simulation.getInputProfile()
            .orElseThrow(() -> new IllegalStateException("No input profile selected before running the simulation"))
            .getNumberOfRuns();
        setupSingleRun(true);

        new Thread(() -> {
            fn.accept(new Pair<>(0, nrOfRuns));

            for (int i = 0; i < nrOfRuns; i++) {

                while (!simulation.isFinished()) {
                    this.simulation.simulateStep();
                }

                fn.accept(new Pair<>(i+1, nrOfRuns));

                if (i != nrOfRuns - 1) {
                    getEnvironment().addRun();
                    setupSingleRun(false);
                }
            }
        }).start();
    }

    // endregion


    // region loading/saving/cleanup

    void updateInputProfilesFile() {
        InputProfilesWriter.updateInputProfilesFile(inputProfiles);
    }

    private List<InputProfile> loadInputProfiles() {
        return InputProfilesReader.readInputProfiles();
    }

    public void loadConfigurationFromFile(File file) {
        this.cleanupSimulation();

        ConfigurationReader.loadConfiguration(file, simulation);

        for (Gateway gateway : simulation.getEnvironment().getGateways()) {
            for (int i = 0; i < algorithms.size(); i++) {
                gateway.addSubscription(moteProbe.get(i));
            }
        }

        setupApplications();
    }


    public void saveConfigurationToFile(File file) {
        ConfigurationWriter.saveConfigurationToFile(file, simulation);
    }


    public void saveSimulationToFile(File file) {
        SimulationWriter.saveSimulationToFile(file, simulation);
    }


    /**
     * Method which is called when a new configuration will be opened,
     * provides manual cleanup for old applications/servers/clients/...
     */
    private void cleanupSimulation() {
        if (this.pollutionMonitor != null) {
            this.pollutionMonitor.destruct();
        }
        if (this.routingApplication != null) {
            this.routingApplication.destruct();
        }

//        this.networkServer.reset();
        this.networkServer.reconnect();
    }

    private void setupApplications() {
        this.pollutionMonitor = new PollutionMonitor(this.getEnvironment());
        this.routingApplication = new RoutingApplication(new AStarRouter());
    }

    // endregion
}
