package it.unibo.acdingnet.protelis.dingnetwrapper

import iot.GlobalClock
import it.unibo.acdingnet.protelis.executioncontext.BuildingExecutionContext
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import org.protelis.vm.NetworkManager

class BuildingECForDingNet(
    buildingNode: BuildingNodeWrapper,
    desiredTemp: Double,
    deltaTemp: Double,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager
) : BuildingExecutionContext(
    buildingNode,
    desiredTemp,
    deltaTemp,
    applicationUID,
    mqttClient,
    netmgr
) {

    private val timer: GlobalClock = buildingNode.timer

    override fun instance(): BuildingExecutionContext = this

    override fun getCurrentTime(): Number {
        return timer.time.asSecond()
    }
}
