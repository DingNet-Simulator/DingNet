package it.unibo.acdingnet.protelis.model.sensorconverter

import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.util.SIZE_BYTES
import java.nio.ByteBuffer

interface SensorConverter<T> {
    fun convert(length: Int, data: MutableList<Byte>): T
}

class DefaultConverter : SensorConverter<Double> {

    override fun convert(length: Int, data: MutableList<Byte>): Double {
        val buffer = ByteBuffer.wrap((1..length).map { data.removeAt(0) }.toByteArray())
        return when (length) {
            Byte.SIZE_BYTES -> (buffer.get().toInt() and 0xFF).toDouble()
            Short.SIZE_BYTES -> buffer.short.toDouble()
            Float.SIZE_BYTES -> buffer.float.toDouble()
            Double.SIZE_BYTES -> buffer.double
            else -> throw IllegalStateException("impossible convert. Sensor with unknown length")
        }
    }
}

class GPSConverter : SensorConverter<LatLongPosition> {
    override fun convert(length: Int, data: MutableList<Byte>): LatLongPosition {
        val buffer = ByteBuffer.wrap((1..length).map { data.removeAt(0) }.toByteArray())
        return LatLongPosition(
            buffer.float.toDouble(),
            buffer.getFloat(Float.SIZE_BYTES).toDouble()
        )
    }
}

class UnsupportedConversion : SensorConverter<Any> {
    override fun convert(length: Int, data: MutableList<Byte>): Double =
        throw UnsupportedOperationException()
}
