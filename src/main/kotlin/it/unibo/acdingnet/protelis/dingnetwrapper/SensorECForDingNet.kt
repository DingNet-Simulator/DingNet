package it.unibo.acdingnet.protelis.dingnetwrapper

import iot.GlobalClock
import it.unibo.acdingnet.protelis.executioncontext.SensorExecutionContext
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.NODE_TYPE
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.SENSOR_TYPE
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
        execEnvironment.put(NODE_TYPE, SENSOR_TYPE)
        execEnvironment.put("pm10", 55.1)
    }

    override fun instance(): SensorECForDingNet {
        return SensorECForDingNet(sensorNode, applicationUID, mqttClient, netmgr)
    }

    override fun getCurrentTime(): Number {
        return timer.time.second
    }
}
