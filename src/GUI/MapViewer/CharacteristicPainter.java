package GUI.MapViewer;

import IotDomain.Characteristic;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class CharacteristicPainter implements Painter<JXMapViewer> {
    private GeoPosition position;
    private Characteristic characteristic;

    public CharacteristicPainter(GeoPosition position, Characteristic characteristic){
        this.position = position;
        this.characteristic = characteristic;

    }
    @Override
    public void paint(Graphics2D g, JXMapViewer jxMapViewer, int i, int i1) {
        Color color = characteristic.getColor();
        g = (Graphics2D) g.create();

        // convert from viewport to world bitmap
        Rectangle rect = jxMapViewer.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (true)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        Point2D pt = jxMapViewer.getTileFactory().geoToPixel(position, jxMapViewer.getZoom());
        Ellipse2D.Double circle = new Ellipse2D.Double(pt.getX(), pt.getY(), 15, 15);
        g.setColor(color);
        g.fill(circle);
    }
}
