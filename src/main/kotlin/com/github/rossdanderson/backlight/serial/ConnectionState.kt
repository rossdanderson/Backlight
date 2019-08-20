package com.github.rossdanderson.backlight.serial

sealed class ConnectionState {
    data class Connected(val portDescriptor: String, val ledCount: Int) : ConnectionState()
    data class Connecting(val portDescriptor: String) : ConnectionState()
    object Disconnected : ConnectionState()
}