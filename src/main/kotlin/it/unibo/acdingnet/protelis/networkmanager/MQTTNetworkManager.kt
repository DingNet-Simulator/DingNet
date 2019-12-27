package it.unibo.acdingnet.protelis.networkmanager

import iot.mqtt.MQTTClientFactory
import iot.mqtt.MqttClientBasicApi
import iot.mqtt.MqttMessageType
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.CodePath
import org.protelis.vm.NetworkManager
import java.io.Serializable

data class MessageState(val payload: Map<CodePath, Any>): Serializable, MqttMessageType

open class MQTTNetworkManager(
    val deviceUID: StringUID,
    protected var mqttClient: MqttClientBasicApi = MQTTClientFactory.getSingletonInstance(),
    applicationEUI: String,
    private var neighbors: Set<StringUID> = emptySet()): NetworkManager {

    protected val baseTopic: String =  "application/$applicationEUI/node/"

    private var messages: Map<DeviceUID, Map<CodePath, Any>> = emptyMap()

    init {
        mqttClient.connect()
        neighbors.forEach{subscribeToMqtt(it)}
    }

    private fun getMqttStateTopicByDevice(deviceUID: StringUID): String = "$baseTopic${deviceUID.uid}/state"

    protected fun subscribeToMqtt(deviceUID: StringUID) {
        mqttClient.subscribe(this, getMqttStateTopicByDevice(deviceUID), MessageState::class.java) { _, message ->
            messages += deviceUID to message.payload
        }
    }

    override fun shareState(toSend: Map<CodePath, Any>): Unit = mqttClient.publish(getMqttStateTopicByDevice(deviceUID), MessageState(toSend))

    override fun getNeighborState(): Map<DeviceUID, Map<CodePath, Any>> = messages.apply { messages = emptyMap() }

    fun setNeighbors(neighbors: Set<StringUID>) {
        //remove sensor not more neighbors
        this.neighbors.filter { !neighbors.contains(it) }.forEach{mqttClient.unsubscribe(this, getMqttStateTopicByDevice(it))}
        //add new neighbors
        neighbors.filter { !this.neighbors.contains(it) }.forEach{subscribeToMqtt(it)}
        this.neighbors = neighbors
    }

    protected fun getNeighbors() = neighbors
}
