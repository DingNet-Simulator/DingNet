package util;

import de.westnordost.osmapi.map.data.*;
import de.westnordost.osmapi.map.handler.MapDataHandler;
import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import util.xml.IdRemapping;


public class MyMapDataHandler implements MapDataHandler {

    private static IdRemapping idRemapping = new IdRemapping();
    @Override
    public void handle(@NotNull BoundingBox bounds) {

    }

    @Override
    public void handle(@NotNull Node node) {
        idRemapping.addWayPoint(node.getId(),new GeoPosition(node.getPosition().getLatitude(),node.getPosition().getLongitude()));

    }

    @Override
    public void handle(Way way) {
        for (int i = 0; i < way.getNodeIds().size()-1; i++) {
            idRemapping.addConnection(way.getId(),new Connection(idRemapping.getNewWayPointId(way.getNodeIds().get(i)),idRemapping.getNewWayPointId(way.getNodeIds().get(i+1))));
            idRemapping.addConnection(-way.getId(),new Connection(idRemapping.getNewWayPointId(way.getNodeIds().get(i+1)),idRemapping.getNewWayPointId(way.getNodeIds().get(i))));
        }
    }

    @Override
    public void handle(Relation relation) {

    }

    public java.util.Map<Long, GeoPosition> getWayPoints(){
        return idRemapping.getWayPoints();
    }

    public java.util.Map<Long, Connection> getConnections(){
        return idRemapping.getConnections();
    }

}
