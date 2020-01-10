package it.unibo.acdingnet.protelis.node

import iot.GlobalClock
import it.unibo.acdingnet.protelis.model.GPSTrace
import it.unibo.acdingnet.protelis.util.millis
import it.unibo.acdingnet.protelis.util.toGeoPosition
import it.unibo.acdingnet.protelis.util.travel
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.model.LatLongPosition
import it.unibo.protelisovermqtt.node.GenericNode
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionContext
import org.protelis.vm.ProtelisProgram
import util.MapHelper
import java.time.LocalTime

class SmartphoneNode(
    protelisProgram: ProtelisProgram,
    startingTime: LocalTime,
    sleepTime: Long,
    deviceUID: StringUID,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    initialPosition: LatLongPosition,
    private val timer: GlobalClock,
    private val trace: GPSTrace,
    neighbors: Set<StringUID>
) : GenericNode(protelisProgram, sleepTime, deviceUID, applicationUID, mqttClient, initialPosition, neighbors) {


    private val updatePositionTriggerId: Long

    override fun createContext(): ExecutionContext {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init {
        timer.addPeriodicTrigger(startingTime, sleepTime) { runVM() }
        updatePositionTriggerId = timer.addPeriodicTrigger(startingTime.minusSeconds(1),
            sleepTime) { position = move() } // generate MQTT message
    }

    // TODO improve error check
    private fun move(): LatLongPosition {
        val currentTime = timer.time
        val next = trace.positions
            .mapIndexed { index, pos -> Pair(index, pos) }
            .firstOrNull { it.second.time > currentTime.millis() }
            ?: return position
        // if the next position to reach is the first of the trace -> DON'T move
        if (next.first == 0) {
            return position
        }
        val (index, nextPos) = next
        val prePos = trace.positions[index-1]
        if (nextPos.time == prePos.time) {
            return nextPos.position
        }
        val traveledTime = (currentTime.millis() - prePos.time) / (nextPos.time - prePos.time)
        val distanceToTravel = MapHelper.distanceMeter(prePos.position.toGeoPosition(),
            nextPos.position.toGeoPosition()) * traveledTime
        return prePos.position.travel(nextPos.position, distanceToTravel)
    }
}
