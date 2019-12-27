package it.unibo.acdingnet.protelis.loader

import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.model.SensorType

data class DeviceWrapper(
    val devEUI: Long,
    val location: LatLongPosition,
    val transmissionPower: Double,
    val spreadingFactor: Int,
    val energyLevel: Double,
    val movementSpeed: Double,
    val sensors: List<SensorType>,
    val isUserMote: Boolean = false
)