package util;

import org.jxmapviewer.viewer.GeoPosition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Path implements Iterable<GeoPosition> {
    private List<Connection> connections;

    public Path() { this(new ArrayList<>()); }

    public Path(List<Connection> connections) {
        this.connections = connections;
    }

    public LinkedList<GeoPosition> getWayPoints() {
        LinkedList<GeoPosition> result = new LinkedList<>();

        for (var con : this.connections) {
            result.add(con.getFrom());
        }

        result.add(this.connections.get(this.connections.size() - 1).getTo());

        return result;
    }

    public Iterator<GeoPosition> iterator() {
        return getWayPoints().iterator();
    }

    public void addConnection(Connection connection) {
        this.connections.add(connection);
    }
}
