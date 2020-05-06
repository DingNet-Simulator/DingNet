import gui.MainGUI;
import iot.SimulationRunner;
import iot.SimulationUpdateListener;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;
import util.MutableInteger;
import util.SettingsReader;
import util.time.DoubleTime;
import util.time.Time;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Simulator starter class.
 * If the simulator is executed with the option "-i" and the input profile is specified then
 * the simulator starts a timedRun simulation in batch mode (without GUI),
 * otherwise it starts the GUI
 */
public class Simulator {

    public static void main(String[] args) {

        Options options = new Options();

        Option inputFile = new Option("i", "inputFile", true, "path of configuration file");
        inputFile.setRequired(false);
        options.addOption(inputFile);

        CommandLine cmd = null;

        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("allowed arguments", options);
            System.exit(1);
        }

        SimulationRunner simulationRunner = SimulationRunner.getInstance();
        SettingsReader settingsReader = SettingsReader.getInstance();
        // methods to copy all the files with the configuration simulation from the
        // resource directory to the the directory in the user directory
        copyResourceDirectory(settingsReader.getConfigurationsResources(), settingsReader.getConfigurationsDirectory());

        if (cmd.hasOption("inputFile")) {
            var file = new File(cmd.getOptionValue("inputFile"));
            simulationRunner.loadConfigurationFromFile(file);
            simulationRunner.getSimulation().setInputProfile(simulationRunner.getInputProfiles().get(0));
            simulationRunner.setupTimedRun();
            var sec = new MutableInteger(5);
            simulationRunner.simulate(sec, new BatchSimulationUpdater(sec));
        } else {
            MainGUI.startGUI(simulationRunner);
        }
    }

    /**
     * Update listener for the simulation in batch mode
     */
    private static class BatchSimulationUpdater implements SimulationUpdateListener {

        private final MutableInteger rate;
        private Time time;

        public BatchSimulationUpdater(MutableInteger rate) {
            this.rate = rate;
            time = DoubleTime.zero();
        }

        @Override
        public void update() {
            time.plusSeconds(rate.intValue());
        }

        @Override
        public void onEnd() {

        }
    }

    // region copy resources
    // TODO improve error check and refresh file
    private static void copyResourceDirectory(@NotNull String source, @NotNull String destination) {
        var sourceStream = Simulator.class.getResourceAsStream(source);
        if (sourceStream == null) {
            throw new IllegalArgumentException("directory not found: " + source);
        }
        var dirDest = new File(destination);
        if (!dirDest.exists()) {
            if (!dirDest.mkdirs()) {
                throw new IllegalStateException("Impossible create directory: " + dirDest.getAbsolutePath());
            }
            try ( var reader = new BufferedReader(new InputStreamReader(sourceStream))) {
                reader.lines()
                    .filter(f -> f.endsWith(".xml"))
                    .forEach(f -> copyResourceFile(source + f, destination + f));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyResourceFile(@NotNull String source, @NotNull String destination) {
        var sourceStream = MainGUI.class.getResourceAsStream(source);
        if (sourceStream == null) {
            throw new IllegalArgumentException("file not found: " + source);
        }
        try (var reader = new BufferedReader(new InputStreamReader(sourceStream, StandardCharsets.UTF_8));
             var output = new OutputStreamWriter(new FileOutputStream(new File(destination)), StandardCharsets.UTF_8)) {

            reader.lines().forEach(l -> {
                try {
                    output.write(l);
                    output.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // endregion
}
