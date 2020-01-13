package it.unibo.acdingnet.protelis

import application.Application
import iot.GlobalClock
import iot.mqtt.MQTTClientFactory
import iot.mqtt.TransmissionWrapper
import iot.networkentity.Mote
import iot.networkentity.UserMote
import it.unibo.acdingnet.protelis.dingnetwrapper.SensorNodeWrapper
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.node.SmartphoneNode
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.acdingnet.protelis.util.LoadGPXFile
import it.unibo.acdingnet.protelis.util.toGeoPosition
import it.unibo.mqttclientwrapper.mock.MqttMockCast
import it.unibo.protelisovermqtt.model.LatLongPosition
import it.unibo.protelisovermqtt.neighborhood.NeighborhoodManager
import it.unibo.protelisovermqtt.neighborhood.Node
import org.jxmapviewer.viewer.GeoPosition
import org.protelis.lang.ProtelisLoader
import org.protelis.lang.datatype.impl.StringUID
import java.time.LocalTime
import java.util.*

class ProtelisApp(motes: List<Mote>, private val timer: GlobalClock) : Application(emptyList()) {

    private val neigh: NeighborhoodManager
    private val random = Random(2)
    private val node: List<SensorNodeWrapper>
    private val smartphone: List<SmartphoneNode>

    init {
        // time get from Vienna demo of Alchemist
        val trace = LoadGPXFile.loadFile(
            this.javaClass.getResourceAsStream("/vcmuser.gpx"),
            1365922800.0*1e3
        )
            .filter { it.positions.isNotEmpty() }
            .map { Pair(StringUID(UUID.randomUUID().toString()), it) }

        val nodes: MutableSet<Node> = motes.map { Node(StringUID("" + it.eui),
            LatLongPosition(it.pathPosition.latitude, it.pathPosition.longitude)) }.toMutableSet()

        trace.map { Node(it.first, it.second.positions.first().position) }.forEach { nodes.add(it) }

        neigh = NeighborhoodManager(Const.APPLICATION_ID,
            MQTTClientFactory.getSingletonInstance(), Const.NEIGHBORHOOD_RANGE, nodes)

        val protelisProgram = "gradient"/*SimulationRunner.getInstance()
            .simulation.inputProfile.orElseThrow()
            .protelisProgram.orElseThrow { IllegalStateException("protelis program not found") }
*/
        node = motes
            .filter { it !is UserMote }
            .map {
                val id = StringUID("" + it.eui)
                SensorNodeWrapper(
                    ProtelisLoader.parse(protelisProgram),
                    LocalTime.of(0, 0, 0, random.nextInt(100) * 1000000),
                    30,
                    id,
                    Const.APPLICATION_ID,
                    MqttMockCast(),
                    MQTTClientFactory.getSingletonInstance(),
                    LatLongPosition(it.pathPosition.latitude, it.pathPosition.longitude),
                    listOf(SensorType.IAQ),
                    timer,
                    neigh.getNeighborhoodByNodeId(id).map { n -> n.uid }.toSet()
                )
            }

        smartphone = trace
            .map {
                SmartphoneNode(
                    ProtelisLoader.parse(protelisProgram),
                    LocalTime.of(0, 0, 0, random.nextInt(100) * 1e6.toInt()),
                    10,
                    it.first,
                    Const.APPLICATION_ID,
                    MqttMockCast(),
                    it.second.positions[0].position,
                    timer,
                    it.second,
                    neigh.getNeighborhoodByNodeId(it.first).map { n -> n.uid }.toSet()
                )
            }
    }

    override fun consumePackets(topicFilter: String, message: TransmissionWrapper) {}

    fun getDrawableNode(): List<GeoPosition> = smartphone.map { it.position.toGeoPosition() }
}
