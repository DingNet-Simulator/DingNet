plugins {
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
    application
    java
    kotlin("jvm") version Versions.org_jetbrains_kotlin
    id("com.github.johnrengelman.shadow") version Versions.com_github_johnrengelman_shadow
    checkstyle
}

group = "KULeuven"
version = "1.2.1"

repositories {
    mavenCentral()
}

dependencies {
    //dependencies for DingNetSimulator
    implementation(kotlin("stdlib-jdk8"))
    implementation(Libs.commons_logging)
    implementation(Libs.org_jfree_jfreechart)
    implementation(Libs.org_jxmapviewer_jxmapviewer2)
    implementation(Libs.com_intellij_forms_rt)
    implementation(files("${projectDir.path}/lib/AnnotationsDoclets.jar"))
    implementation(Libs.gson)
    implementation(files(Util.downloadLibFromUrl(Libs.mqtt_client_wrapper)))
    //dependencies for protelis application
    implementation(Libs.protelis)
    implementation(files(Util.downloadLibFromUrl(Libs.protelis_over_mqtt)))
    implementation(Libs.simplelatlng)
    implementation(Libs.commons_lang3)
    implementation(Libs.konf)
    implementation(Libs.org_eclipse_paho_client_mqttv3)
    //dependencies for test
    testImplementation(Libs.junit_jupiter)
    testImplementation(Libs.kotlintest_runner_junit5)
    testImplementation(Libs.io_mockk_mockk)
}

application {
    mainClassName = "gui.MainGUI"
}

tasks.shadowJar.configure {
    // removes "-all" from the jar name
    archiveClassifier.set("")
    exclude ("**/*.kotlin_metadata")
    exclude ("**/*.kotlin_module")
    //use this exclude only for library Jar not in runnable Jar
    //exclude ("**/*.kotlin_builtins")
}

tasks.withType<Test> {
    useJUnitPlatform {
    }
    maxParallelForks = 1
}

tasks.withType<Checkstyle> {
    ignoreFailures = false
    maxWarnings = 0
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

java {                                      
    sourceCompatibility = JavaVersion.VERSION_11
}
