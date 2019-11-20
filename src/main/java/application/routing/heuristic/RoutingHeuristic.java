package application.routing.heuristic;

import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;

public interface RoutingHeuristic {
    double calculateHeuristic(HeuristicEntry entry);

    class HeuristicEntry {
        public GraphStructure graph;
        public Connection connection;
        public GeoPosition destination;

        public HeuristicEntry(GraphStructure graph, Connection connection, GeoPosition destination) {
            this.graph = graph;
            this.connection = connection;
            this.destination = destination;
        }
    }
}
