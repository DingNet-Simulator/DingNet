package it.unibo.acdingnet.protelis.loader

import it.unibo.acdingnet.protelis.model.LatLongPosition
import it.unibo.acdingnet.protelis.model.SensorType
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object Loader {

    fun loadDevice(path: String): MutableList<DeviceWrapper> {
        val idRemapping = IdRemapping()

        var inputStream =  Loader::class.java.getResourceAsStream(path)
        if (inputStream == null) {
            val file = File(path)
            if (file.exists()) {
                inputStream = file.inputStream()
            } else {
                throw IllegalArgumentException("configuration file not found: $path")
            }
        }

        //inputStream.bufferedReader().lines().reduce(String::plus).also { println(it) }
        val doc = DocumentBuilderFactory
            .newDefaultInstance()
            .newDocumentBuilder()
            .parse(inputStream)
         val configuration = doc.documentElement

        // ---------------
        //    WayPoints
        // ---------------

        val wayPointsElement = configuration.getElementsByTagName("wayPoints").item(0) as Element

        for (i in 0 until wayPointsElement.getElementsByTagName("wayPoint").length) {
            val waypoint = wayPointsElement.getElementsByTagName("wayPoint").item(i) as Element
            val wayPointLatitude = waypoint.textContent.split(",").toTypedArray()[0].toDouble()
            val wayPointLongitude = waypoint.textContent.split(",").toTypedArray()[1].toDouble()
            val id = waypoint.getAttribute("id").toLong()
            idRemapping.addWayPoint(id, LatLongPosition(wayPointLatitude, wayPointLongitude))
        }

        // ---------------
        //      Motes
        // ---------------

        val motes = configuration.getElementsByTagName("motes").item(0) as Element
        return (0 until motes.getElementsByTagName("mote").length).asSequence()
            .map{ motes.getElementsByTagName("mote").item(it) as Element }
            .map {
                DeviceWrapper(
                    readChild(it, "devEUI").toLong(),
                    getLocation(it, idRemapping),
                    readChild(it, "transmissionPower").toDouble(),
                    readChild(it, "spreadingFactor").toInt(),
                    readChild(it, "energyLevel").toDouble(),
                    readChild(it, "movementSpeed").toDouble(),
                    getSensors(it)
                )
            }
            .toMutableList()
            .also {list ->
                list.addAll(
                (0 until motes.getElementsByTagName("userMote").length).asSequence()
                    .map { motes.getElementsByTagName("userMote").item(it) as Element }
                    .map {
                        DeviceWrapper(
                            readChild(it, "devEUI").toLong(),
                            getLocation(it, idRemapping),
                            readChild(it, "transmissionPower").toDouble(),
                            readChild(it, "spreadingFactor").toInt(),
                            readChild(it, "energyLevel").toDouble(),
                            readChild(it, "movementSpeed").toDouble(),
                            getSensors(it),
                            true
                        )
                    })
            }
    }

    private fun getLocation(node: Element, idRemapping: IdRemapping): LatLongPosition {
        val location = node.getElementsByTagName("location").item(0) as Element
        val waypoint = location.getElementsByTagName("waypoint").item(0) as Element
        return idRemapping.getWayPointWithOriginalId(waypoint.getAttribute("id").toLong())
    }

    private fun getSensors(node: Element): List<SensorType> {
        val sensors = node.getElementsByTagName("sensors").item(0) as Element
        return (0 until sensors.getElementsByTagName("sensor").length).asSequence()
            .map { sensors.getElementsByTagName("sensor").item(it) as Element }
            .map { SensorType.valueOf(it.getAttribute("SensorType")) }
            .toList()
    }

    private fun readChild(element: Element, childName: String): String {
        return element.getElementsByTagName(childName).item(0).textContent
    }
}

class IdRemapping {
    private var IdMappingWayPoints: MutableMap<Long, Long> = mutableMapOf()
    private var wayPoints: MutableMap<Long, LatLongPosition> = mutableMapOf()
    private var newWayPointId: Long = 0
    /**
     * Add a waypoint to the remapping.
     * @param originalId The Id which the waypoint currently has.
     * @param pos The waypoint itself.
     * @return The new remapped Id for the given waypoint.
     */
    fun addWayPoint(originalId: Long, pos: LatLongPosition): Long {
        IdMappingWayPoints[originalId] = newWayPointId
        wayPoints[newWayPointId] = pos
        return newWayPointId++
    }

    /**
     * Get the new remapped Id for a waypoint.
     * @param originalId The original Id of the waypoint.
     * @return The new remapped Id for the given original Id.
     */
    fun getNewWayPointId(originalId: Long): Long {
        return IdMappingWayPoints[originalId]!!.toLong()
    }

    /**
     * Get the waypoint corresponding to its new remapped Id.
     * @param newId The new Id of the waypoint.
     * @return The waypoint corresponding to the new Id.
     */
    fun getWayPointWithNewId(newId: Long): LatLongPosition {
        return wayPoints[newId]!!.copy()
    }

    /**
     * Get the connection corresponding to its new remapped Id.
     * @param originalId The new Id of the connection.
     * @return The connection corresponding to the new Id.
     */
    fun getWayPointWithOriginalId(originalId: Long): LatLongPosition {
        return wayPoints[IdMappingWayPoints[originalId]]!!.copy()
    }

    /**
     * Get all the waypoints with their new remapped Ids.
     * @return A map with the new Ids mapped to the waypoints.
     */
    fun getWayPoints(): Map<Long, LatLongPosition> {
        return wayPoints
    }

    /**
     * Reset all the mappings.
     */
    fun reset() {
        IdMappingWayPoints = mutableMapOf()
        wayPoints = mutableMapOf()
        newWayPointId = 1
    }

    init {
        reset()
    }
}
