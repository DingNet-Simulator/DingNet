package it.unibo.acdingnet.protelis.networkmanager

import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.google.gson.JsonSerializer
import it.unibo.acdingnet.protelis.util.Topics
import it.unibo.mqttclientwrapper.MQTTClientSingleton
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.mqttclientwrapper.api.MqttMessageType
import org.apache.commons.lang3.SerializationUtils
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.CodePath
import org.protelis.vm.NetworkManager
import java.io.Serializable
import java.util.*

data class MessageState(val payload: Map<CodePath, Any>) : Serializable, MqttMessageType {

    companion object {
        val jsonSerializer = JsonSerializer<MessageState> { state, _, _ ->
            val obj = JsonObject().also {
                it.addProperty(
                    "state",
                    Base64.getEncoder().encodeToString(SerializationUtils.serialize(state))
                )
            }
            obj
        }

        val jsonDeserialier = JsonDeserializer { jsonElement, _, _ ->
            SerializationUtils.deserialize<MessageState>(Base64.getDecoder().decode(
                jsonElement.asJsonObject["state"].asString))
        }
    }
}

open class MQTTNetworkManager(
    val deviceUID: StringUID,
    protected var mqttClient: MqttClientBasicApi = MQTTClientSingleton.instance,
    protected val applicationEUI: String,
    private var neighbors: Set<StringUID> = emptySet()
) : NetworkManager {

    private var messages: Map<DeviceUID, Map<CodePath, Any>> = emptyMap()

    init {

        mqttClient
            .addSerializer(
                MessageState::class.java,
                MessageState.jsonSerializer
            )
            .addDeserializer(
                MessageState::class.java,
                MessageState.jsonDeserialier
            )

        neighbors.forEach { subscribeToMqtt(it) }
    }

    protected fun subscribeToMqtt(deviceUID: StringUID) {
        mqttClient.subscribe(this, Topics.nodeStateTopic(applicationEUI, deviceUID),
            MessageState::class.java) { _, message ->
            messages += deviceUID to message.payload
        }
    }

    override fun shareState(toSend: Map<CodePath, Any>): Unit =
        mqttClient.publish(
            Topics.nodeStateTopic(applicationEUI, deviceUID),
            MessageState(toSend)
        )

    override fun getNeighborState(): Map<DeviceUID, Map<CodePath, Any>> =
        messages.apply { messages = emptyMap() }

    fun setNeighbors(neighbors: Set<StringUID>) {
        // remove sensor not more neighbors
        this.neighbors
            .filter { !neighbors.contains(it) }
            .forEach { mqttClient.unsubscribe(this, Topics.nodeStateTopic(applicationEUI, it)) }
        // add new neighbors
        neighbors.filter { !this.neighbors.contains(it) }.forEach { subscribeToMqtt(it) }
        this.neighbors = neighbors
    }

    protected fun getNeighbors() = neighbors
}
