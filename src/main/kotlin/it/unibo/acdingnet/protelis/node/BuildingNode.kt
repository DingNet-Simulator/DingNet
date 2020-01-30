package it.unibo.acdingnet.protelis.node

import iot.GlobalClock
import it.unibo.acdingnet.protelis.executioncontext.BuildingEC
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.model.LatLongPosition
import it.unibo.protelisovermqtt.node.GenericNode
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionContext
import org.protelis.vm.ProtelisProgram
import java.time.LocalTime

open class BuildingNode(
    protelisProgram: ProtelisProgram,
    startingTime: LocalTime,
    sleepTime: Long,
    deviceUID: StringUID,
    applicationUID: String,
    netManagerMqttClient: MqttClientBasicApi,
    position: LatLongPosition,
    private val desiredTemp: Double,
    private val deltaTemp: Double,
    val timer: GlobalClock,
    neighbors: Set<StringUID> = emptySet()
) : GenericNode(protelisProgram, sleepTime, deviceUID, applicationUID,
    netManagerMqttClient, position, neighbors) {


    init {
        timer.addPeriodicTrigger(startingTime, sleepTime) { runVM() }
    }

    override fun createContext(): ExecutionContext =
        BuildingEC(
            this,
            desiredTemp,
            deltaTemp,
            networkManager
        )
}
