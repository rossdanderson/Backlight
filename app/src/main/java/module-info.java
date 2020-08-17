module com.github.rossdanderson.backlight.app {
    requires com.fazecast.jSerialComm;
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk8;
    requires kotlinx.coroutines.javafx;
    requires java.desktop;
    requires java.base;
    requires tornadofx;
    requires koin.core;
    requires kotlin.logging;
    requires javafx.swing;
    requires org.apache.logging.log4j;
    requires kotlinx.coroutines.core.jvm;
    requires kotlinx.serialization.core.jvm;

    exports com.github.rossdanderson.backlight.app;
}
