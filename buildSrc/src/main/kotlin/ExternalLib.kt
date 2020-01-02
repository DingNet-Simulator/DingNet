data class ExternalLib(val libBaseName: String, val libVersion: String, val libUrl: String) {

    val libFullName = "$libBaseName-$libVersion.jar"

    companion object {
        val mqtt_client_wrapper: ExternalLib = ExternalLib("MqttClientWrapper", "0.2.1",
            "https://github.com/Placu95/MqttClientWrapper/releases/download/v0.2.1/MqttClientWrapper-0.2.1.jar")

        val protelis_over_mqtt: ExternalLib = ExternalLib("ProtelisOverMqtt", "0.1.1",
            "https://github.com/Placu95/ProtelisOverMqtt/releases/download/v0.1.1/ProtelisOverMqtt-0.1.1.jar")
    }
}
