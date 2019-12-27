package it.unibo.acdingnet.protelis.neighborhood

import iot.mqtt.MqttClientBasicApi
import iot.mqtt.MqttMessageType
import it.unibo.acdingnet.protelis.model.LatLongPosition
import org.protelis.lang.datatype.impl.StringUID

data class NeighborhoodMessage(val type: MessageType, val node: Node): MqttMessageType {

    enum class MessageType {ADD, UPDATE, LEAVE}
}

data class NewNeighborhoodMessage(val neighborhood: Set<Pair<Node, Set<Node>>>): MqttMessageType

data class Node(val uid: StringUID, var position: LatLongPosition) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass == other?.javaClass && uid == (other as Node).uid) return true
        return false
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
}

/**
 * Simple NeighborhoodManager that suppose symmetric distance between two position
 */
class NeighborhoodManager(val applicationUID: String, private val mqttClient: MqttClientBasicApi, val range: Double) {

    object NeighborhoodManager {
        fun computeNeighborhood(node: Node, nodes: Set<Node>, range: Double) =
            nodes.filter { it != node }.filter { node.position.distanceTo(it.position) < range }.toSet()
    }

    val neighborhood: MutableMap<Node, MutableSet<Node>> = mutableMapOf()

    private val subscribedTopic: String = "application/$applicationUID/neighborhoodManager"

    init {
        mqttClient.connect()
        mqttClient.subscribe(this, subscribedTopic, NeighborhoodMessage::class.java) { _, msg ->
            when(msg.type) {
                NeighborhoodMessage.MessageType.ADD -> addNode(msg.node)
                NeighborhoodMessage.MessageType.LEAVE -> removeNode(msg.node)
                NeighborhoodMessage.MessageType.UPDATE -> updateNode(msg.node)
            }
            sendNewNeigh()
        }
    }

    private fun addNode(node: Node) {
        val nodeNeighborhood = neighborhood.keys.filter { node.position.distanceTo(it.position) < range }.toMutableSet()
        neighborhood += (node to nodeNeighborhood)
        nodeNeighborhood.forEach{
            neighborhood[it]!! += node
        }
    }

    private fun removeNode(node: Node) {
        val nodeNeighborhood = neighborhood[node]
        neighborhood -= node
        nodeNeighborhood?.forEach{
            neighborhood[it]?.let {
                it -= node
            }
        }
    }

    private fun updateNode(node: Node) {
        val newNeighborhood = neighborhood.keys.filter { it != node && node.position.distanceTo(it.position) < range }.toMutableSet()
        newNeighborhood.filter { !neighborhood[node]!!.contains(it) }.also {
            it.forEach{ neighborhood[it]!! += node }
        }
        neighborhood[node]?.filter { !newNeighborhood.contains(it) }.also {
            it?.forEach{ neighborhood[it]!! -= node }
        }
        neighborhood[node] = newNeighborhood
    }

    private fun sendNewNeigh() = mqttClient.publish("application/$applicationUID/neighborhood", NewNeighborhoodMessage(neighborhood.map { Pair(it.key,it.value) }.toSet()))
}
