import java.net.URL

plugins {
    application
    java
    kotlin("jvm") version "1.3.61"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    checkstyle
}

group = "KULeuven"
version = "1.2.1"

repositories {
    mavenCentral()
}

fun downloadLibFromUrl(libName: String , libUrl: String, libSaveDir: String = "${projectDir.absolutePath}/build/libs") {
    val folder = File(libSaveDir)
    if (!folder.exists()) {
        folder.mkdirs()
    }
    val file = File("$libSaveDir/$libName")
    if (!file.exists()) {
        URL(libUrl).openStream().readAllBytes().also { file.appendBytes(it) }
    }
    dependencies.add("implementation", files(file.absolutePath))
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("commons-logging:commons-logging:1.2")
    implementation("org.jfree:jfreechart:1.5.0")
    implementation("org.jxmapviewer:jxmapviewer2:2.4")
    implementation("com.intellij:forms_rt:7.0.3")
    implementation(files("${projectDir.path}/lib/AnnotationsDoclets.jar"))
    implementation("com.google.code.gson:gson:2.8.5")
    downloadLibFromUrl(extra["MqttClientWrapperLib"].toString(), extra["MqttClientWrapperUrl"].toString())

    implementation("org.protelis:protelis:${extra["protelisVersion"].toString()}")
    downloadLibFromUrl(extra["ProtelisOverMqttLib"].toString(), extra["ProtelisOverMqttUrl"].toString())
    implementation("com.javadocmd:simplelatlng:${extra["simplelatlng"].toString()}")
    implementation("org.apache.commons:commons-lang3:${extra["commons-lang3"].toString()}")
    implementation("com.uchuhimo:konf:0.13.3")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:${extra["kotlinTestVersion"].toString()}")
    testImplementation("io.mockk:mockk:1.9.1")
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

val test by tasks.getting(Test::class) {
    useJUnitPlatform {
    }
    maxParallelForks = 1
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

java {                                      
    sourceCompatibility = JavaVersion.VERSION_11
}
