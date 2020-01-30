package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.model.LoRaTransmission
import it.unibo.acdingnet.protelis.model.MessageType
import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.mqtt.LoRaTransmissionWrapper
import it.unibo.acdingnet.protelis.node.SensorNode
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.NODE_TYPE
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.SENSOR_TYPE
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.executioncontext.MQTTPositionedExecutionContext
import it.unibo.protelisovermqtt.util.Topics
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

open class SensorExecutionContext @JvmOverloads constructor(
    private val sensorNode: SensorNode,
    val applicationUID: String,
    mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager,
    randomSeed: Int = 1,
    execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : MQTTPositionedExecutionContext(sensorNode.deviceUID, sensorNode.position, mqttClient, netmgr,
    randomSeed, execEnvironment) {

    private var sensorsValue: Map<SensorType, Double> = emptyMap()

    init {
        subscribeTopic(Topics.nodeReceiveTopic(applicationUID, sensorNode.deviceUID),
            LoRaTransmissionWrapper::class.java) { _, msg ->
            handleDeviceTransmission(msg.transmission)
        }
        execEnvironment.put(NODE_TYPE, SENSOR_TYPE)
    }

    override fun instance(): SensorExecutionContext =
        SensorExecutionContext(
            sensorNode,
            applicationUID,
            mqttClient,
            netmgr,
            randomSeed,
            execEnvironment
        )

    protected fun handleDeviceTransmission(message: LoRaTransmission) {
        val payload = message.content.payload.toMutableList()
        if (payload.isNotEmpty() && payload[0] == MessageType.SENSOR_VALUE.code) {
            payload.removeAt(0)
            sensorNode.sensorTypes.forEach {
                when (it) {
                    SensorType.GPS -> sensorNode.position = it.consumeAndConvert(payload)
                    SensorType.IAQ -> throw IllegalArgumentException("IAQ sensor shouldn't be used")
                    else -> {
                        val value: Double = it.consumeAndConvert(payload)
                        execEnvironment.put(it.name, value)
                        sensorsValue = sensorsValue.plus(Pair(it, value))
                    }
                }
            }
            sensorsValue
                .map { sensor -> IAQCalculator.computeIaqLevel(sensor.key, sensor.value) }
                .filterNotNull()
                .max()
                ?.let { value -> execEnvironment.put(Const.ProtelisEnv.IAQLEVEL_KEY, value) }
        }
    }
}

object IAQCalculator {

    private val iaqLevel: List<Pair<Int, Int>> = SensorType.IAQ.levels
    private val sensorsLevel: Map<SensorType, List<Pair<Int, Int>>> = mapOf(
        SensorType.PM10 to SensorType.PM10.levels,
        SensorType.NO2 to SensorType.NO2.levels
    )

    fun computeIaqLevel(sensorType: SensorType, value: Double): Double? {
        return sensorsLevel[sensorType]
            ?.mapIndexed { i, v -> Pair(i, v) }
            ?.find { it.second.first <= value && value < it.second.second }
            ?.let {
                val c = it.second
                val iaq = iaqLevel[it.first]
                1.0 * (iaq.second - iaq.first) / (c.second - c.first) *
                    (value - c.first) + iaq.first
            }
    }
}
