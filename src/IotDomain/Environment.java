package IotDomain;

import IotDomain.networkentity.Gateway;
import IotDomain.networkentity.Mote;
import be.kuleuven.cs.som.annotate.Basic;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;
import util.MapHelper;
import util.Pair;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;

/**
 * A class representing a map of the environment.
 */
public class Environment implements Serializable {

    //? Should this class be Singleton?

    private static final long serialVersionUID = 1L;

    private final MapHelper mapHelper;

    /**
     * The max x-coordinate allowed on the map
     */
    private static int maxXpos = 0;
    /**
     * The max y-coordinate allowed on the map
     */
    private static int maxYpos = 0;
    /**
     * A list containing all motes currently active on the map.
     */
    private LinkedList<Mote> motes = new LinkedList<>();

    /**
     * A list containing all gateways currently active on the map.
     */
    private LinkedList<Gateway> gateways = new LinkedList<>();

    /**
     * The actual map containing the characteristics of the environment.
     */
    private Characteristic[][] characteristics;

    /**
     * The number of zones in the configuration.
     */

    private int numberOfZones;
    /**
     * The graph used for routing.
     */
    private GraphStructure graph;

    /**
     * The number of runs with this configuration.
     */
    private int numberOfRuns;

    /**
     * A way to represent the flow of time in the environment.
     */
    private GlobalClock clock;

    /**
     * A constructor generating a new environment with a given map with characteristics.
     * @param characteristics   The map with the characteristics of the current environment.
     * @param mapOrigin coordinates of the point [0,0] on the map.
     * @param numberOfZones the number of zones defined in the region.
     * @param wayPoints a map of waypoints (ID -> coordinates).
     * @param connections a map of connections (ID -> connection).
     * @Post    Sets the max x-coordinate to the x size of the map if the map is valid.
     * @Post    Sets the max y-coordinate to the y size of the map if the map is valid.
     * @Post    Sets the characteristics to the given map if the map is valid.
     * @Post    Sets the max x-coordinate to 0 if the map is  not valid.
     * @Post    Sets the max y-coordinate to 0 if the map is not valid.
     * @Post    Sets the characteristics to an empty list if the map is not valid.
     */
    public Environment(Characteristic[][] characteristics, GeoPosition mapOrigin, int numberOfZones,
                       Map<Long, GeoPosition> wayPoints, Map<Long, Connection> connections) {
        if (areValidCharacteristics(characteristics)) {
            maxXpos = characteristics.length-1;
            maxYpos = characteristics[0].length-1;
            this.characteristics = characteristics;
        } else {
            maxXpos = 0;
            maxYpos = 0;
            this.characteristics = new Characteristic[0][0];
        }

        this.numberOfZones = numberOfZones;
        this.clock = new GlobalClock();
        this.mapHelper = MapHelper.getInstance();
        this.mapHelper.setMapOrigin(mapOrigin);

        if (GraphStructure.isInitialized()) {
            // Reinitialize the graph structure if a configuration has already been loaded in previously
            this.graph = GraphStructure.getInstance().reInitialize(wayPoints, connections);
        } else {
            this.graph = GraphStructure.initialize(wayPoints, connections);
        }

        numberOfRuns = 1;
    }

    public static int getMapWidth() {
        if (maxXpos == 0) {
            throw new IllegalStateException("map not already initialized");
        }
        return maxXpos;
    }

    public static int getMapHeight() {
        if (maxYpos == 0) {
            throw new IllegalStateException("map not already initialized");
        }
        return maxYpos;
    }

    /**
     * Returns the clock used by this environment.
     * @return The clock used by this environment.
     */
    public GlobalClock getClock(){
        return clock;
    }

    /**
     * Gets the number of zones.
     * @return The number of zones.
     */
    public int getNumberOfZones() {
        return numberOfZones;
    }

    /**
     * Sets the number of zones.
     * @param numberOfZones the number of zones.
     */
    public void setNumberOfZones(int numberOfZones) {
        this.numberOfZones = numberOfZones;
    }



    /**
     * Determines if a x-coordinate is valid on the map
     * @param x The x-coordinate to check
     * @return true if the coordinate is not bigger than the max coordinate
     */
    public boolean isValidXpos(int x) {
        return x >= 0 && x <= getMaxXpos();
    }

    /**
     *
     * @return the max x-coordinate
     */
    @Basic
    public int getMaxXpos() {
        return maxXpos;
    }

    /**
     * Determines if a y-coordinate is valid on the map
     * @param y The y-coordinate to check
     * @return true if the coordinate is not bigger than the max coordinate
     */
    public boolean isValidYpos(int y){
        return y >= 0 && y <= getMaxYpos();
    }

    /**
     *
     * @return the max y-coordinate
     */
    @Basic
    public int getMaxYpos() {
        return maxYpos;
    }

    /**
     * Returns all the gateways on the map.
     * @return A list with all the gateways on the map.
     */
    @Basic
    public LinkedList<Gateway> getGateways() {
        return gateways;
    }

    /**
     * Adds a gateway to the list of gateways if it is located in this environment.
     * @param gateway  the node to add
     * @Post    If the gateway is in this environment, it is added to the list.
     */
    @Basic
    public void addGateway(Gateway gateway) {
        if(gateway.getEnvironment() == this){
            gateways.add(gateway);
        }
    }

    /**
     *
     * @return A list with all the motes on the map.
     */
    @Basic
    public LinkedList<Mote> getMotes() {
        return motes;
    }

    /**
     * Adds a mote to the list of motes if it is located in this environment.
     * @param mote  the mote to add
     * @Post    If the mote is in this environment, it is added to the list.
     */
    @Basic
    public void addMote(Mote mote) {
        if(mote.getEnvironment() == this){
            motes.add(mote);
        }
    }

    /**
     * Determines if a given map of characteristics is valid.
     * @param characteristics The map to check.
     * @return  True if the Map is square.
     */
    public boolean areValidCharacteristics(Characteristic[][] characteristics){
        if (characteristics.length == 0) {
            return false;
        } else if (characteristics[0].length == 0) {
            return false;
        }

        // Make sure that each row has the same length
        int ySize = characteristics[0].length;

        for (Characteristic[] row : characteristics) {
            if (row.length != ySize) {
                return false;
            }
        }

        return true;
    }

    /**
     * returns the characteristics of a given position
     * @param xPos  The x-coordinate of the position.
     * @param yPos  The y-coordinate of the position.
     * @return  the characteristic of the position if the position is valid.
     */
    public Characteristic getCharacteristic(int xPos, int yPos) {
        if (isValidXpos(xPos) && isValidYpos(yPos)) {
            return characteristics[xPos][yPos];
        }
        else
            return null;
    }

    /**
     * Sets the characteristic to the given characteristic on the given location.
     * @param characteristic the given characteristic.
     */
    public void setCharacteristics(Characteristic characteristic, int xPos, int yPos) {
        this.characteristics[xPos][yPos] = characteristic;
    }


    /**
     * Returns the coordinates of the point [0,0] on the map.
     * @return The coordinates of the point [0,0] on the map.
     */
    public GeoPosition getMapOrigin() {
        return mapHelper.getMapOrigin();
    }

    /**
     * Returns the geoPosition of the center of the map.
     * @return The geoPosition of the center of the map.
     */
    public GeoPosition getMapCenter() {
        return new GeoPosition(toLatitude(getMaxYpos()/2),toLongitude(getMaxXpos()/2));
    }

    /**
     * A function to calculate the longitude from a given x-coordinate on the map.
     * @param xPos  The x-coordinate of the entity.
     * @return The longitude of the given x-coordinate
     */
    public double toLongitude(int xPos){
        return mapHelper.toLongitude(xPos);
    }

    /**
     * A function to calculate the latitude from a given y-coordinate on the map.
     * @param yPos  The y-coordinate of the entity.
     * @return The latitude of the given y-coordinate.
     */
    public double toLatitude(int yPos){
        return mapHelper.toLatitude(yPos);
    }

    /**
     * A function for calculating distances from geographical positions.
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        return MapHelper.distance(new GeoPosition(lat1, lon1), new GeoPosition(lat2, lon2));
    }

    /**
     * A function that moves a mote to a geoposition 1 step and returns if the note has moved.
     * @param position The position to move towards.
     * @param mote The mote to move.
     * @return If the node has moved.
     */
    public boolean moveMote(Mote mote, GeoPosition position){
        int xPos = toMapXCoordinate(position);
        int yPos = toMapYCoordinate(position);
        if(Integer.signum(xPos - mote.getXPos()) != 0 || Integer.signum(yPos - mote.getYPos()) != 0){
            if(Math.abs(mote.getXPos() - xPos) >= Math.abs(mote.getYPos() - yPos)){
                mote.setXPos(mote.getXPos()+ Integer.signum(xPos - mote.getXPos()));

            }
            else{
                mote.setYPos(mote.getYPos()+ Integer.signum(yPos - mote.getYPos()));
            }
            return true;
        }
        return false;
    }

    /**
     * Converts a GeoPostion to an x-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The x-coordinate on the map of the GeoPosition.
     */
    public int toMapXCoordinate(GeoPosition geoPosition){
        //? in computing distance just using the longitude of the geoposition. Why?
        return mapHelper.toMapXCoordinate(geoPosition);
    }
    /**
     * Converts a GeoPostion to an y-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The y-coordinate on the map of the GeoPosition.
     */
    public int toMapYCoordinate(GeoPosition geoPosition){
        //? in computing distance just using the longitude of the geoposition. Why?
        return mapHelper.toMapYCoordinate(geoPosition);
    }

    public Pair<Integer,Integer> toMapCoordinate(GeoPosition geoPosition){
        return mapHelper.toMapCoordinate(geoPosition);
    }
    /**
     * reset all entities in the configuration.
     */
    public void reset(){
        getClock().reset();
        for(Mote mote: getMotes()){
            mote.reset();
        }
        for(Gateway gateway: getGateways()){
            gateway.reset();
        }
        numberOfRuns = 1;
    }

    /**
     * Adds a run to all entities in the configuration.
     */
    public void addRun(){
        getClock().reset();
        for (Mote mote : getMotes()) {
            mote.addRun();
        }
        for (Gateway gateway : getGateways()) {
            gateway.addRun();
        }
        numberOfRuns++;
    }

    /**
     * Returns the number of runs of this configuration.
     * @return The number of runs of this configuration.
     */
    @Basic
    public int getNumberOfRuns(){
        return numberOfRuns;
    }


    /**
     * Shortens the routes of motes which contain the given waypoint.
     * @param wayPointId The ID of the waypoint.
     */
    public void removeWayPointFromMotes(long wayPointId) {
        motes.forEach(o -> o.shortenPathFromWayPoint(wayPointId));
    }

    public void removeConnectionFromMotes(long connectionId) {
        motes.forEach(o -> o.shortenPathFromConnection(connectionId));
    }
}

