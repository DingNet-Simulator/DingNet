package it.unibo.acdingnet.protelis.dingnetwrapper

import iot.GlobalClock
import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.node.SensorNode
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ProtelisProgram
import util.time.Time

class SensorNodeWrapper(
    protelisProgram: ProtelisProgram,
    startingTime: Time,
    sleepTime: Long,
    sensorDeviceUID: StringUID,
    applicationUID: String,
    netManagerMqttClient: MqttClientBasicApi,
    execContextMqttClient: MqttClientBasicApi,
    position: LatLongPosition,
    sensorTypes: List<SensorType>,
    val timer: GlobalClock,
    neighborhood: Set<StringUID>
) : SensorNode(protelisProgram, sleepTime, sensorDeviceUID, applicationUID, netManagerMqttClient,
    execContextMqttClient, position, sensorTypes, neighborhood) {

    override fun createContext(): SensorECForDingNet {
        return SensorECForDingNet(this, applicationUID, execContextMqttClient, networkManager)
    }

    init {
        timer.addPeriodicTrigger(startingTime, sleepTime) { runVM() }
    }

    //default value for sensors that have not receive value yet
    fun getPollutionValue(): Double = executionContext.executionEnvironment.get(Const.ProtelisEnv.IAQLEVEL, 40.0) as Double
}
