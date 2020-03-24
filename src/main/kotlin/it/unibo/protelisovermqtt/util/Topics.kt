package it.unibo.protelisovermqtt.util

import org.protelis.lang.datatype.impl.StringUID

object Topics {

    fun nodeBaseTopic(applicationID: String, deviceID: StringUID): String =
        nodeBaseTopic(applicationID, deviceID.uid)

    fun nodeBaseTopic(applicationID: String, deviceID: String): String =
        "application/$applicationID/node/$deviceID/"

    fun nodeReceiveTopic(applicationID: String, deviceID: StringUID): String =
        "${nodeBaseTopic(applicationID, deviceID)}rx"

    fun nodeReceiveTopic(applicationID: String, deviceID: String): String =
        "${nodeBaseTopic(applicationID, deviceID)}rx"

    fun neighborhoodManagerTopic(applicationID: String): String =
        "application/$applicationID/neighborhoodManager"

    fun neighborhoodTopic(applicationID: String): String =
        "application/$applicationID/neighborhood"

    fun nodeStateTopic(applicationID: String, deviceID: StringUID): String =
        "${nodeBaseTopic(applicationID, deviceID)}state"

    fun nodeStateTopic(applicationID: String, deviceID: String): String =
        "${nodeBaseTopic(applicationID, deviceID)}state"
}
