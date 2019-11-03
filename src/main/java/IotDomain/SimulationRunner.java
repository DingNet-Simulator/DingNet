package IotDomain;

import IotDomain.application.AStarRouter;
import IotDomain.application.Application;
import IotDomain.application.PollutionMonitor;
import IotDomain.application.RoutingApplication;
import IotDomain.networkentity.Gateway;
import IotDomain.networkentity.Mote;
import SelfAdaptation.AdaptationGoals.IntervalAdaptationGoal;
import SelfAdaptation.AdaptationGoals.ThresholdAdaptationGoal;
import SelfAdaptation.FeedbackLoop.GenericFeedbackLoop;
import SelfAdaptation.FeedbackLoop.ReliableEfficientDistanceGateway;
import SelfAdaptation.FeedbackLoop.SignalBasedAdaptation;
import SelfAdaptation.Instrumentation.MoteEffector;
import SelfAdaptation.Instrumentation.MoteProbe;
import util.Pair;
import util.xml.*;

import java.io.File;
import java.util.*;
import java.util.function.Function;

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
            .findFirst();

        if (selectedAlgorithm.isEmpty()) {
            throw new RuntimeException(String.format("Could not load approach with name %s", name));
        }
        simulation.setApproach(selectedAlgorithm.get());
    }

    public void updateQoS(QualityOfService QoS) {
        this.QoS.updateAdaptationGoals(QoS);
    }




    public void setupSingleRun() {
        simulation.setupSingleRun();
    }

    public void setupTimedRun() {
        simulation.setupTimedRun();
    }

    private boolean isSimulationFinished() {
        return simulation.isFinished();
    }

    public void simulate(int updateFrequency, SimulationUpdateListener listener) {
        new Thread(() -> {
            Map<Mote, Pair<Integer, Integer>> initialMotePositions = new HashMap<>();
            simulation.getEnvironment().getMotes()
                .forEach(m -> initialMotePositions.put(m, m.getPos()));

            long simulationStep = 0;
            while (!this.isSimulationFinished()) {
                this.simulateStep();

                // Visualize every x seconds
                if (simulationStep++ % (updateFrequency * 1000) == 0) {
                    listener.update();
                }
            }

            // Restore the initial positions after the run
            simulation.updateMotesLocation(initialMotePositions);
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

    public void totalRun(Function<Pair<Integer, Integer>, Void> fn) {
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
