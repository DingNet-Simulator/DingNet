package it.unibo.acdingnet.protelis

import application.Application
import application.pollution.PollutionGrid
import iot.GlobalClock
import iot.mqtt.MQTTClientFactory
import iot.mqtt.TransmissionWrapper
import iot.networkentity.Mote
import iot.networkentity.UserMote
import it.unibo.acdingnet.protelis.dingnetwrapper.SensorNodeWrapper
import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.neighborhood.NeighborhoodManager
import it.unibo.acdingnet.protelis.neighborhood.Node
import it.unibo.acdingnet.protelis.node.BuildingNode
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.acdingnet.protelis.util.gui.ProtelisPollutionGrid
import it.unibo.acdingnet.protelis.util.toGeoPosition
import it.unibo.mqttclientwrapper.mock.MqttMockCast
import org.jxmapviewer.viewer.GeoPosition
import org.protelis.lang.ProtelisLoader
import org.protelis.lang.datatype.impl.StringUID
import util.time.DoubleTime
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
    private val building: List<BuildingNode>

    init {
        val nodes: MutableSet<Node> = motes.map {
            Node(
                StringUID("" + it.eui),
                LatLongPosition(
                    it.pathPosition.latitude,
                    it.pathPosition.longitude
                )
            )
        }.toMutableSet()

        val buildingNode = listOf(
            Pair(
                Node(
                    StringUID("0"),
                    LatLongPosition(
                        50.877910751397,
                        4.69141960144043
                    )
                ), //25
                23.2
            ),
            Pair(
                Node(
                    StringUID("1"),
                    LatLongPosition(
                        50.884419292982145,
                        4.711053371429443
                    )
                ), //23
                24.0
            ),
            Pair(
                Node(
                    StringUID("2"),
                    LatLongPosition(
                        50.86946149128906,
                        4.702663421630859
                    )
                ), //24
                23.5
            )
        )

        nodes.addAll(buildingNode.map { it.first })

        neigh = NeighborhoodManager(
            Const.APPLICATION_ID,
            MQTTClientFactory.getSingletonInstance(), Const.NEIGHBORHOOD_RANGE, nodes
        )

        node = motes
            .filter { it !is UserMote }
            .map {
                val id = StringUID("" + it.eui)
                SensorNodeWrapper(
                    ProtelisLoader.parse(infoProtelisApp.protelisProgram),
                    DoubleTime(random.nextInt(100).toDouble()),
                    900,
                    id,
                    Const.APPLICATION_ID,
                    MqttMockCast(),
                    MQTTClientFactory.getSingletonInstance(),
                    LatLongPosition(
                        it.pathPosition.latitude,
                        it.pathPosition.longitude
                    ),
                    it.sensors.map { s -> SensorType.valueOf("$s") },
                    timer,
                    neigh.getNeighborhoodByNodeId(id).map { n -> n.uid }.toSet()
                )
            }

        building = buildingNode.map {
            BuildingNode(
                ProtelisLoader.parse(infoProtelisApp.protelisProgram),
                DoubleTime(random.nextInt(100).toDouble()).plusMinutes(1.0),
                900,
                it.first.uid,
                Const.APPLICATION_ID,
                MqttMockCast(),
                it.first.position,
                it.second,
                0.1,
                timer,
                neigh.getNeighborhoodByNodeId(it.first.uid).map { n -> n.uid }.toSet()
            )
        }
    }

    override fun consumePackets(topicFilter: String, message: TransmissionWrapper) {}

    fun getDrawableNode(): List<DrawableNodeInfo> = building.map {
        DrawableNodeInfo(
            it.position.toGeoPosition(),
            it.getTemp(Const.ProtelisEnv.DESIRED_TEMP),
            it.getTemp(Const.ProtelisEnv.MAX_TEMP),
            it.getTemp(Const.ProtelisEnv.CURRENT_TEMP)
        )
    }

    fun getPollutionGrid(): PollutionGrid = ProtelisPollutionGrid(
        node.map { Pair(it.position.toGeoPosition(), it.getPollutionValue()) },
        Const.NEIGHBORHOOD_RANGE,
        20.0 //value in good level
    )
}

data class DrawableNodeInfo(
    val position: GeoPosition,
    val desiredTemp: Double,
    val maxTemp: Double,
    val currentTemp: Double
)
