package GUI.util;

import GUI.MapViewer.LinePainter;
import util.MapHelper;

import java.util.ArrayList;
import java.util.List;

public class GUIUtil {
    public static List<LinePainter> getBorderPainters(int maxX, int maxY) {
        var mapHelper = MapHelper.getInstance();
        List<LinePainter> painters = new ArrayList<>();

        painters.add(new LinePainter(List.of(mapHelper.toGeoPosition(0, 0), mapHelper.toGeoPosition(0, maxY))));
        painters.add(new LinePainter(List.of(mapHelper.toGeoPosition(0, 0), mapHelper.toGeoPosition(maxX, 0))));
        painters.add(new LinePainter(List.of(mapHelper.toGeoPosition(maxX, 0), mapHelper.toGeoPosition(maxX, maxY))));
        painters.add(new LinePainter(List.of(mapHelper.toGeoPosition(0, maxY), mapHelper.toGeoPosition(maxX, maxY))));

        return painters;
    }


}
