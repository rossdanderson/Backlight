@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.messages

import com.github.rossdanderson.backlight.app.messages.Header.HEARTBEAT_ACK

object HeartbeatAckMessage : Message {
    override val backingArray: UByteArray = ubyteArrayOf(HEARTBEAT_ACK.toUByte())

    override fun toString(): String {
        return "HeartbeatAckMessage()"
    }
}
