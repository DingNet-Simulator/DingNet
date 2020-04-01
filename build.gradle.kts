plugins {
    id("de.fayard.buildSrcVersions") version Versions.de_fayard_buildsrcversions_gradle_plugin
    application
    java
    kotlin("jvm") version Versions.org_jetbrains_kotlin_jvm_gradle_plugin
    id("com.github.johnrengelman.shadow") version
        Versions.com_github_johnrengelman_shadow_gradle_plugin
    checkstyle
    id("org.jlleitschuh.gradle.ktlint") version
        Versions.org_jlleitschuh_gradle_ktlint_gradle_plugin
}

group = "KULeuven"
version = "1.2.1"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    // dependencies for DingNetSimulator
    implementation(Libs.kotlin_stdlib_jdk8)
    implementation(Libs.commons_logging)
    implementation(Libs.commons_cli)
    implementation(Libs.jfreechart)
    implementation(Libs.jxmapviewer2)
    implementation(Libs.forms_rt)
    implementation(Libs.commons_math3)
    implementation(Libs.gson)
    implementation(Libs.moquette_broker)
    implementation(Libs.konf)
    implementation(files(Util.downloadLibFromUrl(ExternalLib.mqtt_client_wrapper)))
    // dependencies for protelis application
    implementation(Libs.protelis)
    implementation(Libs.simplelatlng)
    implementation(Libs.commons_lang3)
    // dependencies for test
    testImplementation(Libs.junit_jupiter)
    testImplementation(Libs.kotlintest_runner_junit5)
    testImplementation(Libs.mockk)
}

application {
    mainClassName = "Simulator"
}

tasks.shadowJar.configure {
    // removes "-all" from the jar name
    archiveClassifier.set("")
    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_module")
}

tasks.withType<Test> {
    useJUnitPlatform()
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
