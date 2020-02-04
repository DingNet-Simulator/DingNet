package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.model.LoRaTransmission
import it.unibo.acdingnet.protelis.model.MessageType
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.mqtt.LoRaTransmissionWrapper
import it.unibo.acdingnet.protelis.node.DestinationNode
import it.unibo.acdingnet.protelis.node.UserNode
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.model.LatLongPosition
import it.unibo.protelisovermqtt.util.Topics
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

class UserExecutionContext(
    private val userNode: UserNode,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager,
    randomSeed: Int = 1,
    execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : SensorExecutionContext(userNode, applicationUID, mqttClient, netmgr,
    randomSeed, execEnvironment) {

    private var destinationPosition: LatLongPosition? = null

    init {
        subscribeTopic(Topics.nodeReceiveTopic(applicationUID, userNode.deviceUID),
            LoRaTransmissionWrapper::class.java) { _, msg ->
            handleUserDeviceTransmission(msg.transmission)
        }
    }

    override fun instance(): UserExecutionContext = this

    private fun handleUserDeviceTransmission(message: LoRaTransmission) {
        val payload = message.content.payload
        if (payload.isNotEmpty()) {
            when (payload[0]) {
                MessageType.SENSOR_VALUE.code -> super.handleDeviceTransmission(message)
                MessageType.REQUEST_PATH.code -> handleRequestPath(payload.toMutableList().also {
                    it.removeAt(0) })
                else -> throw IllegalArgumentException("message type not supported")
            }
        }
    }

    // TODO
    private fun handleRequestPath(mutPayload: MutableList<Byte>) {
        // TODO put environment variable to start the path
        execEnvironment.put(Const.ProtelisEnv.SOURCE_KEY, true)
        // update position
        userNode.position = SensorType.GPS.consumeAndConvert(mutPayload)
        // TODO create destination node
        destinationPosition = SensorType.GPS.consumeAndConvert<LatLongPosition>(mutPayload).also {
            DestinationNode(userNode.protelisProgram, userNode.sleepTime, StringUID(""),
                applicationUID, mqttClient, it)
        }
    }
}
