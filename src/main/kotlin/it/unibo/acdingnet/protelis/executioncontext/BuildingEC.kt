package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.node.BuildingNode
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.BUILDING_TYPE
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.CURRENT_TEMP
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.DESIRED_TEMP
import it.unibo.acdingnet.protelis.util.Const.ProtelisEnv.NODE_TYPE
import it.unibo.protelisovermqtt.executioncontext.PositionedExecutionContext
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

class BuildingEC(
    private val buildingNode: BuildingNode,
    private val desiredTemp: Double,
    private val deltaTemp: Double,
    private val netmgr: NetworkManager,
    private val randomSeed: Int = 1,
    private val execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : PositionedExecutionContext(buildingNode.deviceUID, buildingNode.position,
    netmgr, randomSeed, execEnvironment) {

    init {
        execEnvironment.put(NODE_TYPE, BUILDING_TYPE)
        execEnvironment.put(DESIRED_TEMP, desiredTemp)
        val a = desiredTemp - deltaTemp * 2
        execEnvironment.put(CURRENT_TEMP, a)
    }
    
    override fun instance(): BuildingEC = BuildingEC(
        buildingNode,
        desiredTemp,
        deltaTemp,
        netmgr,
        randomSeed,
        execEnvironment
    )

    fun maxTempByPollution(pollutionField: Any): Double {
        println(pollutionField)
        return 20.5
    }
    
    fun getDecreaseDelta() = deltaTemp
    fun getIncreaseDelta() = deltaTemp

    override fun getCurrentTime(): Number {
        return buildingNode.timer.time.second
    }

    fun prova() {
        println("prova")
    }
}
