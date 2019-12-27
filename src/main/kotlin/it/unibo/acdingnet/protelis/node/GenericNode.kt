package it.unibo.acdingnet.protelis.node

import iot.mqtt.MqttClientBasicApi
import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.networkmanager.MQTTNetMgrWithMQTTNeighborhoodMgr
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionContext
import org.protelis.vm.ProtelisProgram
import org.protelis.vm.ProtelisVM
import kotlin.properties.Delegates.observable

abstract class GenericNode(
    val protelisProgram: ProtelisProgram,
    val sleepTime: Long,
    val deviceUID: StringUID,
    val applicationUID: String,
    val mqttClient: MqttClientBasicApi,
    initialPosition: LatLongPosition,
    neighbors: Set<StringUID> = emptySet()
) {

    var position: LatLongPosition by observable(initialPosition) {
        _, old, new -> if (old != new) networkManager.changePosition(new)
    }

    protected val networkManager =
        MQTTNetMgrWithMQTTNeighborhoodMgr(deviceUID, mqttClient, applicationUID, position, neighbors)
    protected val executionContext by lazy { createContext() }
    private val protelisVM by lazy { ProtelisVM(protelisProgram, executionContext) }

    protected abstract fun createContext(): ExecutionContext

    fun runVM() = protelisVM.runCycle()
}
