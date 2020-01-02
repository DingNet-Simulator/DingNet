package it.unibo.acdingnet.protelis.dingnetwrapper

import iot.GlobalClock
import it.unibo.acdingnet.protelis.executioncontext.SensorExecutionContext
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import org.protelis.vm.NetworkManager

class SensorECForDingNet(
    private val sensorNode: SensorNodeWrapper,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager
) : SensorExecutionContext(sensorNode, applicationUID, mqttClient, netmgr) {
    private val timer: GlobalClock = sensorNode.timer

    init {
        execEnvironment.put("leader", (sensorNode.deviceUID.uid as String) == "6007316430752245294")
    }

    override fun instance(): SensorECForDingNet {
        return SensorECForDingNet(sensorNode, applicationUID, mqttClient, netmgr)
    }

    override fun getCurrentTime(): Number {
        return timer.time.nano / 1E6
    }
}
