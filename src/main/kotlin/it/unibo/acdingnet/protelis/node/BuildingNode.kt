package it.unibo.acdingnet.protelis.node

import iot.GlobalClock
import it.unibo.acdingnet.protelis.executioncontext.BuildingEC
import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionContext
import org.protelis.vm.ProtelisProgram
import util.time.Time

open class BuildingNode(
    protelisProgram: ProtelisProgram,
    startingTime: Time,
    sleepTime: Long,
    deviceUID: StringUID,
    applicationUID: String,
    netManagerMqttClient: MqttClientBasicApi,
    position: LatLongPosition,
    private val desiredTemp: Double,
    private val deltaTemp: Double,
    val timer: GlobalClock,
    neighbors: Set<StringUID> = emptySet()
) : SensorNode(protelisProgram, sleepTime, deviceUID, applicationUID,
    netManagerMqttClient, netManagerMqttClient, position, emptyList(), neighbors) {

    init {
        timer.addPeriodicTrigger(startingTime, sleepTime) { runVM() }
    }

    override fun createContext(): ExecutionContext =
        BuildingEC(
            this,
            desiredTemp,
            deltaTemp,
            applicationUID,
            execContextMqttClient,
            networkManager
        )

    fun getTemp(temp: String): Double = executionContext.executionEnvironment.get(temp) as Double
}
