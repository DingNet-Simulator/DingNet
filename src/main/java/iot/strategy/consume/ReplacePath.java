package iot.strategy.consume;

import iot.lora.LoraWanPacket;
import iot.networkentity.Mote;
import util.MapHelper;

import java.util.stream.Collectors;

public class ReplacePath extends AddPositionToPath {

    @Override
    public void consume(Mote mote, LoraWanPacket packet) {
        var path = extractPath(packet);
        if (path.isEmpty()) {
            return;
        }
        var motePath = mote.getPath();
        if (motePath.isEmpty()) {
            mote.setPath(path);
            return;
        }
        if (motePath.getWayPoints().size() == 1 && MapHelper.equalsGeoPosition(motePath.getWayPoints().get(0), path.get(0))) {
            // Do NOT override the first position (slightly different due to floating point conversions
            path.remove(0);
            path.add(0, motePath.getWayPoints().get(0));
            motePath.setPath(path);
            return;
        }
        var nextPos = motePath.getNextPoint(mote.getPathPosition());
        if (nextPos.isPresent() && MapHelper.equalsGeoPosition(nextPos.get(), path.get(0))) {
            var newPath = motePath.getWayPoints()
                .stream()
                .limit(motePath.getWayPoints().indexOf(nextPos.get()))
                .collect(Collectors.toList());
            newPath.addAll(path);
            motePath.setPath(newPath);
            return;
        }
        var destPath = motePath.getDestination();
        if (destPath.isPresent() && MapHelper.equalsGeoPosition(destPath.get(), mote.getPathPosition())) {
            motePath.addPositions(path);
        }
    }
}
