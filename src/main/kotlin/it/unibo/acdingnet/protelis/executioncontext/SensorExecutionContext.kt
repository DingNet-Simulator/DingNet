package it.unibo.acdingnet.protelis.executioncontext

import it.unibo.acdingnet.protelis.model.SensorType
import it.unibo.acdingnet.protelis.node.SensorNode
import it.unibo.acdingnet.protelis.util.Const
import it.unibo.acdingnet.protelis.util.Interpolator
import it.unibo.acdingnet.protelis.util.toLatLongPosition
import it.unibo.mqttclientwrapper.api.MqttClientBasicApi
import org.protelis.lang.datatype.Tuple
import org.protelis.vm.ExecutionEnvironment
import org.protelis.vm.NetworkManager
import org.protelis.vm.impl.SimpleExecutionEnvironment
import java.math.RoundingMode

open class SensorExecutionContext @JvmOverloads constructor(
    sensorNode: SensorNode,
    applicationUID: String,
    mqttClient: MqttClientBasicApi,
    netmgr: NetworkManager,
    randomSeed: Int = 1,
    execEnvironment: ExecutionEnvironment = SimpleExecutionEnvironment()
) : LoRaMoteExecutionContext(sensorNode, applicationUID, mqttClient, netmgr,
    randomSeed, execEnvironment) {

    override fun instance(): SensorExecutionContext = this

    override fun manageSensorValues(sensorsValue: Map<SensorType, Double>) {
        sensorsValue
            .map { sensor -> IAQCalculator.computeIaqLevel(sensor.key, sensor.value) }
            .filterNotNull()
            .max()
            ?.let { value -> execEnvironment.put(Const.ProtelisEnv.IAQLEVEL, value) }
    }

    // region used in protelis program
    @JvmOverloads
    fun roundToDecimal(value: Double, numOfDecimal: Int = 1) = value.toBigDecimal()
        .setScale(numOfDecimal, RoundingMode.HALF_EVEN).toDouble()

    fun distanceTo(position: Tuple): Double {
        if (position.size() != 2 || position.toArray().any { it !is Double }) {
            throw IllegalStateException("$position not represent a valid LatLongPosition")
        }
        return coordinates.toLatLongPosition().distanceTo(position.toLatLongPosition())
    }

    fun temperatureByPollution(pollutionValue: Double): Double = when {
        pollutionValue < 1 -> 25.0
        pollutionValue > 100 -> 17.0
        else -> roundToDecimal(Interpolator.interpolateTempByPollution(pollutionValue))
    }
    //endregion
}

object IAQCalculator {

    private val iaqLevel: List<Pair<Int, Int>> = SensorType.IAQ.levels
    private val sensorsLevel: Map<SensorType, List<Pair<Int, Int>>> = mapOf(
        SensorType.PM10 to SensorType.PM10.levels,
        SensorType.NO2 to SensorType.NO2.levels
    )

    fun computeIaqLevel(sensorType: SensorType, value: Double): Double? {
        return sensorsLevel[sensorType]
            ?.mapIndexed { i, v -> Pair(i, v) }
            ?.find { it.second.first <= value && value < it.second.second }
            ?.let {
                val c = it.second
                val iaq = iaqLevel[it.first]
                1.0 * (iaq.second - iaq.first) / (c.second - c.first) *
                    (value - c.first) + iaq.first
            }
    }
}
