package it.unibo.acdingnet.protelis.util

import io.jenetics.jpx.GPX
import io.jenetics.jpx.Track
import it.unibo.acdingnet.protelis.model.GPSPosition
import it.unibo.acdingnet.protelis.model.GPSTrace
import it.unibo.protelisovermqtt.model.LatLongPosition
import java.io.InputStream
import kotlin.streams.toList

object LoadGPXFile {

    fun loadFile(stream: InputStream, startingTime: Double): List<GPSTrace> {
        return GPX.read(stream)
            .tracks()
            .map { trackToGPSTrace(it) }
            .map { it.positions
                .map { pos -> GPSPosition(pos.position, pos.time - startingTime) }
                .filter { pos -> pos.time >= 0 } }
            .map { GPSTrace(it) }
            .toList()
    }

    private fun trackToGPSTrace(track: Track): GPSTrace {
        /*
         * No segments
         */
        if (track.segments.isEmpty()) {
            throw IllegalStateException("Track $track contains no segment")
        }
        /*
         * Empty segments
         */
        if (track.segments.map { it.points }.any { it.isEmpty() } ) {
            throw IllegalStateException("Track $track contains at least a segment with no points")
        }

        track.segments
            .flatMap { it.points }
            .map { GPSPosition(
                LatLongPosition(it.latitude.toDouble(), it.longitude.toDouble()),
                it.time
                    .orElseThrow { IllegalStateException("Track $track contains at least a waypoint without timestamp") }
                    .toInstant().toEpochMilli().toDouble()
            ) }
            .sorted()
            .also { return GPSTrace(it) }
    }
}
