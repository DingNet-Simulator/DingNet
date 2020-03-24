package it.unibo.acdingnet.protelis.neighborhood

import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.util.Topics
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.mqttclientwrapper.api.MqttMessageType
import org.protelis.lang.datatype.impl.StringUID

data class NeighborhoodMessage(val type: MessageType, val node: Node): MqttMessageType  {

    enum class MessageType { ADD, UPDATE, LEAVE }
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
class NeighborhoodManager(
    val applicationUID: String,
    private val mqttClient: MqttClientBasicApi,
    val range: Double,
    initialGroup: Set<Node> = emptySet()
) {

    companion object {
        fun computeNeighborhood(node: Node, nodes: Set<Node>, range: Double) =
            nodes.filter { it != node }
                .filter { node.position.distanceTo(it.position) < range }
                .toSet()
    }

    val neighborhood: MutableMap<Node, MutableSet<Node>>

    init {
        neighborhood =
            initialGroup
                .map { it to computeNeighborhood(
                    it,
                    initialGroup,
                    range
                ).toMutableSet() }
                .toMap().toMutableMap()
        mqttClient.subscribe(this, Topics.neighborhoodManagerTopic(applicationUID),
            NeighborhoodMessage::class.java) { _, msg ->
                when (msg.type) {
                    NeighborhoodMessage.MessageType.ADD -> addNode(msg.node)
                    NeighborhoodMessage.MessageType.LEAVE -> removeNode(msg.node)
                    NeighborhoodMessage.MessageType.UPDATE -> updateNode(msg.node)
                }
                sendNewNeigh()
            }
    }

    fun getNeighborhoodByNodeId(id: StringUID): Set<Node> =
        neighborhood[Node(id, LatLongPosition.zero())].orEmpty()

    private fun addNode(node: Node) {
        val nodeNeighborhood = neighborhood.keys
            .filter { node.position.distanceTo(it.position) < range }.toMutableSet()
        neighborhood += (node to nodeNeighborhood)
        nodeNeighborhood.forEach {
            neighborhood[it]!! += node
        }
    }

    private fun removeNode(node: Node) {
        val nodeNeighborhood = neighborhood[node]
        neighborhood -= node
        nodeNeighborhood?.forEach {
            neighborhood[it]?.let {
                it -= node
            }
        }
    }

    private fun updateNode(node: Node) {
        val newNeighborhood = neighborhood.keys
            .filter { it != node && node.position.distanceTo(it.position) < range }.toMutableSet()
        newNeighborhood.filter { !neighborhood[node]!!.contains(it) }.also {
            it.forEach { neighborhood[it]!! += node }
        }
        neighborhood[node]?.filter { !newNeighborhood.contains(it) }.also {
            it?.forEach { neighborhood[it]!! -= node }
        }
        neighborhood[node] = newNeighborhood
    }

    private fun sendNewNeigh() = mqttClient.publish(
        Topics.neighborhoodTopic(applicationUID),
        NewNeighborhoodMessage(neighborhood.map {
            Pair(
                it.key,
                it.value
            )
        }.toSet())
    )
}
