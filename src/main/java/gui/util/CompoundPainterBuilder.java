package gui.util;

import application.pollution.PollutionGrid;
import application.routing.RoutingApplication;
import gui.mapviewer.*;
import iot.Environment;
import iot.SimulationRunner;
import iot.networkentity.UserMote;
import it.unibo.acdingnet.protelis.DrawableNodeInfo;
import it.unibo.acdingnet.protelis.ProtelisApp;
import it.unibo.acdingnet.protelis.util.gui.ProtelisPulltionGridPainter;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import util.GraphStructure;
import util.SettingsReader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompoundPainterBuilder {
    private List<Painter<JXMapViewer>> painters = new ArrayList<>();

    /**
     * Include painters for motes in the builder.
     * @param environment The environment in which the motes are stored.
     * @return The current object.
     */
    public CompoundPainterBuilder withMotes(Environment environment) {
        Map<MoteWayPoint, Integer> motes = GUIUtil.getMoteMap(environment);

        painters.add(new MotePainter<>().setWaypoints(motes.keySet()));
        painters.add(new NumberPainter<>(NumberPainter.Type.MOTE).setNumberWaypoints(motes));
        return this;
    }

    /**
     * Include painters for gateways in the builder.
     * @param environment The environment in which the gateways are stored.
     * @return The current object.
     */
    public CompoundPainterBuilder withGateways(Environment environment) {
        Map<Waypoint, Integer> gateways = GUIUtil.getGatewayMap(environment);

        painters.add(new GatewayPainter<>().setWaypoints(gateways.keySet()));
        painters.add(new NumberPainter<>(NumberPainter.Type.GATEWAY).setNumberWaypoints(gateways));
        return this;
    }

    /**
     * Include painters for all waypoints in the builder.
     * @param graph The graph which stores all the waypoints.
     * @param includeNumbers Boolean indicating if the Ids of the waypoints should also be painted.
     * @return The current object.
     */
    public CompoundPainterBuilder withWaypoints(GraphStructure graph, boolean includeNumbers) {
        var waypoints = graph.getWayPoints();

        painters.add(new WayPointPainter<>()
            .setWaypoints(waypoints.values()
                .stream()
                .map(DefaultWaypoint::new)
                .collect(Collectors.toSet()))
        );

        if (includeNumbers) {
            painters.add(new NumberPainter<>(NumberPainter.Type.WAYPOINT)
                .setNumberWaypoints(waypoints.entrySet().stream()
                    .collect(Collectors.toMap(e -> new DefaultWaypoint(e.getValue()), e -> e.getKey().intValue())))
            );
        }
        return this;
    }

    /**
     * Include painters for all connections in the builder.
     * @param graph The graph which contains all the connections.
     * @return The current object.
     */
    public CompoundPainterBuilder withConnections(GraphStructure graph) {
        Color lineColor = SettingsReader.getInstance().getConnectionLineColor();
        int lineSize = SettingsReader.getInstance().getConnectionLineSize();

        graph.getConnections().values().forEach(c -> painters.add(new LinePainter(List.of(graph.getWayPoint(c.getFrom()), graph.getWayPoint(c.getTo())), lineColor, lineSize)));
        return this;
    }

    /**
     * Include painters for the borders of the environment in the builder.
     * @param environment The environment which has a bounded x and y value.
     * @return The current object.
     */
    public CompoundPainterBuilder withBorders(Environment environment) {
        painters.addAll(GUIUtil.getBorderPainters(environment));
        return this;
    }

    /**
     * Include painters for the paths of the motes in the builder.
     * @param environment The environment which contains all the motes.
     * @return The current object.
     */
    public CompoundPainterBuilder withMotePaths(Environment environment) {
        Color lineColor = SettingsReader.getInstance().getMotePathLineColor();
        int lineSize = SettingsReader.getInstance().getMotePathLineSize();

        environment.getMotes().forEach(m -> painters.add(new LinePainter(m.getPath().getWayPoints(), lineColor, lineSize)));
        return this;
    }

    /**
     * Include a painter of a pollution grid in the builder.
     * @param pollutionGrid The pollution grid which should be painted.
     * @return The current object.
     */
    public CompoundPainterBuilder withPollutionGrid(PollutionGrid pollutionGrid) {
        if (pollutionGrid != null) {
            painters.add(new PollutionGridPainter(pollutionGrid));
        }
        return this;
    }

    /**
     * Include a painter for the stored routing path (at {@code routingApplication}) for the currently active user mote (if present)
     * @param routingApplication The routing application which stores the user mote's path.
     * @return The current object.
     */
    public CompoundPainterBuilder withRoutingPath(RoutingApplication routingApplication) {
        if (routingApplication != null) {
            Color lineColor = SettingsReader.getInstance().getRoutingPathLineColor();
            int lineSize = SettingsReader.getInstance().getRoutingPathLineSize();

            // Optional painter of the complete path
            SimulationRunner.getInstance().getEnvironment().getMotes().stream()
                .filter(m -> m instanceof UserMote && ((UserMote) m).isActive())
                .findFirst()
                .ifPresent(m -> painters.add(new LinePainter(routingApplication.getRoute(m), lineColor, lineSize)));
        }
        return this;
    }

    public CompoundPainterBuilder withProtelisApp(ProtelisApp protelisApp) {
        if (protelisApp != null) {
            painters.add(new ProtelisPulltionGridPainter(protelisApp.getPollutionGrid()));
            var points = protelisApp.getDrawableNode();
            var painter = new WayPointPainter<>(Color.BLACK, 10)
                .setWaypoints(points
                    .stream()
                    .map(DrawableNodeInfo::getPosition)
                    .map(DefaultWaypoint::new)
                    .collect(Collectors.toSet())
                );
            painters.add(painter);
            var paintNumber = new TextPainter<>(TextPainter.Type.WAYPOINT)
                .setWaypoints(points.stream()
                    .collect(Collectors.toMap(w ->
                        new DefaultWaypoint(w.getPosition()),
                        it -> it.getCurrentTemp() + "\u00ba/" + it.getDesiredTemp() + "\u00ba/" + it.getMaxTemp() + "\u00ba")));
            painters.add(paintNumber);
        }

        return this;
    }


    /**
     * Build a {@link CompoundPainter} which has all the painters added to this builder.
     * @return A {@link CompoundPainter} with all the specified painters.
     */
    public CompoundPainter<JXMapViewer> build() {
        return new CompoundPainter<>(painters);
    }
}
