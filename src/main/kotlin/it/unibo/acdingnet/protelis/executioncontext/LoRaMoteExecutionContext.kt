package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.model.LoRaTransmission
import it.unibo.acdingnet.protelis.model.MessageType
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.mqtt.LoRaTransmissionWrapper
import it.unibo.acdingnet.protelis.node.NodeWithSensor
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.mqttclientwrapper.api.MqttMessageType
import it.unibo.protelisovermqtt.executioncontext.PositionedExecutionContext
import it.unibo.protelisovermqtt.util.Topics
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

abstract class LoRaMoteExecutionContext(
    private val node: NodeWithSensor,
    val applicationUID: String,
    protected val mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager,
    randomSeed: Int = 1,
    protected val execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : PositionedExecutionContext(node.deviceUID, node.position,
    netmgr, randomSeed, execEnvironment) {
    private var sensorsValue: Map<SensorType, Double> = emptyMap()

    init {
        subscribeTopic(
            Topics.nodeReceiveTopic(applicationUID, node.deviceUID),
            LoRaTransmissionWrapper::class.java) { _, msg ->
            handleDeviceTransmission(msg.transmission)
        }
    }

    protected fun <T : MqttMessageType> subscribeTopic(
        topic: String,
        type: Class<T>,
        consumer: (topic: String, message: T) -> Unit
    ) = mqttClient.subscribe(this, topic, type, consumer)

    protected fun handleDeviceTransmission(message: LoRaTransmission) {
        val payload = message.content.payload.toMutableList()
        if (payload.isNotEmpty() &&
            payload[0] == MessageType.SENSOR_VALUE.code ||
            payload[0] == MessageType.KEEPALIVE.code) {

            payload.removeAt(0)
            node.sensorTypes.forEach {
                when (it) {
                    SensorType.GPS -> {
                        nodePosition = it.consumeAndConvert(payload)
                        node.position = nodePosition
                    }
                    SensorType.IAQ -> throw IllegalArgumentException("IAQ sensor shouldn't be used")
                    else -> {
                        val value: Double = it.consumeAndConvert(payload)
                        execEnvironment.put(it.name, value)
                        sensorsValue = sensorsValue.plus(Pair(it, value))
                    }
                }
            }
            manageSensorValues(sensorsValue)
        }
    }

    abstract fun manageSensorValues(sensorsValue: Map<SensorType, Double>)
}
