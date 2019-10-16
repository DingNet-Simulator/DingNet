package util;

import org.jxmapviewer.viewer.GeoPosition;

public class MapHelper {
    public static double distance(GeoPosition pos1, GeoPosition pos2) {
        double lat1 = pos1.getLatitude(), lon1 = pos1.getLongitude();
        double lat2 = pos2.getLatitude(), lon2 = pos2.getLatitude();
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }
    public static int toMapXCoordinate(GeoPosition geoPosition, GeoPosition offset){
        return (int)Math.round(1000*distance(offset, geoPosition));
    }
    /**
     * Converts a GeoPostion to an y-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The y-coordinate on the map of the GeoPosition.
     */
    public static int toMapYCoordinate(GeoPosition geoPosition, GeoPosition offset){
        return (int)Math.round(1000*distance(offset, geoPosition));
    }
}
