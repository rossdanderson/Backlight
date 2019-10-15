module backlight.app {
    requires com.fazecast.jSerialComm;
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk8;
    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.javafx;
    requires java.desktop;
    requires java.base;
    requires tornadofx;
    requires koin.core;
    requires kotlin.logging;
    requires javafx.swing;
    requires kotlinx.serialization.runtime;
    requires org.apache.logging.log4j;

    exports com.github.rossdanderson.backlight.app;
    exports com.github.rossdanderson.backlight.app.ui;
}
