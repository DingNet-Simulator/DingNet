package it.unibo.acdingnet.protelis

import application.Application
import iot.GlobalClock
import iot.mqtt.MQTTClientFactory
import iot.mqtt.TransmissionWrapper
import iot.networkentity.Mote
import iot.networkentity.UserMote
import it.unibo.acdingnet.protelis.dingnetwrapper.SensorNodeWrapper
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.protelisovermqtt.model.LatLongPosition
import it.unibo.protelisovermqtt.neighborhood.NeighborhoodManager
import it.unibo.protelisovermqtt.neighborhood.Node
import org.protelis.lang.ProtelisLoader
import org.protelis.lang.datatype.impl.StringUID
import java.time.LocalTime
import java.util.*
import java.util.stream.Collectors

class ProtelisApp(motes: List<Mote>, private val timer: GlobalClock) : Application(emptyList()) {

    private val neigh = NeighborhoodManager(Const.APPLICATION_ID,
        MQTTClientFactory.getSingletonInstance(), Const.NEIGHBORHOOD_RANGE)
    private val random = Random(2)
    private val node: List<SensorNodeWrapper>

    init {
        val nodes = motes.map { Node(StringUID("" + it.eui),
            LatLongPosition(it.pathPosition.latitude, it.pathPosition.longitude)) }.toSet()
        node = motes.stream()
            .filter { m: Mote? -> m !is UserMote }
            .map { m: Mote ->
                SensorNodeWrapper(
                    ProtelisLoader.parse("gradient"),
                    LocalTime.of(0, 0, 0, random.nextInt(100) * 1000000),
                    30,
                    StringUID("" + m.eui),
                    "" + m.applicationEUI,
                    MQTTClientFactory.getSingletonInstance(),
                    LatLongPosition(m.pathPosition.latitude, m.pathPosition.longitude),
                    listOf(SensorType.IAQ),
                    timer,
                    NeighborhoodManager.computeNeighborhood(
                        Node(StringUID("" + m.eui), LatLongPosition(m.pathPosition.latitude,
                            m.pathPosition.longitude)),
                        nodes, Const.NEIGHBORHOOD_RANGE
                    ).map { it.uid }.toSet()
                )
            }
            .collect(Collectors.toList())
    }

    override fun consumePackets(topicFilter: String, message: TransmissionWrapper) {}
}
