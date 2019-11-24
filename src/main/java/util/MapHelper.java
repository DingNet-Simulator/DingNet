package util;

import org.jxmapviewer.viewer.GeoPosition;

public class MapHelper {
    private GeoPosition origin;

    public MapHelper(GeoPosition origin) {
        this.origin = origin;
    }

    /**
     * Converts a GeoPostion to an x-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The x-coordinate on the map of the GeoPosition.
     */
    public int toMapXCoordinate(GeoPosition geoPosition) {
        return (int) Math.round(1000 * distance(this.origin, new GeoPosition(this.origin.getLatitude(), geoPosition.getLongitude())));
    }

    /**
     * Converts a GeoPostion to an y-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The y-coordinate on the map of the GeoPosition.
     */
    public int toMapYCoordinate(GeoPosition geoPosition) {
        return (int) Math.round(1000 * distance(this.origin, new GeoPosition(geoPosition.getLatitude(), this.origin.getLongitude())));
    }

    /**
     * Converts a GeoPosition to an coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The coordinate on the map of the GeoPosition.
     */
    public Pair<Integer,Integer> toMapCoordinate(GeoPosition geoPosition) {
        return new Pair<>(toMapXCoordinate(geoPosition), toMapYCoordinate(geoPosition));
    }

    /**
     * A function to calculate the longitude from a given x-coordinate on the map.
     * @param x  The x-coordinate of the entity.
     * @return The longitude of the given x-coordinate
     */
    public double toLongitude(int x) {
        double longitude;
        if (x > 0) {
            longitude = x;
            longitude = longitude / 1000;
            longitude = longitude / 1.609344;
            longitude = longitude / (60 * 1.1515);
            longitude = Math.toRadians(longitude);
            longitude = Math.cos(longitude);
            longitude = longitude - Math.sin(Math.toRadians(this.origin.getLatitude())) * Math.sin(Math.toRadians(this.origin.getLatitude()));
            longitude = longitude / (Math.cos(Math.toRadians(this.origin.getLatitude())) * Math.cos(Math.toRadians(this.origin.getLatitude())));
            longitude = Math.acos(longitude);
            longitude = Math.toDegrees(longitude);
            longitude = longitude + this.origin.getLongitude();
        } else {
            longitude = this.origin.getLongitude();
        }
        return longitude;
    }

    /**
     * A function to calculate the latitude from a given y-coordinate on the map.
     * @param y  The y-coordinate of the entity.
     * @return The latitude of the given y-coordinate.
     */
    public double toLatitude(int y) {
        double latitude = y;
        latitude = latitude / 1000 ;
        latitude = latitude / 1.609344;
        latitude = latitude / (60 * 1.1515);
        latitude = latitude + this.origin.getLatitude();
        return latitude;

    }

    public GeoPosition toGeoPosition(Pair<Integer, Integer> coords) {
        return toGeoPosition(coords.getLeft(), coords.getRight());
    }

    public GeoPosition toGeoPosition(int x, int y) {
        return new GeoPosition(toLatitude(y), toLongitude(x));
    }

    /**
     * Calculate the distance (in km) between two geo coordinates.
     * @param pos1 position 1
     * @param pos2 position 2
     * @return The distance between the two positions in km.
     */
    public static double distance(GeoPosition pos1, GeoPosition pos2) {
        double lat1 = pos1.getLatitude(), lon1 = pos1.getLongitude();
        double lat2 = pos2.getLatitude(), lon2 = pos2.getLongitude();

        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return dist;
        }
    }

    public static GeoPosition meanPosition(GeoPosition pos1, GeoPosition pos2) {
        return new GeoPosition((pos1.getLatitude() + pos2.getLatitude()) / 2,
            (pos1.getLongitude() + pos2.getLongitude()) / 2);
    }

    public static DegreesMinutesSeconds toDegreeMinuteSecond(double latOrLong) {
        int degrees = (int) Math.round(Math.floor(latOrLong));
        int minutes = (int) Math.round(Math.floor((latOrLong - degrees) * 60));
        double seconds = (double) Math.round(((latOrLong - degrees) * 60 - minutes) * 60 * 1000d) / 1000d;
        return new DegreesMinutesSeconds(degrees, minutes, seconds);
    }

    /**
     * This function return "N" and "S" for latitude input and "E" and "W" for longitude input
     * base on their value
     * @param axis could have two values "lat" or "long"
     */
    public static String getDirectionSign(double val, String axis) {
        if (axis.equals("lat")) {
            return (Math.signum(val) == 1) ? "N " : "S ";
        } else if(axis.equals("long")) {
            return Math.signum(val) == 1 ? "E " : "W ";
        } else {
            throw new IllegalArgumentException("The value of axis must be one of 'lat' or 'long'");
        }
    }


    public static class DegreesMinutesSeconds {
        int degrees;
        int minutes;
        double seconds;

        DegreesMinutesSeconds(int d, int m, double s) {
            this.degrees = d;
            this.minutes = m;
            this.seconds = s;
        }

        public String toString() {
            return String.format("%d° %d' %.2f\"", this.degrees, this.minutes, this.seconds);
        }
    }
}
