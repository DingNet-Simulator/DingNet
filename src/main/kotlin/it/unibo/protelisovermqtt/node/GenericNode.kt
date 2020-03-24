package it.unibo.protelisovermqtt.node

import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.model.LatLongPosition
import it.unibo.protelisovermqtt.networkmanager.MQTTNetMgrWithMQTTNeighborhoodMgr
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
    netManagerMqttClient: MqttClientBasicApi,
    val execContextMqttClient: MqttClientBasicApi,
    initialPosition: LatLongPosition,
    neighbors: Set<StringUID> = emptySet()
) {
    constructor(
        protelisProgram: ProtelisProgram,
        sleepTime: Long,
        deviceUID: StringUID,
        applicationUID: String,
        mqttClient: MqttClientBasicApi,
        initialPosition: LatLongPosition,
        neighbors: Set<StringUID> = emptySet()
    ) : this(protelisProgram, sleepTime, deviceUID, applicationUID,
        mqttClient, mqttClient, initialPosition, neighbors)

    var position: LatLongPosition by observable(initialPosition) {
        _, old, new -> if (old != new) networkManager.changePosition(new)
    }

    protected val networkManager = MQTTNetMgrWithMQTTNeighborhoodMgr(deviceUID,
        netManagerMqttClient, applicationUID, position, neighbors)
    protected val executionContext by lazy { createContext() }
    private val protelisVM by lazy { ProtelisVM(protelisProgram, executionContext) }

    protected abstract fun createContext(): ExecutionContext

    fun runVM() = protelisVM.runCycle()
}
