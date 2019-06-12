module backlight.main {
    requires com.fazecast.jSerialComm;
    requires kotlin.stdlib;
    requires kotlin.stdlib.jdk8;
    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.javafx;
    requires java.desktop;
    requires java.base;
    requires tornadofx;
    exports com.github.rossdanderson.backlight;
}