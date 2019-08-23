import org.gradle.api.JavaVersion.VERSION_12
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    id("org.javamodularity.moduleplugin") version "1.5.0"
    id("org.openjfx.javafxplugin") version "0.0.7"
    id("org.beryx.jlink") version "2.10.3"
}

group = "com.github.rossdanderson"
version = "1.0-SNAPSHOT"

val moduleName: String by project

application {
    mainClassName = "$moduleName/com.github.rossdanderson.backlight.BacklightApplicationKt"
}

java {
    targetCompatibility = VERSION_12
    sourceCompatibility = VERSION_12
}

javafx {
    version = "12.0.1"
    modules("javafx.controls")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
    implementation("com.fazecast:jSerialComm:2.5.1")
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "12"
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}