import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.4.21"
}

group = "com.github.rossdanderson.backlight"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val kotlinSerializationVersion = "1.0.1"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinSerializationVersion")
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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "14"
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}
