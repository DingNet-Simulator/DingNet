package it.unibo.acdingnet.protelis.util

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import it.unibo.acdingnet.protelis.model.LatLongPosition
import org.jxmapviewer.viewer.GeoPosition
import org.protelis.lang.datatype.Tuple

val Float.Companion.SIZE_BYTES: Int get() = 4

val Double.Companion.SIZE_BYTES: Int get() = 8

fun LatLongPosition.toGeoPosition(): GeoPosition =
    GeoPosition(this.getLatitude(), this.getLongitude())

fun LatLongPosition.travel(destination: LatLongPosition, distance: Double): LatLongPosition {
    val source = LatLng(getLatitude(), getLongitude())
    val dest = LatLng(destination.getLatitude(), destination.getLongitude())
    LatLngTool
        .travel(source, LatLngTool.initialBearing(source, dest), distance, LengthUnit.METER)
        .also { return LatLongPosition(it) }
}

fun Tuple.toLatLongPosition() =
    LatLongPosition(this[0] as Double, this[1] as Double)
