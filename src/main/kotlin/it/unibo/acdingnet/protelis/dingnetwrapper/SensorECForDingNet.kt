package it.unibo.acdingnet.protelis.dingnetwrapper

import iot.GlobalClock
import it.unibo.acdingnet.protelis.executioncontext.SensorExecutionContext
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import org.protelis.vm.NetworkManager
import kotlin.math.abs

class SensorECForDingNet(
    private val sensorNode: SensorNodeWrapper,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager
) : SensorExecutionContext(sensorNode, applicationUID, mqttClient, netmgr) {
    private val timer: GlobalClock = sensorNode.timer

    init {
        execEnvironment.put(Const.ProtelisEnv.IAQLEVEL, 20.0 - abs(sensorNode.deviceUID.uid.toLong()))
    }

    override fun instance(): SensorECForDingNet = this


    override fun getCurrentTime(): Number {
        return timer.time.asSecond()
    }
}
