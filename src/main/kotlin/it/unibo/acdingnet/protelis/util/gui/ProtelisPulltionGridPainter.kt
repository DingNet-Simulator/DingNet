package it.unibo.acdingnet.protelis.util.gui

import application.pollution.PollutionGrid
import gui.mapviewer.PollutionGridPainter
import java.awt.Color

class ProtelisPulltionGridPainter(pollutionGrid: PollutionGrid) : PollutionGridPainter(pollutionGrid) {

    override fun getColor(airQuality: Float): Color = when {
        airQuality > 100 -> Color.decode("#e8416f")
        airQuality > 75 -> Color.decode("#f29305")
        airQuality > 50 -> Color.decode("#eec20b")
        airQuality > 25 -> Color.decode("#c0e010")
        else -> Color.decode("#4edc2e")
    }
}
