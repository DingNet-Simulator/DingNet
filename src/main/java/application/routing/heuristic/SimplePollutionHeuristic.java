package application.routing.heuristic;

import application.pollution.PollutionGrid;
import org.jxmapviewer.viewer.GeoPosition;
import util.MapHelper;

public class SimplePollutionHeuristic implements RoutingHeuristic {
    @Override
    public double calculateHeuristic(HeuristicEntry entry) {
        GeoPosition begin = entry.graph.getWayPoint(entry.connection.getFrom());
        GeoPosition end = entry.graph.getWayPoint(entry.connection.getTo());

        double pollutionValue = PollutionGrid.getInstance()
            .getPollutionLevel(MapHelper.meanPosition(begin, end)).getPollutionFactor();

        double factor = (0.0 <= pollutionValue && pollutionValue < 0.2) ? 1 :
                        (0.2 <= pollutionValue && pollutionValue < 0.4) ? 2 :
                        (0.4 <= pollutionValue && pollutionValue < 0.6) ? 3 : 10;

        // The lower the pollution level, the better the heuristic
        return factor * MapHelper.distance(begin, end);
    }
}
