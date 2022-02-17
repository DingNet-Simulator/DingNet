package iot.strategy.consume;

import iot.lora.LoraWanPacket;
import iot.networkentity.LifeLongMote;
import iot.networkentity.Mote;
import iot.networkentity.UserMote;
import org.jxmapviewer.viewer.GeoPosition;
import util.Converter;
import util.MoteSettings;

import java.util.LinkedList;
import java.util.List;

public class ChangeSettings implements ConsumePacketStrategy{

    @Override
    public void consume(Mote mote, LoraWanPacket packet) {
        if(mote instanceof LifeLongMote) {
            LifeLongMote lifeLongMote = (LifeLongMote) mote;
            var settings = extractSetings(packet);

            lifeLongMote.adjustSettings(settings);
        }
    }

    protected MoteSettings extractSetings(LoraWanPacket packet) {
        if (packet.getPayload().length != MoteSettings.amountOfSettings()) {
            throw new IllegalStateException("the packet doesn't contain the correct amount of bytes");
        }
        var payload = packet.getPayload();
        final MoteSettings settings = Converter.toMoteSettings(payload);
        return settings;
    }
}
