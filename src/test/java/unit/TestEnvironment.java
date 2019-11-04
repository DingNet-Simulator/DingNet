
package unit;

import IotDomain.Characteristic;
import IotDomain.Environment;
import IotDomain.networkentity.Gateway;
import IotDomain.networkentity.Mote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.Connection;
import util.GraphStructure;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TestEnvironment {

    @BeforeEach
    void init() throws IllegalAccessException, NoSuchFieldException {
        // The environment constructor initializes the GraphStructure instance -> destruct each test
        Field instance = GraphStructure.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void happyDay() {
        Environment environment = new Environment(new Characteristic[1][1], new GeoPosition(5, 5), 1, new HashMap<>(), new HashMap<>());

        assertTrue(environment.getMotes().isEmpty());
        assertTrue(environment.getGateways().isEmpty());
        assertEquals(environment.getClock().getTime(), LocalTime.of(0,0,0));
        assertNull(environment.getCharacteristic(0,0));
        assertEquals(environment.getMapCenter(), new GeoPosition(5,5));
        assertEquals(environment.getMaxXpos(), 0);
        assertEquals(environment.getMaxYpos(), 0);
        assertEquals(environment.getNumberOfRuns(), 1);
        assertEquals(environment.getNumberOfZones(), 1);
    }

    @Test
    void waypointsConnections() {
        Environment environment = new Environment(new Characteristic[1][1], new GeoPosition(5, 5), 1,
            new HashMap<>(Map.of(1L, new GeoPosition(6,6), 3L, new GeoPosition(10,5))), new HashMap<>(Map.of(4L, new Connection(1L, 3L))));

        assertTrue(GraphStructure.getInstance().connectionExists(1L, 3L));
        assertEquals(GraphStructure.getInstance().getWayPoint(1L), new GeoPosition(6,6));
        assertNull(GraphStructure.getInstance().getWayPoint(2L));
        assertEquals(GraphStructure.getInstance().getWayPoint(3L), new GeoPosition(10, 5));
    }

    @Test
    void characteristics() {
        Characteristic[][] characteristics = new Characteristic[500][500];
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 100; j++) {
                characteristics[i][j] = Characteristic.Plain;
            }
            for (int j = 100; j < 300; j++) {
                characteristics[i][j] = Characteristic.Forest;
            }
            for (int j = 300; j < 500; j++) {
                characteristics[i][j] = Characteristic.City;
            }
        }

        Environment environment = new Environment(characteristics, new GeoPosition(10, 10), 25, new HashMap<>(), new HashMap<>());

        assertEquals(environment.getNumberOfZones(), 25);
        assertEquals(environment.getMaxXpos(), 499);
        assertEquals(environment.getMaxYpos(), 499);

        assertEquals(environment.getCharacteristic(50, 50), Characteristic.Plain);
        assertEquals(environment.getCharacteristic(499, 299), Characteristic.Forest);
        assertEquals(environment.getCharacteristic(250, 300), Characteristic.City);

        environment.setCharacteristics(Characteristic.Forest, 20, 20);
        assertEquals(environment.getCharacteristic(20, 20), Characteristic.Forest);
    }

    @Test
    void rainyDay() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Environment(new Characteristic[0][0], new GeoPosition(0,0), 1, new HashMap<>(), new HashMap<>());
        });
    }

//    @Test
//    void addMotes() {
//        Environment environment = new Environment(new Characteristic[1][1], new GeoPosition(0,0), 1, new HashMap<>(), new HashMap<>());
//
//        environment.addMote(new Mote());
//    }
//
//    @Test
//    void addGateWays() {
//        Environment environment = new Environment(new Characteristic[1][1], new GeoPosition(0,0), 1, new HashMap<>(), new HashMap<>());
//
//        environment.addGateway(new Gateway());
//    }
}
