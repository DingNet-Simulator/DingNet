package it.unibo.acdingnet.protelis.util.gui

import application.pollution.PollutionGrid
import application.pollution.PollutionLevel
import org.jxmapviewer.viewer.GeoPosition
import util.MapHelper

data class ProtelisPollutionGrid(
    val sensors: List<Pair<GeoPosition, Double>>,
    val range: Double,
    val defaultValue: Double
): PollutionGrid {

    override fun clean() { }

    override fun getPollutionLevel(position: GeoPosition): Double {
        val mapped = sensors
            .map { Pair(MapHelper.distanceMeter(it.first, position), it.second) }
            .filter { it.first < range }
        if (mapped.isEmpty()) {
            return defaultValue
        }
        mapped.find { it.first < MapHelper.DISTANCE_THRESHOLD_ROUNDING_ERROR }?.let { return it.second }
        return mapped.map { it.second / it.first }.reduce { r1, r2 -> r1 + r2} /
            mapped.map { 1 / it.first }.reduce { r1, r2 -> r1 + r2}
    }

    override fun addMeasurement(deviceEUI: Long, position: GeoPosition?, level: PollutionLevel?) { }
}
