package it.unibo.acdingnet.protelis.util

object Const {

    const val APPLICATION_ID = "1"
    const val NEIGHBORHOOD_RANGE = 1000.0
    const val MOTT_ADDRESS = "tcp://test.mosquitto.org:1883"
    const val MQTT_CLIENT_ID = "testFenomeno1995-app"

    object ProtelisEnv {
        const val IAQLEVEL = "iaqLevel"
        const val CURRENT_TEMP = "currentTemp"
        const val DESIRED_TEMP = "desiredTemp"
        const val MAX_TEMP = "maxTemp"
        const val PM10 = "pm10"
    }
}
