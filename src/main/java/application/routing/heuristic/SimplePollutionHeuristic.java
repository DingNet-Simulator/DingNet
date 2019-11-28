package application.routing.heuristic;

import application.pollution.PollutionGrid;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;


/**
 * A simple routing heuristic which also takes the pollution over a given connection into account.
 */
public class SimplePollutionHeuristic implements RoutingHeuristic {
    private final PollutionGrid pollutionGrid;

    public SimplePollutionHeuristic(PollutionGrid pollutionGrid) {
        this.pollutionGrid = pollutionGrid;
    }

    @Override
    public double calculateHeuristic(HeuristicEntry entry) {
        GeoPosition begin = entry.graph.getWayPoint(entry.connection.getFrom());
        GeoPosition end = entry.graph.getWayPoint(entry.connection.getTo());

        double pollutionValue = this.pollutionGrid.getPollutionLevel(MapHelper.meanPosition(begin, end)).getPollutionFactor();

        // The lower the pollution level, the better the heuristic
        return pollutionValue * MapHelper.distance(begin, end);
    }
}
