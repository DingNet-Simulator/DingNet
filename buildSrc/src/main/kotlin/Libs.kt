object Libs {

    const val io_mockk_mockk: String = "io.mockk:mockk:" + Versions.io_mockk_mockk

    const val org_eclipse_paho_client_mqttv3: String = "org.eclipse.paho:org.eclipse.paho.client.mqttv3:" + Versions.org_eclipse_paho_client_mqttv3

    const val com_intellij_forms_rt: String = "com.intellij:forms_rt:" + Versions.com_intellij_forms_rt

    const val org_jxmapviewer_jxmapviewer2 = "org.jxmapviewer:jxmapviewer2:" + Versions.org_jxmapviewer_jxmapviewer2

    const val commons_logging: String = "commons-logging:commons-logging:" + Versions.commons_logging

    const val com_github_spotbugs_gradle_plugin: String =
            "com.github.spotbugs:com.github.spotbugs.gradle.plugin:" +
            Versions.com_github_spotbugs_gradle_plugin

    const val org_jfree_jfreechart: String = "org.jfree:jfreechart:" + Versions.org_jfree_jfreechart

    /**
     * https://spotbugs.github.io/ */
    const val spotbugs: String = "com.github.spotbugs:spotbugs:" + Versions.spotbugs

    /**
     * https://github.com/google/gson */
    const val gson: String = "com.google.code.gson:gson:" + Versions.gson

    /**
     * http://code.google.com/p/simplelatlng */
    const val simplelatlng: String = "com.javadocmd:simplelatlng:" + Versions.simplelatlng

    /**
     * https://github.com/pinterest/ktlint */
    const val ktlint: String = "com.pinterest:ktlint:" + Versions.ktlint

    /**
     * https://github.com/uchuhimo/konf */
    const val konf: String = "com.uchuhimo:konf:" + Versions.konf

     /**
     * http://www.github.com/kotlintest/kotlintest */
    const val kotlintest_runner_junit5: String = "io.kotlintest:kotlintest-runner-junit5:" +
            Versions.kotlintest_runner_junit5

    /**
     * http://commons.apache.org/proper/commons-lang/ */
    const val commons_lang3: String = "org.apache.commons:commons-lang3:" + Versions.commons_lang3

     /**
     * https://junit.org/junit5/ */
    const val junit_jupiter: String = "org.junit.jupiter:junit-jupiter:" +
            Versions.org_junit_jupiter

    /**
     * http://www.protelis.org */
    const val protelis: String = "org.protelis:protelis:" +
            Versions.org_protelis

    val mqtt_client_wrapper: ExternalLib = ExternalLib("MqttClientWrapper", Versions.mqtt_client_wrapper,
        "https://github.com/Placu95/MqttClientWrapper/releases/download/v0.2.1/MqttClientWrapper-0.2.1.jar")

    val protelis_over_mqtt: ExternalLib = ExternalLib("ProtelisOverMqtt", Versions.protelis_over_mqtt,
        "https://github.com/Placu95/ProtelisOverMqtt/releases/download/v0.1.1/ProtelisOverMqtt-0.1.1.jar")
}

data class ExternalLib(val libBaseName: String, val libVersion: String, val libUrl: String) {

    val libFullName = "$libBaseName-$libVersion.jar"
}
