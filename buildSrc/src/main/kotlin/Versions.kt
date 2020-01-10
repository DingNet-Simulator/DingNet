import kotlin.String

/**
 * Find which updates are available by running
 *     `$ ./gradlew buildSrcVersions`
 * This will only update the comments.
 *
 * YOU are responsible for updating manually the dependency version. */
object Versions {
    const val com_github_johnrengelman_shadow_gradle_plugin: String = "5.2.0" 

    const val gson: String = "2.8.5" // available: "2.8.6"

    const val forms_rt: String = "7.0.3" 

    const val simplelatlng: String = "1.3.1" 

    const val ktlint: String = "0.33.0" // available: "0.36.0"

    const val konf: String = "0.13.3" // available: "0.22.1"

    const val commons_logging: String = "1.2" 

    const val de_fayard_buildsrcversions_gradle_plugin: String = "0.3.2" // available: "0.7.0"

    const val jpx: String = "1.4.0" // available: "1.6.1"

    const val kotlintest_runner_junit5: String = "3.3.2" // available: "3.4.2"

    const val mockk: String = "1.9.1" // available: "1.9.3"

    const val moquette_broker: String = "0.12.1" 

    const val commons_lang3: String = "3.9" 

    const val org_eclipse_paho_client_mqttv3: String = "1.2.1" // available: "1.2.2"

    const val org_gradle_kotlin_kotlin_dsl_gradle_plugin: String = "1.2.6" // available: "1.3.3"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String = "1.3.61" 

    const val kotlin_reflect: String = "1.3.21" // available: "1.3.61"

    const val kotlin_sam_with_receiver: String = "1.3.61" 

    const val kotlin_scripting_compiler_embeddable: String = "1.3.61" 

    const val kotlin_stdlib_jdk8: String = "1.3.21" // available: "1.3.61"

    const val jfreechart: String = "1.5.0" 

    const val org_jlleitschuh_gradle_ktlint_gradle_plugin: String = "8.1.0" // available: "9.1.1"

    const val junit_jupiter: String = "5.5.2" 

    const val jxmapviewer2: String = "2.4" // available: "2.5"

    const val protelis: String = "13.0.3" // available: "13.1.1"

    /**
     *
     *   To update Gradle, edit the wrapper file at path:
     *      ./gradle/wrapper/gradle-wrapper.properties
     */
    object Gradle {
        const val runningVersion: String = "5.4.1"

        const val currentVersion: String = "6.0.1"

        const val nightlyVersion: String = "6.2-20200109230020+0000"

        const val releaseCandidate: String = "6.1-rc-2"
    }
}
