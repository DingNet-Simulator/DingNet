package it.unibo.acdingnet.protelis

import application.Application
import iot.GlobalClock
import iot.SimulationRunner
import iot.mqtt.MQTTClientFactory
import iot.mqtt.TransmissionWrapper
import iot.networkentity.Mote
import iot.networkentity.UserMote
import it.unibo.acdingnet.protelis.dingnetwrapper.SensorNodeWrapper
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.node.SmartphoneNode
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.acdingnet.protelis.util.LoadGPXFile
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
        val protelisProgram = SimulationRunner.getInstance()
            .simulation.inputProfile.orElseThrow()
            .protelisProgram.orElseThrow { IllegalStateException("protelis program not found") }
        node = motes.stream()
            .filter { it !is UserMote }
            .map {
                SensorNodeWrapper(
                    ProtelisLoader.parse(protelisProgram),
                    LocalTime.of(0, 0, 0, random.nextInt(100) * 1000000),
                    30,
                    StringUID("" + it.eui),
                    Const.APPLICATION_ID,
                    MQTTClientFactory.getSingletonInstance(),
                    LatLongPosition(it.pathPosition.latitude, it.pathPosition.longitude),
                    listOf(SensorType.IAQ),
                    timer,
                    NeighborhoodManager.computeNeighborhood(
                        Node(StringUID("" + it.eui), LatLongPosition(it.pathPosition.latitude,
                            it.pathPosition.longitude)),
                        nodes, Const.NEIGHBORHOOD_RANGE
                    ).map { n -> n.uid }.toSet()
                )
            }
            .collect(Collectors.toList())

        LoadGPXFile.loadFile(this.javaClass.getResourceAsStream(""), 1365922800.0)//time get from Vienna demo of Alchemist
            .map {
                val uid = StringUID(UUID.randomUUID().toString())
                SmartphoneNode(
                    ProtelisLoader.parse(protelisProgram),
                    LocalTime.of(0, 0, 0, random.nextInt(100) * 1000000),
                    10,
                    uid,
                    Const.APPLICATION_ID,
                    MQTTClientFactory.getSingletonInstance(),
                    it.positions[0].position,
                    timer,
                    it,
                    NeighborhoodManager.computeNeighborhood(
                        Node(uid, it.positions[0].position),
                        nodes, Const.NEIGHBORHOOD_RANGE
                    ).map { n -> n.uid }.toSet())
            }
    }

    override fun consumePackets(topicFilter: String, message: TransmissionWrapper) {}
}
