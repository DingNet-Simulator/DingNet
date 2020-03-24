package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.model.LatLongPosition
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.Field
import org.protelis.lang.datatype.Tuple
import org.protelis.lang.datatype.impl.ArrayTupleImpl
import org.protelis.lang.datatype.impl.StringUID
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.LocalizedDevice
import org.protelis.vm.NetworkManager
import org.protelis.vm.SpatiallyEmbeddedDevice
import org.protelis.vm.impl.AbstractExecutionContext
import org.protelis.vm.impl.SimpleExecutionEnvironment
import java.time.LocalDateTime
import kotlin.random.Random

abstract class PositionedExecutionContext(
    private val _deviceUID: StringUID,
    protected var nodePosition: LatLongPosition,
    netmgr: NetworkManager,
    randomSeed: Int = 1,
    execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : AbstractExecutionContext<PositionedExecutionContext>(execEnvironment, netmgr),
    LocalizedDevice, SpatiallyEmbeddedDevice<Double> {

    private val randomGenerator = Random(randomSeed)

    override fun nextRandomDouble(): Double = randomGenerator.nextDouble()
    override fun getDeviceUID(): DeviceUID = _deviceUID
    override fun getCurrentTime(): Number = LocalDateTime.now().second
    override fun getCoordinates(): Tuple =
        ArrayTupleImpl(nodePosition.getLatitude(), nodePosition.getLongitude())
    override fun nbrVector(): Field<Tuple> = TODO("not implemented")
    override fun nbrRange(): Field<Double> =
        buildField({ it.distanceTo(nodePosition) }, nodePosition)

    // region to move in Utils object
    @JvmOverloads
    fun log(log: Any, who: DeviceUID = _deviceUID) = println("$who: $log")
    @JvmOverloads
    fun logField(field: Field<*>, who: DeviceUID = _deviceUID) = this.log(field.toString(), who)
    // endregion
}
