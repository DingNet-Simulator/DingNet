package it.unibo.acdingnet.protelis.util

object Const {

    const val APPLICATION_ID = "1"
    const val NEIGHBORHOOD_RANGE = 1500.0
    const val MOTT_ADDRESS = "tcp://test.mosquitto.org:1883"
    const val MQTT_CLIENT_ID = "testFenomeno1995-app"

    object ProtelisEnv {
        const val IAQLEVEL_KEY = "iaqLevel"
        const val SOURCE_KEY = ""
        const val DESTINATION_KEY = ""
        const val NODE_TYPE = "nodeType"
        const val SENSOR_TYPE = "sensor"
        const val BUILDING_TYPE = "building"
        const val CURRENT_TEMP = "currentTemp"
        const val DESIRED_TEMP = "desiredTemp"
        const val PM10 = "pm10"
    }
}