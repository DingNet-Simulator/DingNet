package it.unibo.acdingnet.protelis.node

import iot.mqtt.MqttClientBasicApi
import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.model.SensorType
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ProtelisProgram

abstract class NodeWithSensor(
    protelisProgram: ProtelisProgram,
    sleepTime: Long,
    sensorDeviceUID: StringUID,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    position: LatLongPosition,
    val sensorTypes: List<SensorType>,
    neighbors: Set<StringUID> = emptySet()
    ) : GenericNode(protelisProgram, sleepTime, sensorDeviceUID, applicationUID, mqttClient, position, neighbors)
