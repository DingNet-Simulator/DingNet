
import IotDomain.*;
import GUI.MapViewer.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MapView {
    public static void main(String[] args)
    {
        /*
        Set to enable or disable adaptation of node 0 (D1).
         */
        Boolean adaption = true;

        JXMapViewer mapViewer = new JXMapViewer();

        // Create a TileFactoryInfo for OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Use 8 threads in parallel to load the tiles
        tileFactory.setThreadPoolSize(8);
        /*
        generate all the points
         */
        GeoPosition mapzero = new GeoPosition(50.853718, 4.673155);
        Integer mapsize = (int) Math.ceil(1000 *Math.max(Environment.distance(50.853718, 4.673155, 50.878697,   4.673155), Environment.distance(50.853718, 4.673155, 50.853718,   4.701200)));
        GeoPosition leuven = new GeoPosition(50,51,46,4,41,2);
        GeoPosition gw1 = new GeoPosition(50.859722, 4.681944);
        GeoPosition gw2 = new GeoPosition(50.863780, 4.677992);
        GeoPosition gw3 = new GeoPosition(50.867222, 4.678056);
        GeoPosition gw4 = new GeoPosition(50.856667, 4.676389);

        GeoPosition mp1 = new GeoPosition(50.8605, 4.6795);

        GeoPosition wp1 = new GeoPosition(50.856020, 4.675844);
        GeoPosition wp2 = new GeoPosition(50.856545, 4.676743);
        GeoPosition wp3 = new GeoPosition(50.857852, 4.679702);
        GeoPosition wp4 = new GeoPosition(50.860061, 4.683473);
        GeoPosition wp5 = new GeoPosition(50.861985, 4.680993);
        GeoPosition wp6 = new GeoPosition(50.862263, 4.680672);
        GeoPosition wp7 = new GeoPosition(50.862696, 4.680416);
        GeoPosition wp8 = new GeoPosition(50.863049, 4.680321);
        GeoPosition wp9 = new GeoPosition(50.863455, 4.680385);
        GeoPosition wp10 = new GeoPosition(50.863977, 4.680610);
        GeoPosition wp11 = new GeoPosition(50.864770, 4.680898);
        GeoPosition wp12 = new GeoPosition(50.865176, 4.680973);
        GeoPosition wp13 = new GeoPosition(50.865583, 4.680976);
        GeoPosition wp14 = new GeoPosition(50.867980, 4.680381);
        GeoPosition wp15 = new GeoPosition(50.867881, 4.678226);
        GeoPosition wp16 = new GeoPosition(50.868028, 4.678175);
        GeoPosition wp17 = new GeoPosition(50.869650, 4.676740);

        GeoPosition wp21 = new GeoPosition(50.868551, 4.698337);
        GeoPosition wp22 = new GeoPosition(50.866713, 4.695153);
        GeoPosition wp23 = new GeoPosition(50.861330, 4.685687);
        GeoPosition wp24 = new GeoPosition(50.857910, 4.679724);
        GeoPosition wp25 = new GeoPosition(50.856486, 4.676650);

        GeoPosition positionMote2 = new GeoPosition(50.862752, 4.688886);
        /*
         * Paint the points on the map.
         */
        List<GeoPosition> track1 = Arrays.asList(wp1,wp2,wp3,wp4,wp5,wp6,wp7,wp8,wp9,wp10,wp11,wp12,wp13,wp14,wp15,wp16,wp17);
        BorderPainter borderPainter1 = new BorderPainter(track1);

        List<GeoPosition> track2 = Arrays.asList(wp21,wp22,wp23,wp24,wp25,wp1);
        BorderPainter borderPainter2 = new BorderPainter(track2);

        Set<Waypoint> gateWays = new HashSet<>(Arrays.asList(
                new DefaultWaypoint(gw1),
                new DefaultWaypoint(gw2),
                new DefaultWaypoint(gw3),
                new DefaultWaypoint(gw4)));

        Set<Waypoint> mps = new HashSet<>(Arrays.asList(
                new DefaultWaypoint(mp1)));


        GatewayWaypointPainter<Waypoint> gateWayPainter =new GatewayWaypointPainter<>();
        gateWayPainter.setWaypoints(gateWays);

        MoteWaypointPainter<Waypoint> moteWaypointPainter = new MoteWaypointPainter<>();
        moteWaypointPainter.setWaypoints(new HashSet<Waypoint>(Arrays.asList(new DefaultWaypoint(wp5), new DefaultWaypoint(positionMote2))));


        AWaypointPainter<Waypoint> aWaypointPainter = new AWaypointPainter<>();
        aWaypointPainter.setWaypoints(new HashSet<Waypoint>(Arrays.asList(new DefaultWaypoint(wp1))));

        BWaypointPainter<Waypoint> bWaypointPainter = new BWaypointPainter<>();
        bWaypointPainter.setWaypoints(new HashSet<Waypoint>(Arrays.asList(new DefaultWaypoint(wp17))));

        CWaypointPainter<Waypoint> cWaypointPainter = new CWaypointPainter<>();
        cWaypointPainter.setWaypoints(new HashSet<Waypoint>(Arrays.asList(new DefaultWaypoint(wp21))));

        WaypointPainter<Waypoint> MPPainter =new WaypointPainter<>();
        MPPainter.setWaypoints(mps);


        mapViewer.setZoom(4);
        mapViewer.setAddressLocation(leuven);

        // Create a compound painter to paint all points
        List<Painter<JXMapViewer>> painters = new ArrayList<>();
        painters.add(borderPainter1);
        painters.add(borderPainter2);
        painters.add(gateWayPainter);
        //painters.add(MPPainter);
        painters.add(moteWaypointPainter);
        painters.add(aWaypointPainter);
        painters.add(bWaypointPainter);
        painters.add(cWaypointPainter);

        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);

        // Display the viewer in a JFrame
        JFrame frame = new JFrame("Dingnet");
        frame.getContentPane().add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        /*
         Prepare simulation environment.
         */
        Characteristic[][] map = new Characteristic[mapsize][mapsize];
        for(int i =0; i < mapsize; i++){
            for(int j =0; j < mapsize /3 ; j++){
                map[j][i] = Characteristic.Forest;
            }
            for(int j =(int) Math.floor(mapsize /3); j < 2*mapsize /3 ; j++){
                map[j][i] = Characteristic.Plain;
            }

            for(int j =(int) Math.floor(2*mapsize /3); j < mapsize ; j++){
                map[j][i] = Characteristic.City;
            }
        }
        Environment environment = new Environment(map,mapzero,new LinkedHashSet<>());
        /*
        Add motes and gateways.
         */
        Random random = new Random();
        new Gateway(random.nextLong(),(int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), gw1.getLongitude())),
                (int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),gw1.getLatitude(), mapzero.getLongitude())),
                environment, 14,12);
        new Gateway(random.nextLong(),(int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), gw2.getLongitude())),
                (int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),gw2.getLatitude(), mapzero.getLongitude())),
                environment, 14,12);
        new Gateway(random.nextLong(),(int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), gw3.getLongitude())),
                (int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),gw3.getLatitude(), mapzero.getLongitude())),
                environment, 14,12);
        new Gateway(random.nextLong(),(int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), gw4.getLongitude())),
                (int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),gw4.getLatitude(), mapzero.getLongitude())),
                environment, 14,12);
        new Mote(random.nextLong(),(int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), wp1.getLongitude())),
                (int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),wp1.getLatitude(), mapzero.getLongitude())),
                environment, 14,12, new LinkedList<>(),0,new LinkedList<>(),10,0.5);

        new Mote(random.nextLong(),(int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), positionMote2.getLongitude())),
                (int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),positionMote2.getLatitude(), mapzero.getLongitude())),
                environment, 14,12, new LinkedList<>(),0,new LinkedList<>(),10,0.5);
        new Mote(random.nextLong(),toMapXCoordinate(wp21,mapzero),
                toMapYCoordinate(wp21,mapzero),
                environment, 0,12, new LinkedList<>(),0,new LinkedList<>(),10,0.5);
        /*
        get the motes.
         */
        Mote mote0 = environment.getMotes().get(0);
        Mote mote1 = environment.getMotes().get(1);
        Mote mote2 = environment.getMotes().get(2);

        /*
         Actual simulation
         */
        LinkedList<Integer> powerSetting1 = new LinkedList<>();
        LinkedList<LoraTransmission> HighestPower1 = new LinkedList<>();
        Integer mote1counter = 9;
        Integer mote2counter = random.nextInt(15)+1;
        LinkedList<Integer> indexesMote2 = new LinkedList<>();
        indexesMote2.add(mote2counter);
        Integer trackposition1 = track1.size();
        Integer trackposition2 = track2.size();
        while(  Integer.signum(mote0.getXPos() - toMapXCoordinate(track1.get(track1.size()-1),mapzero ))!= 0 ||
                Integer.signum(mote0.getYPos() - toMapYCoordinate(track1.get(track1.size()-1),mapzero ))!= 0 ||
                Integer.signum(mote2.getXPos() - toMapXCoordinate(track2.get(track2.size()-1),mapzero ))!= 0 ||
                Integer.signum(mote2.getYPos() - toMapYCoordinate(track2.get(track2.size()-1),mapzero ))!= 0
                ){
            if(moveMote(track1.get(track1.size()-trackposition1),mote0,mapzero)){
                if(mote1counter == 0) {
                    mote0.sendToGateWay(new Byte[0], new HashMap<>());
                    if(adaption){
                        powerSetting1.add(mote0.getTransmissionPower());
                        HighestPower1.add(naiveAdaptionAlgorithm(mote0));
                    }
                    mote1counter = 9;
                }
                else
                    mote1counter --;

            }
            else if(trackposition1 > 1){
                trackposition1 --;
            }

            if(moveMote(track2.get(track2.size()-trackposition2),mote2,mapzero)){
                if(mote2counter == 0) {
                    mote2.sendToGateWay(new Byte[0], new HashMap<>());
                    mote2counter = random.nextInt(15)+1;
                    indexesMote2.add(indexesMote2.getLast() + mote2counter);
                }
                else
                    mote2counter --;
            }
            else if(trackposition2 > 1){
                trackposition2 --;
            }

            environment.tick(1500);


        }
       /*
       Data collection mote 1
        */
        LinkedList<LinkedList<LoraTransmission>> transmissionsMote0 = new LinkedList<>();
        Integer transmittedPacketsMote0 = 0;
        Integer lostPacketsMote0 = 0;
        for(Gateway gateway:environment.getGateways()){
            transmissionsMote0.add(new LinkedList<>());
            for(LoraTransmission transmission :gateway.getAllReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).keySet()){
                if(transmission.getSender() == mote0) {
                    transmittedPacketsMote0++;
                    if (!gateway.getAllReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).get(transmission))
                        transmissionsMote0.getLast().add(transmission);
                    else {
                        transmissionsMote0.getLast().add(new LoraTransmission(transmission.getSender(),
                                transmission.getReceiver(), -10, transmission.getBandwidth(),
                                transmission.getSpreadingFactor(), transmission.getContent()));
                        lostPacketsMote0++;
                    }
                }
            }
        }

        /*
       Data collection mote 2
        */
        LinkedList<LinkedList<LoraTransmission>> transmissionsMote2 = new LinkedList<>();

        Integer transmittedPacketsMote2 = 0;
        Integer lostPacketsMote2 = 0;
        for(Gateway gateway:environment.getGateways()){
            transmissionsMote2.add(new LinkedList<>());
            for(LoraTransmission transmission :gateway.getAllReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).keySet()){
                if(transmission.getSender() == mote2) {
                    transmittedPacketsMote2 ++;
                    if (!gateway.getAllReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).get(transmission))
                        transmissionsMote2.getLast().add(transmission);
                    else {
                        lostPacketsMote2 ++;
                        transmissionsMote2.getLast().add(new LoraTransmission(transmission.getSender(),
                                transmission.getReceiver(), -10, transmission.getBandwidth(),
                                transmission.getSpreadingFactor(), transmission.getContent()));
                    }
                }
            }
        }

        System.out.println("Sent Packets: " +transmittedPacketsMote0);
        System.out.println("Lost Packets: " +lostPacketsMote0);
        System.out.println("Sent Packets: " +transmittedPacketsMote2);
        System.out.println("Lost Packets: " +lostPacketsMote2);

        /*
        Creating charts
         */
        XYSeriesCollection dataMote0 = new XYSeriesCollection();
        for(LinkedList<LoraTransmission> list: transmissionsMote0){
            XYSeries series = new XYSeries(list.get(0).getReceiver().toString());
            Integer i = 0;
            for (LoraTransmission transmission: list){
                series.add(i,(Number)transmission.getTransmissionPower());
                i = i +10;
            }
            dataMote0.addSeries(series);
        }

        XYSeriesCollection dataMote2 = new XYSeriesCollection();
        for(LinkedList<LoraTransmission> list: transmissionsMote2){
            XYSeries series = new XYSeries(list.get(0).getReceiver().toString());
            Integer i = 0;
            for (LoraTransmission transmission: list){
                series.add(indexesMote2.get(i),(Number)transmission.getTransmissionPower());
                i++;
            }
            dataMote2.addSeries(series);
        }

        JFreeChart receivedPowerChartMote0 = ChartFactory.createScatterPlot(
                null, // chart title
                "Distance travelled in meter", // x axis label
                "Received signal strength in dB", // y axis label
                dataMote0, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        XYPlot xyPlotreceivedPowerMote0 = (XYPlot) receivedPowerChartMote0.getPlot();
        xyPlotreceivedPowerMote0.setDomainCrosshairVisible(true);
        xyPlotreceivedPowerMote0.setRangeCrosshairVisible(true);
        NumberAxis domainreceivedPowerMote0 = (NumberAxis) xyPlotreceivedPowerMote0.getDomainAxis();
        domainreceivedPowerMote0.setRange(0.0, 2700.0);
        domainreceivedPowerMote0.setTickUnit(new NumberTickUnit(200));
        domainreceivedPowerMote0.setVerticalTickLabels(true);
        NumberAxis rangereceivedPowerMote0 = (NumberAxis) xyPlotreceivedPowerMote0.getRangeAxis();
        rangereceivedPowerMote0.setRange(-85, 0.0);
        rangereceivedPowerMote0.setTickUnit(new NumberTickUnit(4));

        JFreeChart receivedPowerChartMote2 = ChartFactory.createScatterPlot(
                null, // chart title
                "Distance travelled in meter", // x axis label
                "Received signal strength in dB", // y axis label
                dataMote2, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true, // tooltips
                false // urls
        );
        XYPlot xyPlotreceivedPowerMote2 = (XYPlot) receivedPowerChartMote2.getPlot();
        xyPlotreceivedPowerMote2.setDomainCrosshairVisible(true);
        xyPlotreceivedPowerMote2.setRangeCrosshairVisible(true);
        NumberAxis domainreceivedPowerMote2 = (NumberAxis) xyPlotreceivedPowerMote2.getDomainAxis();
        domainreceivedPowerMote2.setRange(0.0, 2700.0);
        domainreceivedPowerMote2.setTickUnit(new NumberTickUnit(200));
        domainreceivedPowerMote2.setVerticalTickLabels(true);
        NumberAxis rangereceivedPowerMote2 = (NumberAxis) xyPlotreceivedPowerMote2.getRangeAxis();
        rangereceivedPowerMote2.setRange(-85, 0.0);
        rangereceivedPowerMote2.setTickUnit(new NumberTickUnit(4));

        /*
        Create charts with adaptation of node 0
         */
        if(adaption){
            XYSeriesCollection dataPOW1 = new XYSeriesCollection();
            XYSeries seriesPOW1 = new XYSeries("POWERSETTING");
            Double usedPower = 0.0;

            XYSeriesCollection dataRECPOW1 = new XYSeriesCollection();
            LinkedList<LinkedList<LoraTransmission>> RECPOW1 = new LinkedList<>();

            //setting the data for the naive adaptation
            int counter = 0;
            for (LoraTransmission transmission : transmissionsMote0.get(0)) {
                seriesPOW1.add(counter, powerSetting1.get(counter));
                usedPower += Math.pow(powerSetting1.get(counter),2);
                Boolean placed = false;
                for (int i = 0; i < RECPOW1.size(); i++) {
                    if (RECPOW1.get(i).get(0).getReceiver() == HighestPower1.get(counter).getReceiver()) {
                        placed = true;
                        RECPOW1.get(i).add(HighestPower1.get(counter));
                    }
                }
                if (!placed) {
                    RECPOW1.add(new LinkedList<>());
                    RECPOW1.getLast().add(HighestPower1.get(counter));
                }
                counter++;
            }
            System.out.println("The total used power (sum of the squares of the transmission power): "+ usedPower);
            counter = 0;
            LinkedList<XYSeries> seriesLinkedList = new LinkedList<>();
            for (LinkedList<LoraTransmission> transmissions1 : RECPOW1) {
                XYSeries seriesRECPOW = new XYSeries(transmissions1.getFirst().getReceiver().toString());
                for (LoraTransmission transmission : transmissions1) {
                    seriesRECPOW.add(counter, transmission.getTransmissionPower());
                    counter++;
                }
                seriesLinkedList.add(seriesRECPOW);
            }
            dataRECPOW1.addSeries(seriesLinkedList.get(1));
            dataRECPOW1.addSeries(seriesLinkedList.get(2));
            dataRECPOW1.addSeries(seriesLinkedList.get(3));
            dataRECPOW1.addSeries(seriesLinkedList.get(0));
            dataPOW1.addSeries(seriesPOW1);
            JFreeChart powersettingChart1 = ChartFactory.createScatterPlot(
                    null,// chart title
                    "Distance travelled in meter(/10)", // x axis label
                    "Power setting", // y axis label
                    dataPOW1, // data
                    PlotOrientation.VERTICAL,
                    true, // include legend
                    true, // tooltips
                    false // urls
            );

            XYPlot xyPlotpowersettingChart1 = (XYPlot) powersettingChart1.getPlot();
            xyPlotpowersettingChart1.setDomainCrosshairVisible(true);
            xyPlotpowersettingChart1.setRangeCrosshairVisible(true);
            NumberAxis domainpowersettingChart1 = (NumberAxis) xyPlotpowersettingChart1.getDomainAxis();
            domainpowersettingChart1.setRange(0.0, 270.0);
            domainpowersettingChart1.setTickUnit(new NumberTickUnit(20));
            domainpowersettingChart1.setVerticalTickLabels(true);
            NumberAxis rangepowersettingChart1 = (NumberAxis) xyPlotpowersettingChart1.getRangeAxis();
            rangepowersettingChart1.setRange(0.0, 15.0);
            rangepowersettingChart1.setTickUnit(new NumberTickUnit(1));

            ValueMarker marker1 = new ValueMarker(46);  // position is the value on the axis
            marker1.setPaint(Color.black);
            marker1.setStroke(new  BasicStroke(3));


            ValueMarker marker2 = new ValueMarker(130);  // position is the value on the axis
            marker2.setPaint(Color.black);
            marker2.setStroke(new  BasicStroke(3));


            ValueMarker marker3 = new ValueMarker(187);  // position is the value on the axis
            marker3.setPaint(Color.black);
            marker3.setStroke(new  BasicStroke(3));


            XYPlot powersettingplot = (XYPlot) powersettingChart1.getPlot();
            powersettingplot.addDomainMarker(marker1);
            powersettingplot.addDomainMarker(marker2);
            powersettingplot.addDomainMarker(marker3);

            JFreeChart HighestSignalChart = ChartFactory.createScatterPlot(
                    null, // chart title
                    "Distance travelled in meter(/10)", // x axis label
                    "Received signal in dB", // y axis label
                    dataRECPOW1, // data
                    PlotOrientation.VERTICAL,
                    true, // include legend
                    true, // tooltips
                    false // urls
            );
            XYPlot xyPlotHighestSignal = (XYPlot) HighestSignalChart.getPlot();
            xyPlotHighestSignal.setDomainCrosshairVisible(true);
            xyPlotHighestSignal.setRangeCrosshairVisible(true);
            NumberAxis domainHighestSignal = (NumberAxis) xyPlotHighestSignal.getDomainAxis();
            domainHighestSignal.setRange(0.0, 270.0);
            domainHighestSignal.setTickUnit(new NumberTickUnit(20));
            domainHighestSignal.setVerticalTickLabels(true);
            NumberAxis rangeHighestSignal = (NumberAxis) xyPlotHighestSignal.getRangeAxis();
            rangeHighestSignal.setRange(-62, 0.0);
            rangeHighestSignal.setTickUnit(new NumberTickUnit(4));

            JFrame frame2 = new JFrame("Power setting");
            ChartPanel HighestSignalChartpanel = new ChartPanel(HighestSignalChart);
            HighestSignalChartpanel.setPreferredSize(new java.awt.Dimension(1000, 500));
            frame2.getContentPane().add(HighestSignalChartpanel, BorderLayout.NORTH);
            ChartPanel powersettingChart1panel = new ChartPanel(powersettingChart1);
            powersettingChart1panel.setPreferredSize(new java.awt.Dimension(1000, 350));
            frame2.getContentPane().add( powersettingChart1panel, BorderLayout.SOUTH);
            frame2.pack();
            frame2.setVisible(true);
            

            ChartFrame frame3 = new ChartFrame("Highest received signal", HighestSignalChart);
            frame3.pack();
            frame3.setVisible(true);
        }



        JFrame frame1 = new JFrame("received signals");
        ChartPanel HighestSignalChartpanel = new ChartPanel(receivedPowerChartMote0);
        HighestSignalChartpanel.setPreferredSize(new java.awt.Dimension(1000, 500));
        frame1.getContentPane().add(HighestSignalChartpanel, BorderLayout.NORTH);
        ChartPanel powersettingChart1panel = new ChartPanel(receivedPowerChartMote2);
        powersettingChart1panel.setPreferredSize(new java.awt.Dimension(1000, 500));
        frame1.getContentPane().add( powersettingChart1panel, BorderLayout.SOUTH);
        frame1.pack();
        frame1.setVisible(true);
        /*
        ChartFrame frame1 = new ChartFrame("Simulated environment", receivedPowerChartMote0);
        frame1.pack();
        frame1.setVisible(true);
        */




    }
    /*
    The naïve adaptation for our paper
     */
    private static LinkedList<Double> algorithmBuffer = new LinkedList<>();
    private static LoraTransmission naiveAdaptionAlgorithm(Mote mote){
        LinkedList<LoraTransmission> lastTransmissions = new LinkedList<>();
        for(Gateway gateway :mote.getEnvironment().getGateways()){
            Boolean placed = false;
            for(int i = gateway.getReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).size()-1; i>=0 && !placed; i--) {
                if(gateway.getReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).get(i).getSender() == mote) {
                    lastTransmissions.add(gateway.getReceivedTransmissions(gateway.getEnvironment().getNumberOfRuns()-1).getLast());
                    placed = true;
                }
            }
        }
        LoraTransmission bestTransmission = lastTransmissions.getFirst();
        for (LoraTransmission transmission : lastTransmissions){
            if(transmission.getTransmissionPower() > bestTransmission.getTransmissionPower())
                bestTransmission = transmission;
        }
        algorithmBuffer.add(bestTransmission.getTransmissionPower());
        if(algorithmBuffer.size() ==5){
            double average = 0;
            for (Double power : algorithmBuffer){
                average+= power;
            }
            average = average /5;
            if(average > -42) {
                if (mote.getTransmissionPower() > -3)
                    mote.setTransmissionPower(mote.getTransmissionPower() - 1);
            }
            if(average < -48){
                if(mote.getTransmissionPower() < 14)
                    mote.setTransmissionPower(mote.getTransmissionPower() +1);
            }
            algorithmBuffer = new LinkedList<>();
        }
        return bestTransmission;
    }
    /*
    A more advanced yet unfinished adaptation algorithm
     */
    private static void adaptationAlgorithmRobbe(LinkedList<LoraTransmission> packets){
        if(packets.get(0).getSender().getClass() != Mote.class)
            return;
        Mote endNode = (Mote) packets.get(0).getSender();
        Integer POW = endNode.getTransmissionPower();
        Integer SF = endNode.getSF();
        double DesiredPER = 0;
        Gateway bestGateway;
        double bestSNREstimate = Double.MIN_VALUE;
        double PEREstimated = 1;
        Gateway gateway;
        for(LoraTransmission transmission : packets){
            gateway = (Gateway) transmission.getReceiver();
            transmission.getTransmissionPower();
            double SNREstimate = 0;
            double PERAtGateway = estimatePER(SNREstimate,SF);
            PEREstimated = PEREstimated*PERAtGateway;
            if(SNREstimate>bestSNREstimate){
                bestSNREstimate = SNREstimate;
                bestGateway = gateway;
            }
        }
        if(PEREstimated>DesiredPER){
            if (POW<14)
                endNode.setTransmissionPower(endNode.getTransmissionPower() + 1);
            else if(SF < 12)
                endNode.setSF(endNode.getSF() + 1);
        }
        else{
            if(SF > 1 && estimatePER(bestSNREstimate-SNRDropSFChange(SF),SF)<DesiredPER)
                endNode.setSF(endNode.getSF() - 1);
            else if(POW > -3 && estimatePER(bestSNREstimate-SNRDropPOWChange(POW),SF)<DesiredPER)
                endNode.setTransmissionPower(endNode.getTransmissionPower() - 1);
        }
    }

    private static double estimatePER(double SNR, Integer SF){
        return 0;
    }

    private static double SNRDropSFChange(Integer SF){
        return 0;
    }
    private static double SNRDropPOWChange(Integer POW){
        return 0;
    }

    /**
     * A function that moves a mote to a geoposition 1 step ans returns if the note has moved.
     * @param position
     * @param mote
     * @param mapzero
     * @return If the node has moved
     */
    private static Boolean moveMote(GeoPosition position, Mote mote, GeoPosition mapzero){
        Integer xPos = toMapXCoordinate(position,mapzero);
        Integer yPos = toMapYCoordinate(position,mapzero);
        if(Integer.signum(xPos - mote.getXPos()) != 0 || Integer.signum(yPos - mote.getYPos()) != 0){
            if(Math.abs(mote.getXPos() - xPos) >= Math.abs(mote.getYPos() - yPos)){
                mote.setXPos(mote.getXPos()+ Integer.signum(xPos - mote.getXPos()));

            }
            else{
                mote.setYPos(mote.getYPos()+ Integer.signum(yPos - mote.getYPos()));
            }
            return true;
        }
        return false;
    }

    private static Integer toMapXCoordinate(GeoPosition geoPosition, GeoPosition mapzero){
        return (int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),mapzero.getLatitude(), geoPosition.getLongitude()));
    }

    private static Integer toMapYCoordinate(GeoPosition geoPosition, GeoPosition mapzero){
        return (int)Math.round(1000* Environment.distance(mapzero.getLatitude(),mapzero.getLongitude(),geoPosition.getLatitude(), mapzero.getLongitude()));
    }


}
