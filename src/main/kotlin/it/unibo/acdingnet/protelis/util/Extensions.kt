package it.unibo.acdingnet.protelis.util

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import it.unibo.protelisovermqtt.model.LatLongPosition
import org.jxmapviewer.viewer.GeoPosition

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
