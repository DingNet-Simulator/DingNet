package util;

import org.jxmapviewer.viewer.GeoPosition;

public class Path {
    // TODO could maybe already store the region in which this path belongs (and what to do in case of multiple regions)
    // TODO similar idea for weights of the path (in case of A*)
    private GeoPosition from;
    private GeoPosition to;

    public Path(GeoPosition from, GeoPosition to) {
        this.from = from;
        this.to = to;
    }

    public GeoPosition getFrom() {
        return from;
    }

    public GeoPosition getTo() {
        return to;
    }
}
