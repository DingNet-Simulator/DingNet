package it.unibo.acdingnet.protelis.model

import it.unibo.acdingnet.protelis.model.sensorconverter.DefaultConverter
import it.unibo.acdingnet.protelis.model.sensorconverter.GPSConverter
import it.unibo.acdingnet.protelis.model.sensorconverter.SensorConverter

enum class SensorType(val length: Int = 1, val levels: List<Pair<Int, Int>> = emptyList(), val converter: SensorConverter<*> = DefaultConverter()) {
    GPS(8, converter = GPSConverter()),
    PM10(levels = listOf(Pair(0, 25), Pair(25, 50), Pair(50, 90), Pair(90, 180), Pair(180, 255))),
    NO2(2, listOf(Pair(0, 50), Pair(50, 100), Pair(100, 200), Pair(200, 400), Pair(400, 600))),
    IAQ(1, listOf(Pair(0, 25), Pair(25, 50), Pair(50, 75), Pair(75, 100), Pair(100, 125)));

    inline fun <reified T> consumeAndConvert(data: MutableList<Byte>): T = converter.convert(length, data) as T
}
