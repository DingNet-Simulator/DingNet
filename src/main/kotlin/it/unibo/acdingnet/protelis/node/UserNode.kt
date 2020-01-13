package it.unibo.acdingnet.protelis.node

import it.unibo.acdingnet.protelis.executioncontext.UserExecutionContext
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.model.LatLongPosition
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionContext
import org.protelis.vm.ProtelisProgram

class UserNode(
    protelisProgram: ProtelisProgram,
    sleepTime: Long,
    userDeviceUID: StringUID,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    position: LatLongPosition,
    sensorTypes: List<SensorType> = emptyList()
) : SensorNode(protelisProgram, sleepTime, userDeviceUID, applicationUID,
    mqttClient, mqttClient, position, sensorTypes) {

    override fun createContext(): ExecutionContext =
        UserExecutionContext(
            this,
            applicationUID,
            execContextMqttClient,
            networkManager
        )
}
