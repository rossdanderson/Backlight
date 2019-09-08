import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.3.50"

    idea
    java
    application
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("kotlinx-serialization") version kotlinVersion
    id("org.javamodularity.moduleplugin") version "1.5.0"
    id("org.openjfx.javafxplugin") version "0.0.7"
}

group = "com.github.rossdanderson.backlight"
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

val kotlinSerializationVersion = "0.11.1"
val kotlinCoroutinesVersion = "1.3.1"
val javaFXVersion = "12.0.1"
val koinVersion = "2.0.1"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinSerializationVersion")
    implementation("com.fazecast:jSerialComm:2.5.1")
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    implementation("org.koin:koin-core:$koinVersion")
    implementation("io.github.microutils:kotlin-logging:1.6.26")
    implementation("org.slf4j:slf4j-simple:1.7.26")

    testImplementation("org.koin:koin-test:$koinVersion")
}

sourceSets {
    val main by getting
    main.java.srcDirs("$projectDir/src/generated/java")
    main.resources.srcDirs("$projectDir/src/generated/resources")
}

idea {
    module {
        generatedSourceDirs = setOf(file("$projectDir/src/generated/java"))
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "12"
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}
