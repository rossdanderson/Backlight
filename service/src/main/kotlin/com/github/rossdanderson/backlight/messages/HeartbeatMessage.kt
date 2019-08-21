@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.messages

object HeartbeatMessage : Message {
    override val backingArray: UByteArray = ubyteArrayOf(heartbeat)
}