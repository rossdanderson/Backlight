package com.github.rossdanderson.backlight.serial

sealed class ConnectResult {
    object Success : ConnectResult()
    object Failure : ConnectResult()
}