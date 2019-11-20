package gui.util;

import iot.lora.LoraTransmission;
import iot.networkentity.Gateway;
import iot.networkentity.Mote;
import iot.networkentity.NetworkEntity;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import util.EnvironmentHelper;
import util.Pair;
import util.Statistics;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.LinkedList;
import java.util.List;

public class ChartGenerator {

    /**
     * Generates a received power graph for a specific mote for a specific run, the amount of packets sent and the amount lost.
     * NOTE: this also updates the fields {@code packetsSent} and {@code packetsLost} to the corresponding values of that mote.
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A Pair containing ChartPanel containing a received power graph and another pair containing 2 integers: the amount of packets sent and the amount lost.
     */
    public static ChartPanel generateReceivedPowerGraphForMotes(Mote mote, int run) {
        LinkedList<List<Pair<NetworkEntity, Pair<Integer, Double>>>> transmissionsMote = new LinkedList<>();

        var env = mote.getEnvironment();
        Statistics statistics = Statistics.getInstance();

        for (Gateway gateway : mote.getEnvironment().getGateways()) {
            transmissionsMote.add(new LinkedList<>());
            for (LoraTransmission transmission : statistics.getAllReceivedTransmissions(gateway.getEUI(), run)) {
                if (transmission.getSender() == mote.getEUI()) {
                    if (!transmission.isCollided())
                        transmissionsMote.getLast().add(
                            new Pair<>(EnvironmentHelper.getNetworkEntityById(env, transmission.getReceiver()),
                                new Pair<>(transmission.getDepartureTime().toSecondOfDay(), transmission.getTransmissionPower())));
                    else {
                        transmissionsMote.getLast().add(
                            new Pair<>(EnvironmentHelper.getNetworkEntityById(env, transmission.getReceiver()),
                                new Pair<>(transmission.getDepartureTime().toSecondOfDay(), (double) 20)));
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
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a spreading factor graph.
     */
    public static ChartPanel generateSpreadingFactorGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataSpreadingFactorMote = new XYSeriesCollection();
        XYSeries seriesSpreadingFactorMote = new XYSeries("Spreading factor");
        Statistics statistics = Statistics.getInstance();

        int i = 0;
        for (int spreadingFactor : statistics.getSpreadingFactorHistory(mote.getEUI(), run)) {
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
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A Pair withChartPanel containing a used energy graph and a double the total ued energy.
     */
    public static ChartPanel generateUsedEnergyGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataUsedEnergyEntity = new XYSeriesCollection();
        int i = 0;
        Statistics statistics = Statistics.getInstance();

        XYSeries seriesUsedEnergyEntity = new XYSeries("Used energy");
        for (double usedEnergy : statistics.getUsedEnergy(mote.getEUI(), run)) {
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
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a distance to gateway graph.
     */
    public static ChartPanel generateDistanceToGatewayGraph(Mote mote, int run) {
        LinkedList<LinkedList<LoraTransmission>> transmissionsMote = new LinkedList<>();
        Statistics statistics = Statistics.getInstance();

        for (Gateway gateway : mote.getEnvironment().getGateways()) {
            transmissionsMote.add(new LinkedList<>());
            for (LoraTransmission transmission : statistics.getAllReceivedTransmissions(gateway.getEUI(), run)) {
                if (transmission.getSender() == mote.getEUI()) {
                    transmissionsMote.getLast().add(transmission);
                }
            }
            if (transmissionsMote.getLast().isEmpty()) {
                transmissionsMote.remove(transmissionsMote.size() - 1);
            }
        }
        XYSeriesCollection dataDistanceToGateway = new XYSeriesCollection();

        var env = mote.getEnvironment();
        for (LinkedList<LoraTransmission> list : transmissionsMote) {
            NetworkEntity receiver = EnvironmentHelper.getNetworkEntityById(env, list.get(0).getReceiver());

            //noinspection SuspiciousMethodCalls Here we know for certain that the receiver is a gateway (packets are only sent to gateways)
            XYSeries series = new XYSeries("gateway " + (mote.getEnvironment().getGateways().indexOf(receiver) + 1));
            int i = 0;
            for (LoraTransmission transmission : list) {
                series.add(i, (Number) Math.sqrt(Math.pow(EnvironmentHelper.getNetworkEntityById(env, transmission.getReceiver()).getYPosInt() - transmission.getYPos(), 2) +
                    Math.pow(EnvironmentHelper.getNetworkEntityById(env, transmission.getReceiver()).getXPosInt() - transmission.getXPos(), 2)));
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
     *
     * @param mote The mote to generate the graph of.
     * @param run  The run to generate the graph of
     * @return A ChartPanel containing a power setting graph.
     */
    public static ChartPanel generatePowerSettingGraph(NetworkEntity mote, int run) {
        XYSeriesCollection dataPowerSettingMote = new XYSeriesCollection();
        XYSeries seriesPowerSettingMote = new XYSeries("Power setting");
        Statistics statistics = Statistics.getInstance();

        for (Pair<Integer, Integer> powerSetting : statistics.getPowerSettingHistory(mote.getEUI(), run)) {
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

}
