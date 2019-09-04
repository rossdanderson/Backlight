plugins {
    java
    id("org.javamodularity.moduleplugin") version "1.5.0"
}

java {
    targetCompatibility = JavaVersion.VERSION_12
    sourceCompatibility = JavaVersion.VERSION_12
}

val compileJava: JavaCompile by tasks
compileJava.options.compilerArgs.addAll(listOf("-h", "$buildDir/headers"))
