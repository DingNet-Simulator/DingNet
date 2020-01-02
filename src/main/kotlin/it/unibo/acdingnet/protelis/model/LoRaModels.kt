package it.unibo.acdingnet.protelis.model

import java.time.LocalTime

data class FrameHeaderApp(
    val sourceAddress: List<Byte>,
    val fCtrl: Int,
    val fCnt: Int,
    val fOpts: List<Byte>
)

data class LoRaWanPacket(
    val senderEUI: Long,
    val designatedReceiverEUI: Long,
    val lowDataRateOptimization: Boolean,
    val codingRate: Double,
    val length: Int,
    val payload: List<Byte>,
    val macCommands: List<String>,
    val header: FrameHeaderApp
)

data class LoRaTransmission(
    val sender: Long,
    val receiver: Long,
    val transmissionPower: Double,
    val xPos: Int,
    val yPos: Int,
    val content: LoRaWanPacket,
    val regionalParameter: String,
    val departureTime: LocalTime,
    val timeOnAir: Double,
    val arrived: Boolean,
    val collided: Boolean
)
