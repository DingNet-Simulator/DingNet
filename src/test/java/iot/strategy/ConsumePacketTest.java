package iot.strategy;

import iot.Characteristic;
import iot.Environment;
import iot.lora.LoraWanPacket;
import iot.networkentity.Mote;
import iot.strategy.consume.AddPositionToPath;
import iot.strategy.consume.ReplacePath;
import org.junit.jupiter.api.Test;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.ListHelper;
import util.MapHelper;
import util.Path;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class ConsumePacketTest {

    private class DummyMote extends Mote {

        public DummyMote(long devEUI, Environment env) {
            this(devEUI, new Path(null), env);
        }

        public DummyMote(long devEUI, Path path, Environment env) {
            super(devEUI, 1, 1, 0, 7, List.of(), 0,
                path, 0, env);
        }
    }

    // region addPositionStrategy
    @Test
    public void wrongPacket() {
        var env = new Environment(new Characteristic[1][1], new GeoPosition(5, 5), 1, Map.of(), Map.of());

        var mote = new DummyMote(1, env);
        var strategy = new AddPositionToPath();
        assertThrows(IllegalStateException.class,
            () -> strategy.consume(mote, new LoraWanPacket(2,1, new byte[2], List.of())));
    }

    @Test
    public void increasePath() {
        var env = new Environment(new Characteristic[1][1], new GeoPosition(5, 5), 1, Map.of(), Map.of());

        var mote = new DummyMote(1, env);
        assertTrue(mote.getPath().isEmpty());

        var strategy = new AddPositionToPath();
        var pos = new GeoPosition(2,2);
        strategy.consume(mote, new LoraWanPacket(2,1, Converter.toByteArray(pos), List.of()));
        assertEquals(1, mote.getPath().getWayPoints().size());
        assertTrue(MapHelper.equalsGeoPosition(pos, ListHelper.getLast(mote.getPath().getWayPoints())));
    }
    // endregion

    // region replacePathStrategy

    @Test
    public void emptyPath() {
        var env = new Environment(new Characteristic[1][1], new GeoPosition(5, 5), 1, Map.of(), Map.of());

        var mote = new DummyMote(1, env);
        assertTrue(mote.getPath().isEmpty());

        var strategy = new ReplacePath();
        var pos = List.of(new GeoPosition(2,2), new GeoPosition(1,1));
        var posArray = pos.stream()
            .flatMap(p -> Arrays.stream(Converter.toObjectType(Converter.toByteArray(p))))
            .toArray(Byte[]::new);
        strategy.consume(mote, new LoraWanPacket(2,1, Converter.toRowType(posArray), List.of()));
        assertEquals(2, mote.getPath().getWayPoints().size());
        assertTrue(
            IntStream
                .range(0, pos.size())
                .allMatch(i -> MapHelper.equalsGeoPosition(
                    pos.get(i),
                    mote.getPath().getWayPoints().get(i)))
        );

        //with a empty packet the path don't change
        strategy.consume(mote, LoraWanPacket.createEmptyPacket(2,1));

        assertEquals(2, mote.getPath().getWayPoints().size());
        assertTrue(
            IntStream
                .range(0, pos.size())
                .allMatch(i -> MapHelper.equalsGeoPosition(
                    pos.get(i),
                    mote.getPath().getWayPoints().get(i)))
        );
    }

    // endregion
}
