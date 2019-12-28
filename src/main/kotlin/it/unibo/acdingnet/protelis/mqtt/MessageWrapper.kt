package it.unibo.acdingnet.protelis.mqtt

import it.unibo.acdingnet.protelis.model.LoRaTransmission
import it.unibo.mqttclientwrapper.api.MqttMessageType


data class LoRaTransmissionWrapper(val transmission: LoRaTransmission): MqttMessageType
