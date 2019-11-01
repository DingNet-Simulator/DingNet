package GUI.MapViewer;

import IotDomain.Environment;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import util.MapHelper;
import util.pollution.PollutionGrid;

import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.awt.geom.Point2D;

public class PollutionGridPainter implements Painter<JXMapViewer> {
    private PollutionGrid pollutionGrid;
    private Environment environment;

    private final float TRANSPARENCY = .3f;

    public PollutionGridPainter(Environment environment) {
        this.pollutionGrid = PollutionGrid.getInstance();
        this.environment = environment;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
        g = (Graphics2D) g.create();

        // convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int maxX = environment.getMaxXpos() + 1;
        int maxY = environment.getMaxYpos() + 1;

        // Can decide to be more fine grained later on
        final int DIVISION = 10;
        TileFactory factory = map.getTileFactory();
        MapHelper mapHelper = MapHelper.getInstance();

        for (int i = 0; i < DIVISION; i++) {
            for (int j = 0; j < DIVISION; j++) {
                // The starting position of the square is specified by the point in the upper left corner
                Point2D topLeft = factory.geoToPixel(mapHelper.toGeoPosition(
                    Math.round(i * ((float) maxX / DIVISION)),
                    Math.round((j+1) * ((float) maxY / DIVISION))
                ), map.getZoom());
                Point2D bottomRight = factory.geoToPixel(mapHelper.toGeoPosition(
                    Math.round((i+1) * ((float) maxX / DIVISION)),
                    Math.round(j * ((float) maxY / DIVISION))
                ), map.getZoom());
                GeoPosition middle = mapHelper.toGeoPosition((int) ((i+.5) * maxX / DIVISION), (int) ((j+.5) * maxY / DIVISION));

                float airQuality = (float) pollutionGrid.getPollutionLevel(middle).getPollutionFactor();
                g.setColor(this.getColor(airQuality));
                g.fill(new Rectangle2D.Double(topLeft.getX(), topLeft.getY(),
                    Math.abs(topLeft.getX() - bottomRight.getX()), Math.abs(topLeft.getY() - bottomRight.getY())));
            }
        }

        g.dispose();
    }

    private Color getColor(float airQuality) {
        if (airQuality <= 0.2) {
            return new Color(0f, .3f - airQuality, 0f, TRANSPARENCY);
        } else if (airQuality <= 0.5) {
            return new Color(.3f + airQuality, .1f + airQuality, 0f, TRANSPARENCY);
        } else {
            return new Color(airQuality, 0f, 0f, TRANSPARENCY);
        }

    }
}
