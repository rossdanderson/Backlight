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
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val kotlinCoroutinesVersion = "1.2.1"
val javaFXVersion = "12.0.1"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$kotlinCoroutinesVersion")
    implementation("com.fazecast:jSerialComm:2.5.1")
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
//    implementation("org.openjfx:javafx-swing:$javaFXVersion")
//    implementation("org.openjfx:javafx-web:$javaFXVersion")
//    implementation("org.openjfx:javafx-fxml:$javaFXVersion")

}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "12"
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}