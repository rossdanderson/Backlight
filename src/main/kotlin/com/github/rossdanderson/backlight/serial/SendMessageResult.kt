package com.github.rossdanderson.backlight.serial

sealed class SendMessageResult {
    object Success : SendMessageResult()
    data class Failure(val reason: String) : SendMessageResult()
}