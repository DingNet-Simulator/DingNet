package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.util.Const
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment

class DestinationExecutionContext(
    private val _deviceUID: StringUID,
    private val nodePosition: LatLongPosition,
    private val netmgr: NetworkManager,
    private val randomSeed: Int = 1,
    private val execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
    ) : PositionedExecutionContext(_deviceUID, nodePosition, netmgr, randomSeed, execEnvironment) {

    init {
        //add variable env per destination
        execEnvironment.put(Const.ProtelisEnv.DESTINATION_KEY, true)
    }

    override fun instance(): DestinationExecutionContext =
        DestinationExecutionContext(
            _deviceUID,
            nodePosition,
            netmgr,
            randomSeed,
            execEnvironment
        )

}