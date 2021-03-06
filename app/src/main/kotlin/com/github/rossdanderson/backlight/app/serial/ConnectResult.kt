package com.github.rossdanderson.backlight.app.serial

sealed class ConnectResult {
    object Success : ConnectResult()
    data class Failure(val reason: String) : ConnectResult()
}
