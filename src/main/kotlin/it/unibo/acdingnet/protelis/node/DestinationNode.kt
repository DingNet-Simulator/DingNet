package it.unibo.acdingnet.protelis.node

import iot.mqtt.MqttClientBasicApi
import it.unibo.acdingnet.protelis.executioncontext.DestinationExecutionContext
import it.unibo.acdingnet.protelis.model.LatLongPosition
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionContext
import org.protelis.vm.ProtelisProgram

class DestinationNode(
    protelisProgram: ProtelisProgram,
    sleepTime: Long,
    destinationUID: StringUID,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    position: LatLongPosition
) : GenericNode(protelisProgram, sleepTime, destinationUID, applicationUID, mqttClient, position) {

    override fun createContext(): ExecutionContext =
        DestinationExecutionContext(deviceUID, position, networkManager)
}
