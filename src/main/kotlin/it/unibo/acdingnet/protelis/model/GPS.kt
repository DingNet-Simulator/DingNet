package it.unibo.acdingnet.protelis.model

import it.unibo.protelisovermqtt.model.LatLongPosition

data class GPSPosition(val position: LatLongPosition, val time: Double) : Comparable<GPSPosition> {

    override fun compareTo(other: GPSPosition): Int = time.compareTo(other.time)
}

data class GPSTrace(val positions: List<GPSPosition>)
