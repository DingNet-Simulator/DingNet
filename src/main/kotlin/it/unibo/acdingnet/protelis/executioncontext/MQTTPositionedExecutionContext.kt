package it.unibo.acdingnet.protelis.executioncontext

import iot.mqtt.MqttClientBasicApi
import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.model.LoRaTransmission
import it.unibo.acdingnet.protelis.mqtt.LoRaTransmissionWrapper
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

abstract class MQTTPositionedExecutionContext(
    _deviceUID: StringUID,
    nodePosition: LatLongPosition,
    val applicationUID: String,
    protected val mqttClient: MqttClientBasicApi,
    val netmgr: NetworkManager,
    val randomSeed: Int = 1,
    protected val execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : PositionedExecutionContext(_deviceUID, nodePosition, netmgr, randomSeed, execEnvironment) {

    private val baseTopic: String = "application/$applicationUID/node/${_deviceUID.uid}/"
    private val receiveTopic: String = "${baseTopic}rx"

    init {
        mqttClient.connect()
        mqttClient.subscribe(this, receiveTopic, LoRaTransmissionWrapper::class.java) { t, m -> handleDeviceTransmission(t, m.transmission)}
    }

    protected abstract fun handleDeviceTransmission(topic: String, message: LoRaTransmission)

}
