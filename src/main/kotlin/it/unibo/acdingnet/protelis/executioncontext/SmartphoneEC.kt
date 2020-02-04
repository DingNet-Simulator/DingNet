package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.node.SmartphoneNode
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import it.unibo.protelisovermqtt.executioncontext.MQTTPositionedExecutionContext
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

class SmartphoneEC(
    private val smartphoneNode: SmartphoneNode,
    mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager,
    randomSeed: Int = 1,
    execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : MQTTPositionedExecutionContext(smartphoneNode.deviceUID, smartphoneNode.position, mqttClient,
    netmgr, randomSeed, execEnvironment) {

    override fun instance(): SmartphoneEC = this
}
