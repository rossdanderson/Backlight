@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.messages

import com.github.rossdanderson.backlight.app.messages.Header.HEARTBEAT

object HeartbeatMessage : Message {
    override val backingArray: UByteArray = ubyteArrayOf(HEARTBEAT.toUByte())

    override fun toString(): String {
        return "HeartbeatMessage()"
    }
}
