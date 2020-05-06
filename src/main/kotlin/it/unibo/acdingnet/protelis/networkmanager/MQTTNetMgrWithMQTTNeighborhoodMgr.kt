package it.unibo.acdingnet.protelis.networkmanager

import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.neighborhood.NeighborhoodMessage
import it.unibo.acdingnet.protelis.neighborhood.NeighborhoodMessage.MessageType
import it.unibo.acdingnet.protelis.neighborhood.NewNeighborhoodMessage
import it.unibo.acdingnet.protelis.neighborhood.Node
import it.unibo.acdingnet.protelis.util.Topics
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import org.protelis.lang.datatype.impl.StringUID

open class MQTTNetMgrWithMQTTNeighborhoodMgr(
    deviceUID: StringUID,
    mqttClient: MqttClientBasicApi,
    applicationEUI: String,
    initialPosition: LatLongPosition,
    neighbors: Set<StringUID> = emptySet()
) : MQTTNetworkManager(deviceUID, mqttClient, applicationEUI, emptySet()) {

    init {
        mqttClient.subscribe(this, Topics.neighborhoodTopic(applicationEUI),
            NewNeighborhoodMessage::class.java) {
            _, message ->
            message.neighborhood
                .find { it.first.uid == deviceUID }
                ?.let { setNeighbors(it.second.map { it.uid }.toSet()) }
        }
        if (neighbors.isEmpty()) {
            mqttClient.publish(
                Topics.neighborhoodManagerTopic(applicationEUI),
                generateMessage(MessageType.ADD, deviceUID, initialPosition))
        } else {
            setNeighbors(neighbors)
        }
    }

    fun changePosition(position: LatLongPosition) =
        mqttClient.publish(
            Topics.neighborhoodManagerTopic(applicationEUI),
            generateMessage(MessageType.UPDATE, deviceUID, position))

    fun nodeDeleted() = mqttClient.publish(
        Topics.neighborhoodManagerTopic(applicationEUI),
        generateMessage(MessageType.LEAVE, deviceUID, LatLongPosition.zero()))

    private fun generateMessage(type: MessageType, uid: StringUID, position: LatLongPosition) =
        generateMessage(type, Node(uid, position))

    private fun generateMessage(type: MessageType, node: Node) =
        NeighborhoodMessage(type, node)
}
