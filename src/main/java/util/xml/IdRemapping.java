package util.xml;

import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;

import java.util.HashMap;
import java.util.Map;

public class IdRemapping {

    private Map<Long, Long> IdMappingWayPoints;
    private Map<Long, Long> IdMappingConnections;

    private Map<Long, GeoPosition> wayPoints;
    private Map<Long, Connection> connections;

    private long newWayPointId;
    private long newConnectionId;


    public IdRemapping() {
        this.reset();
    }

    public long addWayPoint(long originalId, GeoPosition pos) {
        IdMappingWayPoints.put(originalId, newWayPointId);
        wayPoints.put(newWayPointId, pos);

        return newWayPointId++;
    }


    public long addConnection(long originalId, Connection conn) {
        IdMappingConnections.put(originalId, newConnectionId);
        connections.put(newConnectionId, conn);

        return newConnectionId++;
    }


    public long getNewWayPointId(long originalId) {
        return IdMappingWayPoints.get(originalId);
    }

    public long getNewConnectionId(long originalId) {
        return IdMappingConnections.get(originalId);
    }



    public GeoPosition getWayPointWithNewId(long newId) {
        return wayPoints.get(newId);
    }

    public GeoPosition getWayPointWithOriginalId(long originalId) {
        return wayPoints.get(IdMappingWayPoints.get(originalId));
    }


    public Connection getConnectionWithOriginalId(long originalId) {
        return connections.get(IdMappingConnections.get(originalId));
    }

    public Map<Long, GeoPosition> getWayPoints() {
        return wayPoints;
    }

    public Map<Long, Connection> getConnections() {
        return connections;
    }


    public void reset() {
        IdMappingWayPoints = new HashMap<>();
        IdMappingConnections = new HashMap<>();
        wayPoints = new HashMap<>();
        connections = new HashMap<>();

        newWayPointId = 1;
        newConnectionId = 1;
    }
}
