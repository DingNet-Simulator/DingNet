package gui.util;

import application.routing.RoutingApplication;
import gui.mapviewer.*;
import iot.Environment;
import iot.networkentity.UserMote;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.Waypoint;
import util.GraphStructure;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompoundPainterBuilder {
    private List<Painter<JXMapViewer>> painters = new ArrayList<>();

    public CompoundPainterBuilder withMotes(Environment environment) {
        Map<MoteWayPoint, Integer> motes = GUIUtil.getMoteMap(environment);

        painters.add(new MotePainter<>().setWaypoints(motes.keySet()));
        painters.add(new NumberPainter<>(NumberPainter.Type.MOTE).setWaypoints(motes));
        return this;
    }

    public CompoundPainterBuilder withGateways(Environment environment) {
        Map<Waypoint, Integer> gateways = GUIUtil.getGatewayMap(environment);

        painters.add(new GatewayPainter<>().setWaypoints(gateways.keySet()));
        painters.add(new NumberPainter<>(NumberPainter.Type.GATEWAY).setWaypoints(gateways));
        return this;
    }

    public CompoundPainterBuilder withWaypoints(GraphStructure graph, boolean includeNumbers) {
        var waypoints = graph.getWayPoints();

        painters.add(new WayPointPainter<>().setWaypoints(waypoints.values().stream()
            .map(DefaultWaypoint::new)
            .collect(Collectors.toSet()))
        );

        if (includeNumbers) {
            painters.add(new NumberPainter<>(NumberPainter.Type.WAYPOINT)
                .setWaypoints(waypoints.entrySet().stream()
                    .collect(Collectors.toMap(e -> new DefaultWaypoint(e.getValue()), e -> e.getKey().intValue())))
            );
        }
        return this;
    }

    public CompoundPainterBuilder withConnections(GraphStructure graph) {
        var connections = graph.getConnections().values();
        for (var conn : connections) {
            painters.add(new LinePainter(
                List.of(graph.getWayPoint(conn.getFrom()), graph.getWayPoint(conn.getTo())), Color.RED, 2
            ));
        }
        return this;
    }

    public CompoundPainterBuilder withBorders(Environment environment) {
        painters.addAll(GUIUtil.getBorderPainters(environment.getMaxXpos(), environment.getMaxYpos(), environment.getMapOrigin()));
        return this;
    }

    public CompoundPainterBuilder withMotePaths(Environment environment) {
        environment.getMotes().forEach(m -> painters.add(new LinePainter(m.getPath().getWayPoints(), Color.RED, 1)));
        return this;
    }

    public CompoundPainterBuilder withPollutionGrid(Environment environment) {
        painters.add(new PollutionGridPainter(environment));
        return this;
    }

    public CompoundPainterBuilder withRoutingPath(Environment environment, RoutingApplication routingApplication) {
        // Optional painter of the complete path
        environment.getMotes().stream()
            .filter(m -> m instanceof UserMote && ((UserMote) m).isActive())
            .findFirst()
            .ifPresent(m -> painters.add(new LinePainter(routingApplication.getRoute(m), Color.CYAN, 2)));
        return this;
    }


    public CompoundPainter<JXMapViewer> build() {
        return new CompoundPainter<>(painters);
    }
}
