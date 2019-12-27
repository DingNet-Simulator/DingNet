package it.unibo.acdingnet.protelis.model

enum class MessageType(val code: Byte) {
    SENSOR_VALUE(0),
    REQUEST_PATH(1),
    REQUEST_UPDATE_PATH(2),
    KEEPALIVE(3);
}