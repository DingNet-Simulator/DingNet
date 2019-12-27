package it.unibo.acdingnet.protelis.node

import iot.mqtt.MqttClientBasicApi
import it.unibo.acdingnet.protelis.executioncontext.SensorExecutionContext
import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.model.SensorType
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionContext
import org.protelis.vm.ProtelisProgram


//TODO if is only a sensor node (not an extension) check sensorTypes size > 0
open class SensorNode(
    protelisProgram: ProtelisProgram,
    sleepTime: Long,
    sensorDeviceUID: StringUID,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    position: LatLongPosition,
    sensorTypes: List<SensorType>,
    neighbors: Set<StringUID> = emptySet())
    : NodeWithSensor(protelisProgram, sleepTime, sensorDeviceUID, applicationUID, mqttClient, position, sensorTypes, neighbors) {

    override fun createContext(): ExecutionContext =
        SensorExecutionContext(
            this,
            applicationUID,
            mqttClient,
            networkManager
        )
}
