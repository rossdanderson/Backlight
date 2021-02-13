import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.4.21-2"

    idea
    java

    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.compose") version "0.3.0-build148"
}

val moduleName by extra("com.github.rossdanderson.backlight.app")

group = "com.github.rossdanderson.backlight"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val kotlinSerializationVersion = "1.0.1"
val kotlinCoroutinesVersion = "1.4.2"
val koinVersion = "2.0.1"
val jSerialCommVersion = "2.6.2"
val kotlinLoggingVersion = "1.8.3"
val slf4jVersion = "2.12.1"

dependencies {
    implementation(project(":serialized"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
    implementation("com.fazecast:jSerialComm:$jSerialCommVersion")
    implementation("org.koin:koin-core:$koinVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$slf4jVersion")
    implementation(compose.desktop.currentOs)

    testImplementation("org.koin:koin-test:$koinVersion")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(15))
    }
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
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses", "-Xopt-in=kotlin.RequiresOptIn")
}


compose.desktop {
    application {
        mainClass = "com.github.rossdanderson.backlight.app.BacklightApplicationKt"
    }
}
