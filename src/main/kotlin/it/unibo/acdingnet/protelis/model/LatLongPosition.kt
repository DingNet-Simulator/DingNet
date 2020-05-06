package it.unibo.acdingnet.protelis.model

import com.javadocmd.simplelatlng.LatLng
import java.io.Serializable
import java.nio.ByteBuffer

/**
 * LatLong position in meter
 */
data class LatLongPosition(private val latLong: LatLng) : Serializable {

    constructor(latitude: Double, longitude: Double) : this(LatLng(latitude, longitude))

    fun getLatitude() = latLong.latitude
    fun getLongitude() = latLong.longitude

    fun distanceTo(position: LatLongPosition): Double {
        // TODO this can be lazy but is necessary modify conversion to json
        val lat1R = Math.toRadians(getLatitude())
        // TODO this can be lazy but is necessary modify conversion to json
        val long1R = Math.toRadians(getLongitude())
        val lat2R = Math.toRadians(position.getLatitude())
        val long2R = Math.toRadians(position.getLongitude())

        val x = (long2R - long1R) * Math.cos((lat1R + lat2R) / 2)
        val y = (lat2R - lat1R)
        return Math.sqrt(x * x + y * y) * EARTH_MEAN_RADIUS_METERS
    }

    fun toBytes(): List<Byte> {
        val data = ByteArray(8)
        ByteBuffer.wrap(data, 0, 4).putFloat(getLatitude().toFloat())
        ByteBuffer.wrap(data, 4, 4).putFloat(getLongitude().toFloat())
        return data.toList()
    }

    companion object {
        const val EARTH_MEAN_RADIUS_METERS: Double = 6371009.0
        fun zero() = LatLongPosition(0.0, 0.0)
    }
}
