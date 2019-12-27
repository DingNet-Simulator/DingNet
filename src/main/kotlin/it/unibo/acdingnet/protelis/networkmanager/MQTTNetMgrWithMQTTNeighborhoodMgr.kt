package it.unibo.acdingnet.protelis.networkmanager

import iot.mqtt.MqttClientBasicApi
import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.neighborhood.NeighborhoodMessage
import it.unibo.acdingnet.protelis.neighborhood.NeighborhoodMessage.MessageType
import it.unibo.acdingnet.protelis.neighborhood.NewNeighborhoodMessage
import it.unibo.acdingnet.protelis.neighborhood.Node
import org.protelis.lang.datatype.impl.StringUID

open class MQTTNetMgrWithMQTTNeighborhoodMgr(
    deviceUID: StringUID,
    mqttClient: MqttClientBasicApi,
    applicationEUI: String,
    initialPosition: LatLongPosition,
    neighbors: Set<StringUID> = emptySet()
) : MQTTNetworkManager(deviceUID, mqttClient, applicationEUI, emptySet()) {

    private val publishOnTopic: String = "application/$applicationEUI/neighborhoodManager"
    private val subscribeOnTopic: String = "application/$applicationEUI/neighborhood"

    init {
        mqttClient.subscribe(this, subscribeOnTopic, NewNeighborhoodMessage::class.java) {
            _, message ->
            message.neighborhood
                .find { it.first.uid == deviceUID }
                ?.let { setNeighbors(it.second.map { it.uid }.toSet()) }
        }
        if (neighbors.isEmpty()) {
            mqttClient.publish(publishOnTopic, generateMessage(MessageType.ADD, deviceUID, initialPosition))
        } else {
            setNeighbors(neighbors)
        }
    }

    fun changePosition(position: LatLongPosition) =
        mqttClient.publish(publishOnTopic, generateMessage(MessageType.UPDATE, deviceUID, position))


    fun nodeDeleted() = mqttClient.publish(publishOnTopic, generateMessage(MessageType.LEAVE, deviceUID, LatLongPosition.zero()))

    private fun generateMessage(type: MessageType, uid: StringUID, position: LatLongPosition) =
        generateMessage(type, Node(uid, position))

    private fun generateMessage(type: MessageType, node: Node) = NeighborhoodMessage(type, node)

}
