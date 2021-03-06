import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.4.30"

    idea
    java
    application
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("kotlinx-serialization") version kotlinVersion
    id("org.openjfx.javafxplugin") version "0.0.9"
}

val moduleName by extra("com.github.rossdanderson.backlight.app")

group = "com.github.rossdanderson.backlight"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val kotlinSerializationVersion = "1.0.1"
val kotlinCoroutinesVersion = "1.4.2"
val javaFXVersion = "12.0.1"
val koinVersion = "2.0.1"
val jSerialCommVersion = "2.6.2"
val kotlinLoggingVersion = "1.8.3"
val slf4jVersion = "2.12.1"
val tornadofxVersion = "2.0.0-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
    implementation("com.fazecast:jSerialComm:$jSerialCommVersion")
    implementation("no.tornado:tornadofx:$tornadofxVersion")
    implementation("org.koin:koin-core:$koinVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$slf4jVersion")

    testImplementation("org.koin:koin-test:$koinVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(15))
    }
}

javafx {
    version = javaFXVersion
    modules(
        "javafx.controls",
        "javafx.fxml",
        "javafx.media",
        "javafx.web",
        "javafx.swing"
    )
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
    jvmTarget = "14"
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}
