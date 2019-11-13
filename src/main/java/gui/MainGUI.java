package gui;


import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gui.mapviewer.*;
import gui.util.*;
import iot.*;
import iot.lora.LoraTransmission;
import iot.networkentity.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;
import selfadaptation.adaptationgoals.IntervalAdaptationGoal;
import selfadaptation.adaptationgoals.ThresholdAdaptationGoal;
import selfadaptation.feedbackLoop.GenericFeedbackLoop;
import util.MapHelper;
import util.MutableInteger;
import util.Pair;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MainGUI extends JFrame implements SimulationUpdateListener {
    private JPanel map;
    private JPanel console;
    private JPanel mainPanel;
    private JToolBar toolBarEnvironment;
    private JToolBar toolBarAdaptation;
    private JTabbedPane tabbedPaneGraphs;
    private JButton openConfigurationButton;
    private JButton configureButton;
    private JButton helpButton;
    private JButton aboutButton;
    private JButton simulationSaveButton;
    private JButton totalRunButton;
    private JLabel centerLabel;
    private JPanel entitesPanel;
    private JScrollPane entitiesPane;
    private JPanel receivedPowerGraph;
    private JPanel powerSettingGraph;
    private JPanel spreadingFactorGraph;
    private JButton saveConfigurationButton;
    private JPanel usedEnergyGraph;
    private JButton moteApplicationButton;
    private JButton regionButton;
    private JTabbedPane tabbedPane1;
    private JButton moteCharacteristicsButton;
    private JLabel moteCharacteristicsLabel;
    private JLabel moteApplicationLabel;
    private JPanel particulateMatterPanel;
    private JPanel carbonDioxideField;
    private JPanel sootPanel;
    private JPanel ozonePanel;
    private JProgressBar totalRunProgressBar;
    private JPanel distanceToGatewayGraph;
    private JPanel InputProfilePanel;
    private JLabel progressLabel;
    private JButton singleRunButton;
    private JComboBox<String> adaptationComboBox;
    private JSlider speedSlider;
    private JButton clearButton;
    private JButton editRelComButton;
    private JButton editEnConButton;
    private JButton editColBoundButton;
    private JLabel relComLabel;
    private JLabel enConLabel;
    private JLabel colBoundLabel;
    private JButton resultsButton;
    private JButton timedRunButton;

    private static JXMapViewer mapViewer = new JXMapViewer();
    // Create a TileFactoryInfo for OpenStreetMap
    private static TileFactoryInfo info = new OSMTileFactoryInfo();
    private static DefaultTileFactory tileFactory = new DefaultTileFactory(info);

    private SimulationRunner simulationRunner;
    private MutableInteger simulationSpeed;

    private double usedEnergy;
    private int packetsSent;
    private int packetsLost;



    public static void main(String[] args) {
        SimulationRunner simulationRunner = SimulationRunner.getInstance();

        SwingUtilities.invokeLater(() -> {
            mapViewer.setTileFactory(tileFactory);
            tileFactory.setThreadPoolSize(GUISettings.THREADPOOLSIZE);

            if (GUISettings.USE_MAP_CACHING) {
                File cache = new File(GUISettings.PATH_CACHE_TILEFACTORY);
                tileFactory.setLocalCache(new FileBasedLocalCache(cache, false));
            }

            JFrame frame = new JFrame("Dynamic DingNet simulator");
            MainGUI gui = new MainGUI(simulationRunner);
            frame.setContentPane(gui.mainPanel);

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();

            gui.updateInputProfiles();
            gui.loadAlgorithms();
            frame.setVisible(true);
            frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        });
    }





    public MainGUI(SimulationRunner simulationRunner) {
        this.simulationRunner = simulationRunner;
        this.simulationSpeed = new MutableInteger(GUISettings.BASE_VISUALIZATION_SPEED);

        updateInputProfiles();
        updateAdaptationGoals();

        resultsButton.setEnabled(false);
        editColBoundButton.setEnabled(false);
        editEnConButton.setEnabled(false);
        editRelComButton.setEnabled(false);
        configureButton.setEnabled(false);
        saveConfigurationButton.setEnabled(false);


        // ===========================================
        // Action listeners for the buttons in the GUI
        // ===========================================

        configureButton.addActionListener(new ConfigureActionListener(this));
        moteCharacteristicsButton.addActionListener(new MoteSelectActionListener(this));
        moteApplicationButton.addActionListener(new MoteApplicationSelectActionListener(this));
        editRelComButton.addActionListener(new ComRelActionListener(this));
        editColBoundButton.addActionListener(new ColBoundActionListener(this));
        editEnConButton.addActionListener(new EnConActionListener(this));
        openConfigurationButton.addActionListener(new OpenConfigurationListener());
        saveConfigurationButton.addActionListener(new SaveConfigurationListener());
        simulationSaveButton.addActionListener(new SaveSimulationResultListener());

        regionButton.addActionListener(e -> this.setApplicationGraphs(simulationRunner.getEnvironment()));


        singleRunButton.addActionListener(e -> {
            if (simulationRunner.getSimulation().getInputProfile().isEmpty()) {
                showNoInputProfileSelectedError();
                return;
            }

            this.setEnabledRunButtons(false);
            simulationRunner.setupSingleRun();

            simulationRunner.simulate(simulationSpeed, this);
        });

        timedRunButton.addActionListener(e -> {
            if (simulationRunner.getSimulation().getInputProfile().isEmpty()) {
                showNoInputProfileSelectedError();
                return;
            }

            this.setEnabledRunButtons(false);
            simulationRunner.setupTimedRun();

            simulationRunner.simulate(simulationSpeed, this);
        });

        totalRunButton.addActionListener(e -> {
            if (simulationRunner.getSimulation().getInputProfile().isEmpty()) {
                showNoInputProfileSelectedError();
                return;
            }

            this.setEnabledRunButtons(false);

            simulationRunner.totalRun(this::setProgress);
        });


        adaptationComboBox.addActionListener((ActionEvent e) -> {
            String chosenOption = (String) adaptationComboBox.getSelectedItem();
            simulationRunner.setApproach(chosenOption);
        });

        clearButton.addActionListener((ActionEvent e) -> {
            moteCharacteristicsLabel.setText("");
            moteApplicationLabel.setText("");
            resultsButton.setEnabled(false);

            List.of(receivedPowerGraph, powerSettingGraph, spreadingFactorGraph, usedEnergyGraph, distanceToGatewayGraph,
                ozonePanel, sootPanel, carbonDioxideField, particulateMatterPanel)
                .forEach((JPanel panel) -> {
                    panel.removeAll();
                    panel.repaint();
                    panel.revalidate();
                });
        });

        helpButton.addActionListener((ActionEvent e) -> {
            HelpGUI helpgui = new HelpGUI();
            helpgui.pack();
            helpgui.setVisible(true);
        });

        aboutButton.addActionListener((ActionEvent e) -> {
            AboutSimDialog aboutSimDialog = new AboutSimDialog();
            aboutSimDialog.pack();
            aboutSimDialog.setVisible(true);
        });

        resultsButton.addActionListener((ActionEvent e) -> {
            MoteCharacteristicsDialog moteCharacteristicsDialog =
                new MoteCharacteristicsDialog(usedEnergy, packetsSent, packetsLost);
            moteCharacteristicsDialog.pack();
            moteCharacteristicsDialog.setVisible(true);
        });

        speedSlider.addChangeListener(
            e -> this.simulationSpeed.setValue(GUISettings.BASE_VISUALIZATION_SPEED * speedSlider.getValue())
        );
    }


    public Environment getEnvironment() {
        return simulationRunner.getEnvironment();
    }


    private void loadAlgorithms() {
        var algorithms = simulationRunner.getAlgorithms();

        DefaultComboBoxModel<String> adaptationComboBoxModel = new DefaultComboBoxModel<>();
        for (GenericFeedbackLoop algorithm : algorithms) {
            adaptationComboBoxModel.addElement(algorithm.getName());
        }

        adaptationComboBox.setModel(adaptationComboBoxModel);
    }


    private void updateEntries(Environment environment) {
        entitesPanel.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 0, 2, 0);
        JTextArea textArea;

        for (Gateway gateway : environment.getGateways()) {
            textArea = new JTextArea();
            textArea.append(String.format("Gateway %d:\n", environment.getGateways().indexOf(gateway) + 1));
            textArea.append(String.format("EUID: %d\n", gateway.getEUI()));

            double latitude = environment.toLatitude(gateway.getYPosInt());
            double longitude = environment.toLongitude(gateway.getXPosInt());
            textArea.append(String.format("%s%s, %s%s",
                MapHelper.getDirectionSign(latitude, "lat"), MapHelper.toDegreeMinuteSecond(latitude),
                MapHelper.getDirectionSign(longitude, "long"), MapHelper.toDegreeMinuteSecond(longitude)));

            for (int i = 0; i < textArea.getMouseListeners().length; i++) {
                textArea.removeMouseListener(textArea.getMouseListeners()[i]);
            }

            textArea.addMouseListener(gateWayMouse);
            entitesPanel.add(textArea, constraints);
        }

        for (Mote mote : environment.getMotes()) {
            textArea = new JTextArea();
            textArea.append(String.format("Mote %d:\n", environment.getMotes().indexOf(mote) + 1));
            textArea.append(String.format("EUID: %d\n", mote.getEUI()));

            double latitude = environment.toLatitude(mote.getYPosInt());
            double longitude = environment.toLongitude(mote.getXPosInt());
            textArea.append(String.format("%s%s, %s%s",
                MapHelper.getDirectionSign(latitude, "lat"), MapHelper.toDegreeMinuteSecond(latitude),
                MapHelper.getDirectionSign(longitude, "long"), MapHelper.toDegreeMinuteSecond(longitude)));

            for (int i = 0; i < textArea.getMouseListeners().length; i++) {
                textArea.removeMouseListener(textArea.getMouseListeners()[i]);
            }
            textArea.addMouseListener(moteMouse);
            entitesPanel.add(textArea, constraints);
        }
        entitesPanel.repaint();
        entitesPanel.revalidate();
    }


    private void updateInputProfiles() {
        InputProfilePanel.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(2, 0, 2, 0);
        constraints.weighty = 0;
        JPanel panel;

        Optional<InputProfile> selectedInputProfile = simulationRunner.getSimulation().getInputProfile();

        for (InputProfile inputProfile : simulationRunner.getInputProfiles()) {
            panel = new JPanel(new BorderLayout());
            panel.setPreferredSize(new Dimension(InputProfilePanel.getWidth() - 10, 50));
            panel.setBackground(Color.white);
            panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            JPanel subPanel1 = new JPanel();
            subPanel1.setOpaque(false);
            JPanel subPanel2 = new JPanel();
            subPanel2.setOpaque(false);

            if (selectedInputProfile.isPresent() && inputProfile == selectedInputProfile.get()) {
                subPanel1.add(new JLabel(new ImageIcon(ImageLoader.IMAGE_CIRCLE_SELECTED)));
            } else {
                subPanel1.add(new JLabel(new ImageIcon(ImageLoader.IMAGE_CIRCLE_UNSELECTED)));
            }
            subPanel1.add(new JLabel(inputProfile.getName()));
            panel.add(subPanel1, BorderLayout.WEST);
            subPanel2.add(new JLabel(new ImageIcon(ImageLoader.IMAGE_EDIT_ICON)));
            panel.add(subPanel2, BorderLayout.EAST);

            subPanel2.addMouseListener(new InputProfileEditMouse(inputProfile));
            subPanel1.addMouseListener(new InputProfileSelectMouse(inputProfile));
            InputProfilePanel.add(panel, constraints);
        }

        panel = new JPanel();
        constraints.weighty = 0.9;
        InputProfilePanel.add(panel, constraints);
        InputProfilePanel.repaint();
        InputProfilePanel.revalidate();
    }

    private void updateAdaptationGoals() {
        QualityOfService QoS = simulationRunner.getQoS();

        relComLabel.setText(String.format("Interval: %s", QoS.getAdaptationGoal("reliableCommunication").toString()));
        enConLabel.setText(String.format("Threshold: %s", QoS.getAdaptationGoal("energyConsumption").toString()));
        colBoundLabel.setText(String.format("Threshold: %.2f", Double.parseDouble(QoS.getAdaptationGoal("collisionBound").toString()) * 100));
    }


    private void loadMap(boolean refresh) {
        Environment environment = simulationRunner.getEnvironment();

        if (!refresh) {
            mapViewer.setAddressLocation(environment.getMapCenter());
            mapViewer.setZoom(5);
        }

        mapViewer.setOverlayPainter(new CompoundPainterBuilder()
            .withPollutionGrid(environment)
            .withRoutingPath(environment, simulationRunner.getRoutingApplication())
            .withMotePaths(environment)
            .withMotes(environment)
            .withGateways(environment)
            .build()
        );

        map.add(mapViewer);

        double latitude = environment.getMapCenter().getLatitude();
        double longitude = environment.getMapCenter().getLongitude();
        centerLabel.setText(" " + MapHelper.getDirectionSign(latitude, "lat") +
                MapHelper.toDegreeMinuteSecond(latitude).toString() + ", " +
                MapHelper.getDirectionSign(longitude, "long") +
                MapHelper.toDegreeMinuteSecond(longitude).toString());
    }


    public void refresh() {
        refreshMap();
        updateEntries(simulationRunner.getEnvironment());

    }

    private void refreshMap() {
        loadMap(true);
    }


    private void showNoInputProfileSelectedError() {
        JOptionPane.showMessageDialog(null, "Make sure to have an input profile selected before running the simulator.",
            "Warning: no input profile selected", JOptionPane.ERROR_MESSAGE);
    }

    private MouseAdapter gateWayMouse = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            JTextArea jTextArea = (JTextArea) e.getSource();
            String text = jTextArea.getText();
            int index = Integer.parseInt(text.substring(8, text.indexOf(":")));
            if (e.getClickCount() == 2) {
                JFrame frame = new JFrame("Gateway settings");
                GatewayGUI gatewayGUI = new GatewayGUI(simulationRunner.getEnvironment().getGateways().get(index - 1));
                frame.setContentPane(gatewayGUI.getMainPanel());
                frame.setPreferredSize(new Dimension(600, 400));
                frame.setMinimumSize(new Dimension(600, 400));
                frame.setVisible(true);
            }
        }
    };

    private MouseAdapter moteMouse = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            JTextArea jTextArea = (JTextArea) e.getSource();
            String text = jTextArea.getText();
            int index = Integer.parseInt(text.substring(5, text.indexOf(":")));
            if (e.getClickCount() == 2) {
                JFrame frame = new JFrame("Mote settings");
                MoteGUI moteGUI = new MoteGUI(simulationRunner.getEnvironment().getMotes().get(index - 1), frame);
                frame.setContentPane(moteGUI.getMainPanel());
                frame.setPreferredSize(new Dimension(600, 400));
                frame.setMinimumSize(new Dimension(600, 400));
                frame.setVisible(true);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        refresh();
                    }
                });
            }
            if (e.getClickCount() == 1) {
                setCharacteristics(index - 1, 0);
            }
        }

    };


    class MoteSelectActionListener implements ActionListener {
        private MainGUI mainGui;

        MoteSelectActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Select Mote");
            SelectMoteGUI selectMoteGUI = new SelectMoteGUI(simulationRunner.getEnvironment(), mainGui, frame);
            frame.setContentPane(selectMoteGUI.getMainPanel());
            frame.setPreferredSize(new Dimension(750, 750));
            frame.setMinimumSize(new Dimension(750, 750));
            frame.setVisible(true);
        }
    }

    class ComRelActionListener implements ActionListener {
        private MainGUI mainGui;

        ComRelActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            QualityOfService QoS = simulationRunner.getQoS();
            JFrame frame = new JFrame("Change reliable communication goal");
            EditRelComGUI editRelComGUI = new EditRelComGUI(((IntervalAdaptationGoal) QoS.getAdaptationGoal("reliableCommunication")), mainGui, frame);
            frame.setContentPane(editRelComGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class EnConActionListener implements ActionListener {
        private MainGUI mainGui;

        EnConActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            QualityOfService QoS = simulationRunner.getQoS();
            JFrame frame = new JFrame("Change energy consumption goal");
            EditEnConGUI editEnConGUI = new EditEnConGUI((ThresholdAdaptationGoal) QoS.getAdaptationGoal("energyConsumption"), mainGui, frame);
            frame.setContentPane(editEnConGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class ColBoundActionListener implements ActionListener {
        private MainGUI mainGui;

        ColBoundActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            QualityOfService QoS = simulationRunner.getQoS();
            JFrame frame = new JFrame("Change collision boundary goal");
            EditColBoundGUI editColBoundGUI = new EditColBoundGUI((ThresholdAdaptationGoal) QoS.getAdaptationGoal("collisionBound"), mainGui, frame);
            frame.setContentPane(editColBoundGUI.getMainPanel());
            frame.pack();
            frame.setVisible(true);
        }
    }

    class MoteApplicationSelectActionListener implements ActionListener {
        private MainGUI mainGui;

        MoteApplicationSelectActionListener(MainGUI mainGui) {
            this.mainGui = mainGui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Select Mote");
            SelectMoteApplicationGUI selectMoteApplicationGUI =
                new SelectMoteApplicationGUI(simulationRunner.getEnvironment(), mainGui, frame);
            frame.setContentPane(selectMoteApplicationGUI.getMainPanel());
            frame.setPreferredSize(new Dimension(600, 400));
            frame.setMinimumSize(new Dimension(600, 400));
            frame.setVisible(true);
        }
    }

    private class InputProfileEditMouse extends MouseAdapter {

        private InputProfile inputProfile;

        InputProfileEditMouse(InputProfile inputProfile) {
            this.inputProfile = inputProfile;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (simulationRunner.getEnvironment() != null) {
                JFrame frame = new JFrame("Edit input profile");
                EditInputProfileGUI EditInputProfileGUI =
                    new EditInputProfileGUI(inputProfile, simulationRunner.getEnvironment());
                frame.setContentPane(EditInputProfileGUI.getMainPanel());
                frame.setPreferredSize(new Dimension(750, 400));
                frame.setMinimumSize(new Dimension(750, 400));
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Load a configuration before editing an input profile", "InfoBox: Edit InputProfile", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }


    private class InputProfileSelectMouse extends MouseAdapter {
        private InputProfile inputProfile;

        InputProfileSelectMouse(InputProfile inputProfile) {
            this.inputProfile = inputProfile;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (simulationRunner.getEnvironment() != null) {
                Simulation simulation = simulationRunner.getSimulation();
                Optional<InputProfile> currentProfile = simulation.getInputProfile();

                if (currentProfile.isPresent() && inputProfile == currentProfile.get()) {
                    simulation.setInputProfile(null);
                    editRelComButton.setEnabled(false);
                    editColBoundButton.setEnabled(false);
                    editEnConButton.setEnabled(false);
                } else {
                    // Adjust the used inputprofile and QoS in the simulation
                    simulation.setInputProfile(inputProfile);
                    simulationRunner.updateQoS(inputProfile.getQualityOfServiceProfile());

                    Set<String> qualityNames = inputProfile.getQualityOfServiceProfile().getNames();
                    editRelComButton.setEnabled(qualityNames.contains("reliableCommunication"));
                    editEnConButton.setEnabled(qualityNames.contains("energyConsumption"));
                    editColBoundButton.setEnabled(qualityNames.contains("collisionBound"));
                }
                updateInputProfiles();
                updateAdaptationGoals();
            } else {
                JOptionPane.showMessageDialog(null, "Load a configuration before selecting an input profile",
                    "InfoBox: Select InputProfile", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Sets the graphs of the corresponding characteristics of a given mote of a given run.
     * @param moteIndex The index of the mote.
     * @param run       The number of the run.
     */
    void setCharacteristics(int moteIndex, int run) {
        moteCharacteristicsLabel.setText("Mote " + (moteIndex + 1) + " | Run " + (run + 1));
        resultsButton.setEnabled(true);

        BiConsumer<JPanel, ChartPanel> updateGraph = (p, c) -> {
            p.removeAll();
            p.add(c);
            p.repaint();
            p.revalidate();
        };

        updateGraph.accept(receivedPowerGraph, generateReceivedPowerGraphForMotes(simulationRunner.getEnvironment().getMotes().get(moteIndex), run));
        updateGraph.accept(usedEnergyGraph, generateUsedEnergyGraph(simulationRunner.getEnvironment().getMotes().get(moteIndex), run));
        updateGraph.accept(powerSettingGraph, generatePowerSettingGraph(simulationRunner.getEnvironment().getMotes().get(moteIndex), run));
        updateGraph.accept(spreadingFactorGraph, generateSpreadingFactorGraph(simulationRunner.getEnvironment().getMotes().get(moteIndex), run));
        updateGraph.accept(distanceToGatewayGraph, generateDistanceToGatewayGraph(simulationRunner.getEnvironment().getMotes().get(moteIndex), run));
    }

    void setApplicationGraphs(int index) {
        moteApplicationLabel.setText("Mote " + (index + 1));

        BiConsumer<JPanel, Function<Mote, ChartPanel>> updateGraph = (p, f) -> {
            p.removeAll();
            p.add(f.apply(simulationRunner.getEnvironment().getMotes().get(index)));
            p.repaint();
            p.revalidate();
        };

        updateGraph.accept(particulateMatterPanel, this::generateParticulateMatterGraph);
        updateGraph.accept(carbonDioxideField, this::generateCarbonDioxideGraph);
        updateGraph.accept(sootPanel, this::generateSootGraph);
        updateGraph.accept(ozonePanel, this::generateOzoneGraph);
    }


    private void setApplicationGraphs(Environment environment) {
        moteApplicationLabel.setText("Region 1");

        BiConsumer<JPanel, Pair<JPanel, JComponent>> updateGraph = (p, r) -> {
            p.removeAll();
            p.add(r.getLeft());
            p.add(r.getRight(), BorderLayout.EAST);
            p.repaint();
            p.revalidate();
        };

        updateGraph.accept(particulateMatterPanel, generateParticulateMatterGraph(environment));
        updateGraph.accept(carbonDioxideField, generateCarbonDioxideGraph(environment));
        updateGraph.accept(sootPanel, generateSootGraph(environment));
        updateGraph.accept(ozonePanel, generateOzoneGraph(environment));
    }

    private Pair<JPanel, JComponent> createHeatChart(LinkedList<Pair<GeoPosition, Double>> dataSet, Environment environment) {

        // create a paint-scale and a legend showing it
        SpectrumPaintScale paintScale = new SpectrumPaintScale(80, 100);

        PaintScaleLegend psl = new PaintScaleLegend(paintScale, new NumberAxis());
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
        psl.setMargin(50.0, 20.0, 80.0, 0.0);

        XYPlot plot = new XYPlot();
        JFreeChart chart = new JFreeChart(null, null, plot, false);
        chart.addSubtitle(psl);
        ImageIcon icon = new ImageIcon(chart.createBufferedImage(400, 300));
        Image image = icon.getImage();
        image = createImage(new FilteredImageSource(image.getSource(),
            new CropImageFilter(350, 40, 50, 220)));
        ImageIcon newIcon = new ImageIcon(image);
        JLabel jLabel = new JLabel(newIcon);
        jLabel.setMinimumSize(new Dimension(0, 0));

        // finally a renderer and a plot

        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);
        mapViewer.setAddressLocation(environment.getMapCenter());
        mapViewer.setZoom(5);
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
        for (Pair<GeoPosition, Double> data : dataSet) {
            compoundPainter.addPainter(new SensorDataPainter(data.getLeft(), paintScale.getPaint(data.getRight())));
        }
        mapViewer.setOverlayPainter(compoundPainter);


        return new Pair<>(mapViewer, jLabel);

    }


    @SuppressWarnings("SuspiciousNameCombination")
    private Pair<JPanel, JComponent> generateGraph(Environment environment, MoteSensor moteSensor, String keyName) {
        DefaultXYZDataset data = new DefaultXYZDataset();
        HashMap<Pair<Integer, Integer>, LinkedList<Double>> seriesList = new HashMap<>();
        LinkedList<Pair<GeoPosition, Double>> dataSet = new LinkedList<>();

        for (Mote mote : simulationRunner.getEnvironment().getMotes()) {
            if (mote.getSensors().contains(moteSensor)) {
                for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                    int xPos = transmission.getXPos();
                    int yPos = transmission.getYPos();
                    for (Pair<Integer, Integer> key : seriesList.keySet()) {
                        if (Math.sqrt(Math.pow(key.getLeft() - xPos, 2) + Math.pow(key.getRight() - yPos, 2)) < 300) {
                            xPos = key.getLeft();
                            yPos = key.getRight();
                        }
                    }
                    if (seriesList.containsKey(new Pair<>(xPos, yPos))) {
                        seriesList.get(new Pair<>(xPos, yPos)).add(moteSensor.getValue(xPos, yPos));
                    } else {
                        LinkedList<Double> dataList = new LinkedList<>();
                        dataList.add(moteSensor.getValue(xPos, yPos));
                        seriesList.put(new Pair<>(xPos, yPos), dataList);
                    }
                }
            }
        }
        for (Pair<Integer, Integer> key : seriesList.keySet()) {
            double average = seriesList.get(key).stream()
                .mapToDouble(o -> o)
                .average()
                .orElse(0.0);

            seriesList.get(key).clear();
            seriesList.get(key).add(average);
            dataSet.add(new Pair<>(new GeoPosition(environment.toLatitude(key.getRight()), environment.toLongitude(key.getLeft())), average));
        }

        double[][] seriesParticulateMatter = new double[3][seriesList.size()];
        int i = 0;
        for (Pair<Integer, Integer> key : seriesList.keySet()) {
            seriesParticulateMatter[0][i] = key.getLeft();
            seriesParticulateMatter[1][i] = key.getRight();
            seriesParticulateMatter[2][i] = seriesList.get(key).get(0);
            i++;
        }
        data.addSeries(keyName, seriesParticulateMatter);
        return createHeatChart(dataSet, environment);
    }

    private ChartPanel generateGraph(Mote mote, MoteSensor moteSensor, String keyName) {
        XYSeriesCollection dataSoot = new XYSeriesCollection();
        int i = 0;
        XYSeries series = new XYSeries(keyName);
        if (mote.getSensors().contains(moteSensor)) {
            for (LoraTransmission transmission : mote.getSentTransmissions(mote.getEnvironment().getNumberOfRuns() - 1)) {
                series.add(i * 10, moteSensor.getValue(transmission.getXPos(), transmission.getYPos()));
                i = i + 1;
            }
            dataSoot.addSeries(series);
        }

        JFreeChart chart = ChartFactory.createScatterPlot(
            null, // chart title
            "Distance traveled in meter", // x axis label
            keyName, // y axis label
            dataSoot, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesShape(0, shape);
        return new ChartPanel(chart);
    }

    private ChartPanel generateParticulateMatterGraph(Mote mote) {
        return this.generateGraph(mote, MoteSensor.PARTICULATE_MATTER, "Particulate Matter");
    }

    private Pair<JPanel, JComponent> generateParticulateMatterGraph(Environment environment) {
        return this.generateGraph(environment, MoteSensor.PARTICULATE_MATTER, "Particulate Matter");
    }

    private ChartPanel generateCarbonDioxideGraph(Mote mote) {
        return this.generateGraph(mote, MoteSensor.CARBON_DIOXIDE, "Carbon Dioxide");
    }

    private Pair<JPanel, JComponent> generateCarbonDioxideGraph(Environment environment) {

        return this.generateGraph(environment, MoteSensor.CARBON_DIOXIDE, "Carbon Dioxide");
    }

    private ChartPanel generateSootGraph(Mote mote) {
        return this.generateGraph(mote, MoteSensor.SOOT, "Soot");
    }

    private Pair<JPanel, JComponent> generateSootGraph(Environment environment) {
        return this.generateGraph(environment, MoteSensor.SOOT, "Soot");
    }

    private ChartPanel generateOzoneGraph(Mote mote) {
        return this.generateGraph(mote, MoteSensor.OZONE, "Ozone");
    }

    private Pair<JPanel, JComponent> generateOzoneGraph(Environment environment) {
        return this.generateGraph(environment, MoteSensor.OZONE, "Ozone");
    }

    private class ConfigureActionListener implements ActionListener {
        private MainGUI gui;

        ConfigureActionListener(MainGUI gui) {
            this.gui = gui;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Configure configuration");
            ConfigureGUI configureGUI = new ConfigureGUI(gui, frame);
            frame.setContentPane(configureGUI.getMainPanel());
            frame.setPreferredSize(new Dimension(700, 600));
            frame.setMinimumSize(new Dimension(700, 600));
            frame.setVisible(true);

        }
    }

    private class OpenConfigurationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Load a configuration");
            fc.setFileFilter(new FileNameExtensionFilter("xml configuration", "xml"));

            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String basePath = file.getParentFile().getParent();
            fc.setCurrentDirectory(new File(Paths.get(basePath, "settings", "configurations").toUri()));

            int returnVal = fc.showOpenDialog(mainPanel);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                JFrame frame = new JFrame("Loading configuration");
                LoadingGUI loadingGUI = new LoadingGUI();
                frame.setContentPane(loadingGUI.getMainPanel());
                frame.setMinimumSize(new Dimension(300, 300));
                frame.setVisible(true);

                simulationRunner.loadConfigurationFromFile(fc.getSelectedFile());

                frame.dispose();

                updateEntries(simulationRunner.getEnvironment());
                loadMap(false);

                MouseInputListener mia = new PanMouseInputListener(mapViewer);
                mapViewer.addMouseListener(mia);
                mapViewer.addMouseMotionListener(mia);
                mapViewer.addMouseListener(new CenterMapListener(mapViewer));
                mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));

                mapViewer.addPropertyChangeListener("zoom", (e) -> refresh());
                mapViewer.addPropertyChangeListener("center", (e) -> refresh());

                configureButton.setEnabled(true);
                saveConfigurationButton.setEnabled(true);
            }
        }
    }

    private class SaveConfigurationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save a configuration");
            fc.setFileFilter(new FileNameExtensionFilter("xml configuration", "xml"));

            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String basePath = file.getParentFile().getParent();
            fc.setCurrentDirectory(new File(Paths.get(basePath, "settings", "configurations").toUri()));

            int returnVal = fc.showSaveDialog(mainPanel);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = GUIUtil.getOutputFile(fc.getSelectedFile(), "xml");
                simulationRunner.saveConfigurationToFile(file);
            }
        }
    }

    private class SaveSimulationResultListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Save output");
            fc.setFileFilter(new FileNameExtensionFilter("xml output", "xml"));

            File file = new File(MainGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            file = new File(file.getParent());
            fc.setCurrentDirectory(file);

            int returnVal = fc.showSaveDialog(mainPanel);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = GUIUtil.getOutputFile(fc.getSelectedFile(), "xml");
                simulationRunner.saveSimulationToFile(file);
            }
        }
    }

    private void setProgress(Pair<Integer, Integer> progress) {
        totalRunProgressBar.setMinimum(0);
        totalRunProgressBar.setMaximum(progress.getRight());
        totalRunProgressBar.setValue(progress.getLeft());
        progressLabel.setText(progress.getLeft() + "/" + progress.getRight());

        // If the runs have finished, re enable the run buttons
        if (progress.getRight().equals(progress.getLeft())) {
            this.setEnabledRunButtons(true);
        }
    }

    /**
     * Generates a received power graph for a specific mote for a specific run, the amount of packets sent and the amount lost.
     * NOTE: this also updates the fields {@code packetsSent} and {@code packetsLost} to the corresponding values of that mote.
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A Pair containing ChartPanel containing a received power graph and another pair containing 2 integers: the amount of packets sent and the amount lost.
     */
    private ChartPanel generateReceivedPowerGraphForMotes(Mote mote, int run) {
        LinkedList<List<Pair<NetworkEntity, Pair<Integer, Double>>>> transmissionsMote = new LinkedList<>();

        this.packetsSent = 0;
        this.packetsLost = 0;

        for (Gateway gateway : mote.getEnvironment().getGateways()) {
            transmissionsMote.add(new LinkedList<>());
            for (LoraTransmission transmission : gateway.getAllReceivedTransmissions(run).keySet()) {
                if (transmission.getSender() == mote) {
                    this.packetsSent++;
                    if (!gateway.getAllReceivedTransmissions(run).get(transmission))
                        transmissionsMote.getLast().add(new Pair<>(transmission.getReceiver(), new Pair<>(transmission.getDepartureTime().toSecondOfDay(), transmission.getTransmissionPower())));
                    else {
                        transmissionsMote.getLast().add(new Pair<>(transmission.getReceiver(), new Pair<>(transmission.getDepartureTime().toSecondOfDay(), (double) 20)));
                        this.packetsLost++;
                    }
                }
            }
            if (transmissionsMote.getLast().isEmpty()) {
                transmissionsMote.remove(transmissionsMote.size() - 1);
            }
        }
        XYSeriesCollection dataReceivedPowerMote = new XYSeriesCollection();

        for (List<Pair<NetworkEntity, Pair<Integer, Double>>> list : transmissionsMote) {
            NetworkEntity receiver = list.get(0).getLeft();

            //noinspection SuspiciousMethodCalls Here we know for certain that the receiver is a gateway (packets are only sent to gateways)
            XYSeries series = new XYSeries("gateway " + (mote.getEnvironment().getGateways().indexOf(receiver) + 1));

            for (Pair<NetworkEntity, Pair<Integer, Double>> data : list) {
                series.add(data.getRight().getLeft(), data.getRight().getRight());
            }
            dataReceivedPowerMote.addSeries(series);
        }
        JFreeChart receivedPowerChartMote = ChartFactory.createScatterPlot(
            null, // chart title
            "Seconds", // x axis label
            "Received signal strength in dBm", // y axis label
            dataReceivedPowerMote, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) receivedPowerChartMote.getPlot();
        XYItemRenderer renderer = plot.getRenderer();
        for (int i = 0; i < dataReceivedPowerMote.getSeriesCount(); i++) {
            renderer.setSeriesShape(i, shape);
        }
        return new ChartPanel(receivedPowerChartMote);
    }

    /**
     * Generates a spreading factor graph for a specific mote for a specific run.
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a spreading factor graph.
     */
    private ChartPanel generateSpreadingFactorGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataSpreadingFactorMote = new XYSeriesCollection();
        XYSeries seriesSpreadingFactorMote = new XYSeries("Spreading factor");

        int i = 0;
        for (int spreadingFactor : mote.getSpreadingFactorHistory(run)) {
            seriesSpreadingFactorMote.add(i, spreadingFactor);
            i++;
        }
        dataSpreadingFactorMote.addSeries(seriesSpreadingFactorMote);

        JFreeChart spreadingFactorChartMote = ChartFactory.createScatterPlot(
            null, // chart title
            "Transmissions", // x axis label
            "Spreading factor", // y axis label
            dataSpreadingFactorMote, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) spreadingFactorChartMote.getPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 15.0);
        range.setTickUnit(new NumberTickUnit(1.0));
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (int series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);

        return new ChartPanel(spreadingFactorChartMote);

    }

    /**
     * Generates a used energy graph and the total used energy for a specific mote for a specific run.
     * NOTE: this also updates the field {@code usedEnergy} to the corresponding value of that mote.
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A Pair withChartPanel containing a used energy graph and a double the total ued energy.
     */
    private ChartPanel generateUsedEnergyGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataUsedEnergyEntity = new XYSeriesCollection();
        int i = 0;
        XYSeries seriesUsedEnergyEntity = new XYSeries("Used energy");
        this.usedEnergy = 0;
        for (Double usedEnergy : mote.getUsedEnergy(run)) {
            this.usedEnergy += usedEnergy;
            seriesUsedEnergyEntity.add(i, usedEnergy);
            i = i + 1;
        }
        dataUsedEnergyEntity.addSeries(seriesUsedEnergyEntity);
        JFreeChart usedEnergyChartEntity = ChartFactory.createXYLineChart(
            null, // chart title
            "Transmissions", // x axis label
            "Used energy in mJoule", // y axis label
            dataUsedEnergyEntity, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) usedEnergyChartEntity.getPlot();
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (int series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);

        return new ChartPanel(usedEnergyChartEntity);

    }

    /**
     * Generates a distance to gateway graph for a specific mote for a specific run.
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a distance to gateway graph.
     */
    private ChartPanel generateDistanceToGatewayGraph(Mote mote, int run) {
        LinkedList<LinkedList<LoraTransmission>> transmissionsMote = new LinkedList<>();

        for (Gateway gateway : mote.getEnvironment().getGateways()) {
            transmissionsMote.add(new LinkedList<>());
            for (LoraTransmission transmission : gateway.getAllReceivedTransmissions(run).keySet()) {
                if (transmission.getSender() == mote) {
                    transmissionsMote.getLast().add(transmission);
                }
            }
            if (transmissionsMote.getLast().isEmpty()) {
                transmissionsMote.remove(transmissionsMote.size() - 1);
            }
        }
        XYSeriesCollection dataDistanceToGateway = new XYSeriesCollection();

        for (LinkedList<LoraTransmission> list : transmissionsMote) {
            NetworkEntity receiver = list.get(0).getReceiver();

            //noinspection SuspiciousMethodCalls Here we know for certain that the receiver is a gateway (packets are only sent to gateways)
            XYSeries series = new XYSeries("gateway " + (mote.getEnvironment().getGateways().indexOf(receiver) + 1));
            int i = 0;
            for (LoraTransmission transmission : list) {
                series.add(i, (Number) Math.sqrt(Math.pow(transmission.getReceiver().getYPosInt() - transmission.getYPos(), 2) +
                    Math.pow(transmission.getReceiver().getXPosInt() - transmission.getXPos(), 2)));
                i = i + 1;
            }
            dataDistanceToGateway.addSeries(series);
        }

        JFreeChart DistanceToGatewayChartMote = ChartFactory.createXYLineChart(
            null, // chart title
            "Transmissions", // x axis label
            "Distance to the gateway in  m", // y axis label
            dataDistanceToGateway, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        Shape shape = new Ellipse2D.Double(0, 0, 3, 3);
        XYPlot plot = (XYPlot) DistanceToGatewayChartMote.getPlot();
        XYItemRenderer renderer = plot.getRenderer();

        XYLineAndShapeRenderer LineRenderer = new XYLineAndShapeRenderer();
        for (int series = 0; series < plot.getSeriesCount(); series++) {
            LineRenderer.setSeriesPaint(series, renderer.getSeriesPaint(series));
            LineRenderer.setSeriesStroke(series, new BasicStroke(1.0f));
            LineRenderer.setSeriesShape(series, shape);
        }
        plot.setRenderer(LineRenderer);
        return new ChartPanel(DistanceToGatewayChartMote);

    }

    /**
     * Generates a power setting graph for a specific mote for a specific run.
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a power setting graph.
     */
    private ChartPanel generatePowerSettingGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataPowerSettingMote = new XYSeriesCollection();
        XYSeries seriesPowerSettingMote = new XYSeries("Power setting");
        for (Pair<Integer, Integer> powerSetting : mote.getPowerSettingHistory(run)) {
            seriesPowerSettingMote.add(powerSetting.getLeft(), powerSetting.getRight());
        }
        dataPowerSettingMote.addSeries(seriesPowerSettingMote);

        JFreeChart powerSettingChartMote = ChartFactory.createXYLineChart(
            null, // chart title
            "Seconds", // x axis label
            "Power setting in dBm", // y axis label
            dataPowerSettingMote, // data
            PlotOrientation.VERTICAL,
            true, // include legend
            true, // tooltips
            false // urls
        );
        XYPlot plot = (XYPlot) powerSettingChartMote.getPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 15.0);
        range.setTickUnit(new NumberTickUnit(1.0));
        return new ChartPanel(powerSettingChartMote);
    }

    void setRelCom(IntervalAdaptationGoal intervalAdaptationGoal) {
        Simulation simulation = simulationRunner.getSimulation();
        QualityOfService QoS = simulationRunner.getQoS();

        simulation.getInputProfile().ifPresent(inputProfile -> {
            inputProfile.putAdaptationGoal("reliableCommunication", intervalAdaptationGoal);
            QoS.putAdaptationGoal("reliableCommunication", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("reliableCommunication"));
            updateAdaptationGoals();
        });
    }

    void setEnCon(ThresholdAdaptationGoal thresholdAdaptationGoal) {
        Simulation simulation = simulationRunner.getSimulation();
        QualityOfService QoS = simulationRunner.getQoS();

        simulation.getInputProfile().ifPresent(inputProfile -> {
            inputProfile.putAdaptationGoal("energyConsumption", thresholdAdaptationGoal);
            QoS.putAdaptationGoal("energyConsumption", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("energyConsumption"));
            updateAdaptationGoals();
        });

    }

    void setColBound(ThresholdAdaptationGoal thresholdAdaptationGoal) {
        Simulation simulation = simulationRunner.getSimulation();
        QualityOfService QoS = simulationRunner.getQoS();

        simulation.getInputProfile().ifPresent(inputProfile -> {
            inputProfile.putAdaptationGoal("collisionBound", thresholdAdaptationGoal);
            QoS.putAdaptationGoal("collisionBound", inputProfile.getQualityOfServiceProfile().getAdaptationGoal("collisionBound"));
            updateAdaptationGoals();
        });

    }


    private void setEnabledRunButtons(boolean state) {
        this.singleRunButton.setEnabled(state);
        this.timedRunButton.setEnabled(state);
        this.totalRunButton.setEnabled(state);
    }

    @Override
    public void update() {
        try {
            SwingUtilities.invokeAndWait(this::refreshMap);
        } catch (InterruptedException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onEnd() {
        try {
            SwingUtilities.invokeAndWait(this::refreshMap);
        } catch (InterruptedException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        this.setEnabledRunButtons(true);
    }





    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        toolBarEnvironment = new JToolBar();
        toolBarEnvironment.setFloatable(false);
        toolBarEnvironment.setRollover(true);
        toolBarEnvironment.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        mainPanel.add(toolBarEnvironment, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Configuration:");
        toolBarEnvironment.add(label1);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator1);
        openConfigurationButton = new JButton();
        openConfigurationButton.setText("Open");
        toolBarEnvironment.add(openConfigurationButton);
        final JToolBar.Separator toolBar$Separator2 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator2);
        saveConfigurationButton = new JButton();
        saveConfigurationButton.setText("Save");
        toolBarEnvironment.add(saveConfigurationButton);
        final JToolBar.Separator toolBar$Separator3 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator3);
        configureButton = new JButton();
        configureButton.setText("Configure");
        toolBarEnvironment.add(configureButton);
        final Spacer spacer1 = new Spacer();
        toolBarEnvironment.add(spacer1);
        final JToolBar.Separator toolBar$Separator4 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator4);
        helpButton = new JButton();
        helpButton.setText("Help");
        toolBarEnvironment.add(helpButton);
        final Spacer spacer2 = new Spacer();
        toolBarEnvironment.add(spacer2);
        final JToolBar.Separator toolBar$Separator5 = new JToolBar.Separator();
        toolBarEnvironment.add(toolBar$Separator5);
        aboutButton = new JButton();
        aboutButton.setText("About");
        toolBarEnvironment.add(aboutButton);
        final Spacer spacer3 = new Spacer();
        toolBarEnvironment.add(spacer3);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(550);
        splitPane1.setOrientation(0);
        mainPanel.add(splitPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane1.setLeftComponent(splitPane2);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane2.setLeftComponent(panel1);
        entitiesPane = new JScrollPane();
        entitiesPane.setHorizontalScrollBarPolicy(31);
        entitiesPane.setVerticalScrollBarPolicy(22);
        panel1.add(entitiesPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(250, -1), new Dimension(250, -1), 0, false));
        entitesPanel = new JPanel();
        entitesPanel.setLayout(new GridBagLayout());
        entitiesPane.setViewportView(entitesPanel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        splitPane2.setRightComponent(panel2);
        final JToolBar toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        panel2.add(toolBar1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Map");
        toolBar1.add(label2);
        final JToolBar.Separator toolBar$Separator6 = new JToolBar.Separator();
        toolBar1.add(toolBar$Separator6);
        final Spacer spacer4 = new Spacer();
        toolBar1.add(spacer4);
        final JLabel label3 = new JLabel();
        label3.setText("Center:");
        toolBar1.add(label3);
        centerLabel = new JLabel();
        centerLabel.setText("");
        toolBar1.add(centerLabel);
        final Spacer spacer5 = new Spacer();
        toolBar1.add(spacer5);
        map = new JPanel();
        map.setLayout(new BorderLayout(0, 0));
        map.setBackground(new Color(-4473925));
        map.setForeground(new Color(-12828863));
        panel2.add(map, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(400, 200), null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel2.add(spacer6, new GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 250), null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane1.setRightComponent(panel3);
        final JSplitPane splitPane3 = new JSplitPane();
        splitPane3.setDividerLocation(450);
        panel3.add(splitPane3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane3.setLeftComponent(panel4);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        console = new JPanel();
        console.setLayout(new GridLayoutManager(1, 1, new Insets(0, 5, 0, 0), -1, -1));
        console.setBackground(new Color(-4473925));
        console.setForeground(new Color(-12828863));
        panel5.add(console, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), new Dimension(-1, 400), 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(20);
        console.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(250, -1), null, null, 0, false));
        InputProfilePanel = new JPanel();
        InputProfilePanel.setLayout(new GridBagLayout());
        scrollPane1.setViewportView(InputProfilePanel);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 20, 0), -1, -1));
        panel5.add(panel6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(250, -1), null, new Dimension(-1, 150), 0, false));
        final JToolBar toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        panel6.add(toolBar2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Adaptation Goals:");
        toolBar2.add(label4);
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel7.add(panel8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(400, -1), null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel8.add(panel9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(-1, 36), 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Reliable communication:");
        panel9.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        panel9.add(spacer7, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        relComLabel = new JLabel();
        relComLabel.setText("Interval: [-48,-42]");
        panel9.add(relComLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editRelComButton = new JButton();
        editRelComButton.setText("Edit");
        panel9.add(editRelComButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("dB");
        panel9.add(label6, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel8.add(panel10, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(262, 36), new Dimension(-1, 36), 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Energy consumption:");
        panel10.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enConLabel = new JLabel();
        enConLabel.setText("Threshold: 100");
        panel10.add(enConLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editEnConButton = new JButton();
        editEnConButton.setText("Edit");
        panel10.add(editEnConButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        panel10.add(spacer8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("mJ/min");
        panel10.add(label8, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(1, 5, new Insets(2, 3, 0, 3), -1, -1));
        panel8.add(panel11, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(262, 36), new Dimension(-1, 36), 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Collision Bound: ");
        panel11.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        colBoundLabel = new JLabel();
        colBoundLabel.setText("Threshold: 10");
        panel11.add(colBoundLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        editColBoundButton = new JButton();
        editColBoundButton.setText("Edit");
        panel11.add(editColBoundButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        panel11.add(spacer9, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("%");
        panel11.add(label10, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JToolBar toolBar3 = new JToolBar();
        toolBar3.setFloatable(false);
        panel5.add(toolBar3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Input Profile");
        toolBar3.add(label11);
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane3.setRightComponent(panel12);
        final JSplitPane splitPane4 = new JSplitPane();
        splitPane4.setDividerLocation(650);
        panel12.add(splitPane4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane4.setLeftComponent(panel13);
        tabbedPaneGraphs = new JTabbedPane();
        tabbedPaneGraphs.setBackground(new Color(-4473925));
        tabbedPaneGraphs.setForeground(new Color(-12828863));
        panel13.add(tabbedPaneGraphs, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(250, 400), null, 0, false));
        receivedPowerGraph = new JPanel();
        receivedPowerGraph.setLayout(new BorderLayout(0, 0));
        receivedPowerGraph.setBackground(new Color(-4473925));
        receivedPowerGraph.setForeground(new Color(-12828863));
        tabbedPaneGraphs.addTab("Received Power", receivedPowerGraph);
        powerSettingGraph = new JPanel();
        powerSettingGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Power Setting", powerSettingGraph);
        distanceToGatewayGraph = new JPanel();
        distanceToGatewayGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Distance To Gateway", distanceToGatewayGraph);
        spreadingFactorGraph = new JPanel();
        spreadingFactorGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Spreading Factor", spreadingFactorGraph);
        usedEnergyGraph = new JPanel();
        usedEnergyGraph.setLayout(new BorderLayout(0, 0));
        tabbedPaneGraphs.addTab("Used Energy", usedEnergyGraph);
        final JToolBar toolBar4 = new JToolBar();
        toolBar4.setFloatable(false);
        panel13.add(toolBar4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar.Separator toolBar$Separator7 = new JToolBar.Separator();
        toolBar4.add(toolBar$Separator7);
        moteCharacteristicsButton = new JButton();
        moteCharacteristicsButton.setText("Mote");
        toolBar4.add(moteCharacteristicsButton);
        final JToolBar.Separator toolBar$Separator8 = new JToolBar.Separator();
        toolBar4.add(toolBar$Separator8);
        final JLabel label12 = new JLabel();
        label12.setText("Selected: ");
        toolBar4.add(label12);
        moteCharacteristicsLabel = new JLabel();
        moteCharacteristicsLabel.setText("");
        toolBar4.add(moteCharacteristicsLabel);
        final JLabel label13 = new JLabel();
        label13.setText(" ");
        toolBar4.add(label13);
        resultsButton = new JButton();
        resultsButton.setText("Results");
        toolBar4.add(resultsButton);
        final Spacer spacer10 = new Spacer();
        toolBar4.add(spacer10);
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPane4.setRightComponent(panel14);
        final JToolBar toolBar5 = new JToolBar();
        toolBar5.setFloatable(false);
        panel14.add(toolBar5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JToolBar.Separator toolBar$Separator9 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator9);
        moteApplicationButton = new JButton();
        moteApplicationButton.setText("Mote");
        toolBar5.add(moteApplicationButton);
        final JToolBar.Separator toolBar$Separator10 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator10);
        regionButton = new JButton();
        regionButton.setText("Region");
        toolBar5.add(regionButton);
        final JToolBar.Separator toolBar$Separator11 = new JToolBar.Separator();
        toolBar5.add(toolBar$Separator11);
        final JLabel label14 = new JLabel();
        label14.setText("Selected: ");
        toolBar5.add(label14);
        moteApplicationLabel = new JLabel();
        moteApplicationLabel.setText("");
        toolBar5.add(moteApplicationLabel);
        final Spacer spacer11 = new Spacer();
        toolBar5.add(spacer11);
        tabbedPane1 = new JTabbedPane();
        panel14.add(tabbedPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        particulateMatterPanel = new JPanel();
        particulateMatterPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Particulate matter", particulateMatterPanel);
        carbonDioxideField = new JPanel();
        carbonDioxideField.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Carbon dioxide", carbonDioxideField);
        sootPanel = new JPanel();
        sootPanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Soot", sootPanel);
        ozonePanel = new JPanel();
        ozonePanel.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab("Ozone", ozonePanel);
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 0, 0));
        panel3.add(panel15, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toolBarAdaptation = new JToolBar();
        toolBarAdaptation.setBorderPainted(true);
        toolBarAdaptation.setFloatable(false);
        toolBarAdaptation.setRollover(true);
        toolBarAdaptation.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        panel15.add(toolBarAdaptation, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Simulation  ");
        toolBarAdaptation.add(label15);
        adaptationComboBox = new JComboBox();
        toolBarAdaptation.add(adaptationComboBox);
        final JToolBar.Separator toolBar$Separator12 = new JToolBar.Separator();
        toolBarAdaptation.add(toolBar$Separator12);
        singleRunButton = new JButton();
        singleRunButton.setText("Single Run");
        toolBarAdaptation.add(singleRunButton);
        final JLabel label16 = new JLabel();
        label16.setText("  ");
        toolBarAdaptation.add(label16);
        final JLabel label17 = new JLabel();
        label17.setText("Speed:");
        toolBarAdaptation.add(label17);
        speedSlider = new JSlider();
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMaximum(5);
        speedSlider.setMinimum(1);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintLabels(false);
        speedSlider.setPaintTicks(true);
        speedSlider.setValue(1);
        speedSlider.setValueIsAdjusting(false);
        toolBarAdaptation.add(speedSlider);
        timedRunButton = new JButton();
        timedRunButton.setText("Timed Run");
        toolBarAdaptation.add(timedRunButton);
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        toolBarAdaptation.add(panel16);
        final JToolBar toolBar6 = new JToolBar();
        toolBar6.setFloatable(false);
        panel16.add(toolBar6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 35), null, 0, false));
        final JToolBar.Separator toolBar$Separator13 = new JToolBar.Separator();
        toolBar6.add(toolBar$Separator13);
        totalRunButton = new JButton();
        totalRunButton.setText("Total Run");
        toolBar6.add(totalRunButton);
        final JLabel label18 = new JLabel();
        label18.setText("  ");
        toolBar6.add(label18);
        final JLabel label19 = new JLabel();
        label19.setText("Progress: ");
        toolBar6.add(label19);
        totalRunProgressBar = new JProgressBar();
        toolBar6.add(totalRunProgressBar);
        final JToolBar.Separator toolBar$Separator14 = new JToolBar.Separator();
        toolBar6.add(toolBar$Separator14);
        progressLabel = new JLabel();
        progressLabel.setText("0/0");
        toolBar6.add(progressLabel);
        final JPanel panel17 = new JPanel();
        panel17.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
        panel15.add(panel17, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JToolBar toolBar7 = new JToolBar();
        toolBar7.setFloatable(false);
        panel17.add(toolBar7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 35), null, 0, false));
        final JToolBar.Separator toolBar$Separator15 = new JToolBar.Separator();
        toolBar7.add(toolBar$Separator15);
        final JLabel label20 = new JLabel();
        label20.setText("Experimental results:  ");
        toolBar7.add(label20);
        simulationSaveButton = new JButton();
        simulationSaveButton.setText("Save");
        toolBar7.add(simulationSaveButton);
        final JLabel label21 = new JLabel();
        label21.setText("  ");
        toolBar7.add(label21);
        clearButton = new JButton();
        clearButton.setText("Clear");
        toolBar7.add(clearButton);
        final JToolBar.Separator toolBar$Separator16 = new JToolBar.Separator();
        toolBar7.add(toolBar$Separator16);
        final Spacer spacer12 = new Spacer();
        toolBar7.add(spacer12);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}

