import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.3.40"
    id("org.javamodularity.moduleplugin") version "1.5.0"
    id("org.openjfx.javafxplugin") version "0.0.7"
}

group = "com.github.rossdanderson"
version = "1.0-SNAPSHOT"

val moduleName: String by project

application {
    mainClassName = "$moduleName/com.github.rossdanderson.backlight.BacklightApplicationKt"
}

java {
    targetCompatibility = JavaVersion.VERSION_12
    sourceCompatibility = JavaVersion.VERSION_12
}

javafx {
    version = "12.0.1"
    modules(
        "javafx.controls",
        "javafx.fxml",
        "javafx.media",
        "javafx.web",
        "javafx.swing"
    )
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val kotlinCoroutinesVersion = "1.3.0-M2"
val javaFXVersion = "12.0.1"
val koinVersion = "2.0.1"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$kotlinCoroutinesVersion")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
    implementation("com.fazecast:jSerialComm:2.5.1")
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    implementation("org.koin:koin-core:$koinVersion")
    implementation("io.github.microutils:kotlin-logging:1.6.26")
//    implementation("io.projectreactor:reactor-core:3.2.10.RELEASE")
    implementation("org.slf4j:slf4j-simple:1.7.26")

    testImplementation("org.koin:koin-test:$koinVersion")
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "12"
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}