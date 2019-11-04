package IotDomain.application;

import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import util.*;
import util.pollution.PollutionGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AStarRouter implements PathFinder {
    private PollutionGrid grid;
    private Function<Pair<Connection, GraphStructure>, Double> heuristicConnection;

    public AStarRouter() {
        this.grid = PollutionGrid.getInstance();

        this.heuristicConnection = (p) -> {
            Connection connection = p.getLeft();
            GraphStructure graph = p.getRight();
            GeoPosition begin = graph.getWayPoint(connection.getFrom());
            GeoPosition end = graph.getWayPoint(connection.getTo());


            double pollutionValue = grid.getPollutionLevel(MapHelper.meanPosition(begin, end)).getPollutionFactor();
            double factor = (0.0 <= pollutionValue && pollutionValue < 0.2) ? 1 :
                            (0.2 <= pollutionValue && pollutionValue < 0.4) ? 2 :
                            (0.4 <= pollutionValue && pollutionValue < 0.6) ? 3 : 10;

            // The lower the pollution level, the better the heuristic
            return factor * MapHelper.distance(begin, end);
        };
    }

    @Override
    public List<GeoPosition> retrievePath(GraphStructure graph, GeoPosition begin, GeoPosition end) {
        // NOTE: assumption -> the positions given are located at waypoints in the graph
        long beginWaypointId = graph.getClosestWayPointWithinRange(begin, 0.05)
            .orElseThrow(() -> new IllegalStateException("The mote position retrieved from the message is not located at a waypoint."));
        long endWaypointId = graph.getClosestWayPointWithinRange(end, 0.05)
            .orElseThrow(() -> new IllegalStateException("The destination position retrieved from the message is not located at a waypoint."));

        PriorityQueue<FringeEntry> fringe = new PriorityQueue<>();
        // Initialize the fringe by adding the first outgoing connections
        graph.getConnections().entrySet().stream()
            .filter(me -> me.getValue().getFrom() == beginWaypointId)
            .forEach(me -> fringe.add(new FringeEntry(
                List.of(me.getKey()),
                this.heuristicConnection.apply(new Pair<>(me.getValue(), graph))
            )));

        // Actual A* algorithm
        while (!fringe.isEmpty()) {
            FringeEntry current = fringe.poll();

            if (graph.getConnections().get(current.getLastConnectionId()).getTo() == endWaypointId) {
                return this.getPath(current.connections, graph);
            }

            // Explore the different outgoing connections from the last connection in the list
            long lastWaypointId = graph.getConnections().get(current.getLastConnectionId()).getTo();

            // Add the new possible paths (together with their new heuristic values) to the fringe
            graph.getOutgoingConnectionsById(lastWaypointId).stream()
                .filter(cId -> !current.connections.contains(cId)) // Filter out circular routes (i.e., check if connection already in existing path)
                .forEach((cId) -> {
                    List<Long> extendedPath = new ArrayList<>(current.connections);
                    extendedPath.add(cId);

                    double newHeuristicValue = current.heuristicValue
                        + this.heuristicConnection.apply(new Pair<>(graph.getConnections().get(cId), graph));

                    fringe.add(new FringeEntry(extendedPath, newHeuristicValue));
                });
        }

        throw new RuntimeException(String.format("Could not find a path from {%s} to {%s}", begin.toString(), end.toString()));
    }

    private List<GeoPosition> getPath(List<Long> connectionIds, GraphStructure graph) {
        List<GeoPosition> points = connectionIds.stream()
            .map(o -> graph.getConnections().get(o).getFrom())
            .map(graph::getWayPoint)
            .collect(Collectors.toList());

        // Don't forget the final waypoint
        long lastWaypointId = graph.getConnections().get(connectionIds.get(connectionIds.size()-1)).getTo();
        points.add(graph.getWayPoint(lastWaypointId));

        return points;
    }

    private static class FringeEntry implements Comparable<FringeEntry> {
        List<Long> connections;
        double heuristicValue;

        FringeEntry(List<Long> connections, double heuristicValue) {
            this.connections = connections;
            this.heuristicValue = heuristicValue;
        }

        long getLastConnectionId() {
            return connections.get(connections.size() - 1);
        }

        @Override
        public int compareTo(@NotNull FringeEntry fringeEntry) {
            return Double.compare(this.heuristicValue, fringeEntry.heuristicValue);
        }
    }
}
