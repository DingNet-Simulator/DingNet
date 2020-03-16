package gui.mapviewer;

import org.jxmapviewer.viewer.Waypoint;
import util.Pair;
import util.SettingsReader;

import java.util.Map;
import java.util.stream.Collectors;


public class NumberPainter<W extends Waypoint> extends TextPainter<W> {


    public NumberPainter(Type type) {
        super(type);

        setAntialiasing(SettingsReader.getInstance().useGUIAntialiasing());
        setCacheable(false);
    }

    /**
     * Sets the current set of waypoints to paint
     * @param waypoints the new Set of Waypoints to use
     */
    public TextPainter<W> setNumberWaypoints(Map<? extends W, Integer> waypoints) {
        super.setWaypoints(waypoints.entrySet().stream()
            .map(it -> new Pair<>(it.getKey(), it.getValue().toString()))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));
        return this;
    }

}

