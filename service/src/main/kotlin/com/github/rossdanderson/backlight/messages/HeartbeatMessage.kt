@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.messages

import com.github.rossdanderson.backlight.messages.Header.HEARTBEAT

object HeartbeatMessage : Message {
    override val backingArray: UByteArray = ubyteArrayOf(HEARTBEAT.toUByte())

    override fun toString(): String {
        return "HeartbeatMessage()"
    }
}
