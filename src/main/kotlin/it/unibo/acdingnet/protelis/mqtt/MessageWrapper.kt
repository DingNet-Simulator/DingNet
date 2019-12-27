package it.unibo.acdingnet.protelis.mqtt

import iot.mqtt.MqttMessageType
import it.unibo.acdingnet.protelis.model.LoRaTransmission


data class LoRaTransmissionWrapper(val transmission: LoRaTransmission): MqttMessageType
