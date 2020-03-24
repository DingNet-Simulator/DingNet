package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.node.BuildingNode
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.CURRENT_TEMP
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.DESIRED_TEMP
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.MAX_TEMP
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

class BuildingEC(
    private val buildingNode: BuildingNode,
    desiredTemp: Double,
    private val deltaTemp: Double,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager,
    randomSeed: Int = 1,
    execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : SensorExecutionContext(buildingNode, applicationUID, mqttClient,
    netmgr, randomSeed, execEnvironment) {

    init {
        execEnvironment.put(MAX_TEMP, desiredTemp)
        execEnvironment.put(DESIRED_TEMP, desiredTemp)
        execEnvironment.put(CURRENT_TEMP, (desiredTemp - deltaTemp * 5))
    }

    override fun instance(): BuildingEC = this

    fun getDecreaseDelta() = deltaTemp
    fun getIncreaseDelta() = deltaTemp

    override fun getCurrentTime(): Number {
        return buildingNode.timer.time.asSecond()
    }
}
