package gui.util;

import gui.mapviewer.LinePainter;
import gui.mapviewer.MoteWayPoint;
import iot.Environment;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import util.MapHelper;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GUIUtil {
    public static List<LinePainter> getBorderPainters(int maxX, int maxY, GeoPosition origin) {
        List<LinePainter> painters = new ArrayList<>();

        painters.add(new LinePainter(List.of(MapHelper.toGeoPosition(0, 0, origin), MapHelper.toGeoPosition(0, maxY, origin))));
        painters.add(new LinePainter(List.of(MapHelper.toGeoPosition(0, 0, origin), MapHelper.toGeoPosition(maxX, 0, origin))));
        painters.add(new LinePainter(List.of(MapHelper.toGeoPosition(maxX, 0, origin), MapHelper.toGeoPosition(maxX, maxY, origin))));
        painters.add(new LinePainter(List.of(MapHelper.toGeoPosition(0, maxY, origin), MapHelper.toGeoPosition(maxX, maxY, origin))));

        return painters;
    }

    public static Map<MoteWayPoint, Integer> getMoteMap(Environment environment) {
        Map<MoteWayPoint, Integer> map = new HashMap<>();
        var motes = environment.getMotes();

        var wraps = motes.stream()
            .map(m -> {
                var pos = MapHelper.toGeoPosition(m.getPosInt(), environment.getMapOrigin());
                if (m instanceof UserMote) {
                    return new MoteWayPoint(pos, true, ((UserMote)m).isActive());
                }
                return new MoteWayPoint(pos);
            })
            .collect(Collectors.toList());

        IntStream.range(0, wraps.size())
            .forEach(i -> map.put(wraps.get(i), i+1));
        return map;
    }

    public static Map<Waypoint, Integer> getGatewayMap(Environment environment) {
        Map<Waypoint, Integer> map = new HashMap<>();
        var gateways = environment.getGateways();

        IntStream.range(0, gateways.size())
            .forEach(i -> map.put(new DefaultWaypoint(MapHelper.toGeoPosition(gateways.get(i).getPosInt(), environment.getMapOrigin())), i+1));

        return map;
    }

    public static File getOutputFile(File givenFile, String extension) {
        String name = givenFile.getName();

        if (name.length() < extension.length() + 2 || !name.substring(name.length() - (extension.length()+1)).equals("." + extension)) {
            // Either the filename is too short, or it is still missing the (right) extension
            return new File(givenFile.getPath() + "." + extension);
        } else {
            return new File(givenFile.getPath());
        }
    }

    public static void updateTextFieldCoordinate(JTextField field, double value, String alt1, String alt2) {
        field.setText(coordinateToString(value, alt1, alt2));
    }


    public static void updateLabelCoordinateLat(JLabel label, double value) {
        updateLabelCoordinate(label, value, "E", "W");
    }

    public static void updateLabelCoordinateLon(JLabel label, double value) {
        updateLabelCoordinate(label, value, "N", "S");
    }

    private static void updateLabelCoordinate(JLabel label, double value, String alt1, String alt2) {
        label.setText(coordinateToString(value, alt1, alt2));
    }

    private static String coordinateToString(double value, String alt1, String alt2) {
        // TODO clean up magic numbers
        int degrees = (int) Math.floor(value);
        int minutes = (int) Math.floor((value - degrees) * 60);
        double seconds = (double) Math.round(((value - degrees) * 60 - minutes) * 60 * 1000d) / 1000d;

        return String.format("%s %d° %d' %.2f\"", value > 0 ? alt1 : alt2, degrees, minutes, seconds);
    }
}
