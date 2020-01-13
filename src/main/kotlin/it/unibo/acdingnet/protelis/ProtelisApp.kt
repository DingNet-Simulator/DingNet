package it.unibo.acdingnet.protelis

import application.Application
import iot.GlobalClock
import iot.mqtt.MQTTClientFactory
import iot.mqtt.TransmissionWrapper
import iot.networkentity.Mote
import iot.networkentity.UserMote
import it.unibo.acdingnet.protelis.dingnetwrapper.SensorNodeWrapper
import it.unibo.acdingnet.protelis.model.GPSTrace
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

data class InfoProtelisApp @JvmOverloads constructor(
    val protelisProgram: String,
    val gpxFileTrace: String? = null,
    val startingTimeTrace: Double = 0.0 // in ms
)

class ProtelisApp(
    infoProtelisApp: InfoProtelisApp,
    motes: List<Mote>,
    private val timer: GlobalClock
) : Application(emptyList()) {

    private val neigh: NeighborhoodManager
    private val random = Random(2)
    private val node: List<SensorNodeWrapper>
    private val smartphone: List<SmartphoneNode>

    init {
        val trace: List<Pair<StringUID, GPSTrace>> = infoProtelisApp.gpxFileTrace?.let {
            LoadGPXFile.loadFile(
                this.javaClass.getResourceAsStream(it),
                infoProtelisApp.startingTimeTrace)
                .filter { t -> t.positions.isNotEmpty() }
                .map { t -> Pair(StringUID(UUID.randomUUID().toString()), t) }
        }.orEmpty()

        val nodes: MutableSet<Node> = motes.map { Node(StringUID("" + it.eui),
            LatLongPosition(it.pathPosition.latitude, it.pathPosition.longitude)) }.toMutableSet()

        trace.map { Node(it.first, it.second.positions.first().position) }.forEach { nodes.add(it) }

        neigh = NeighborhoodManager(Const.APPLICATION_ID,
            MQTTClientFactory.getSingletonInstance(), Const.NEIGHBORHOOD_RANGE, nodes)

        node = motes
            .filter { it !is UserMote }
            .map {
                val id = StringUID("" + it.eui)
                SensorNodeWrapper(
                    ProtelisLoader.parse(infoProtelisApp.protelisProgram),
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
                    ProtelisLoader.parse(infoProtelisApp.protelisProgram),
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
