package util;

import org.jetbrains.annotations.NotNull;
import org.jxmapviewer.viewer.GeoPosition;
import util.xml.IdRemapping;

import java.util.*;


/**
 * Class which represents a path of a mote.
 */
public class Path implements Iterable<GeoPosition> {
    // A list with waypoints of the path
    private ArrayList<GeoPosition> points;

    public Path(){
        this.points = new ArrayList<>();
    }

    public Path(List<GeoPosition> points) {
        this.points = new ArrayList<>(points);
    }

    public ArrayList<GeoPosition> getWayPoints() {
        return this.points;
    }

    @NotNull
    public Iterator<GeoPosition> iterator() {
        return getWayPoints().iterator();
    }


    /**
     * Set the path to a given list of positions.
     * @param positions The list of GeoPositions.
     */
    public void setPath(ArrayList<GeoPosition> positions) {
        this.points = positions;
    }

    public int size(){
        return points.size();
    }

    /**
     * Check if the path contains any waypoints.
     * @return True if no waypoints are present in the path.
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }


    /**
     * Get the first position in the path, if present.
     * @return Either the first position of the path if present, otherwise an empty Optional.
     */
    public Optional<GeoPosition> getSource() {
        return isEmpty() ? Optional.empty() : Optional.of(points.get(0));
    }


    /**
     * Get the last position in the path, if present.
     * @return Either the last position of the path if present, otherwise an empty Optional.
     */
    public Optional<GeoPosition> getDestination() {
        return isEmpty() ? Optional.empty() : Optional.of(points.get(points.size() - 1));
    }

    public Optional<GeoPosition> getNextPoint(GeoPosition actualPoint) {
        return getPoint(getWayPoints().indexOf(actualPoint)+1);
    }

    public Optional<GeoPosition> getPoint(int actualPointIndex) {
        if(actualPointIndex > -1 && actualPointIndex < getWayPoints().size())
            return Optional.of(getWayPoints().get(actualPointIndex));
        else
            return Optional.empty();
    }

    /**
     * Add a waypoint at the end of this path.
     * @param point The waypoint which is added at the end of the path.
     */
    public void addPosition(GeoPosition point) {
        this.points.add(point);
    }


    /**
     * Add a list of waypoints to the path
     * @param points A list of GeoPositions to be added.
     */
    public void addPositions(@NotNull List<GeoPosition> points) {
        this.points.addAll(points);
    }




    // NOTE: The following functions are only used during setup/saving of the configuration, not at runtime
}
