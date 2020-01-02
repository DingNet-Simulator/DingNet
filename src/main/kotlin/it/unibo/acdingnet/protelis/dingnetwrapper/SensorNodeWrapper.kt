package it.unibo.acdingnet.protelis.dingnetwrapper

import iot.GlobalClock
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.node.SensorNode
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.model.LatLongPosition
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ProtelisProgram
import java.time.LocalTime

class SensorNodeWrapper(
    protelisProgram: ProtelisProgram,
    startingTime: LocalTime?,
    sleepTime: Long,
    sensorDeviceUID: StringUID,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    position: LatLongPosition,
    sensorTypes: List<SensorType>,
    val timer: GlobalClock,
    neighborhood: Set<StringUID>
) : SensorNode(protelisProgram, sleepTime, sensorDeviceUID, applicationUID, mqttClient,
    position, sensorTypes, neighborhood) {

    override fun createContext(): SensorECForDingNet {
        return SensorECForDingNet(this, applicationUID, mqttClient, networkManager)
    }

    init {
        timer.addPeriodicTrigger(startingTime, sleepTime) { runVM() }
    }
}
