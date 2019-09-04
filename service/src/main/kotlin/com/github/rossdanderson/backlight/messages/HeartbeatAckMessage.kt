@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.messages

import com.github.rossdanderson.backlight.messages.Header.HEARTBEAT_ACK

object HeartbeatAckMessage : Message {
    override val backingArray: UByteArray = ubyteArrayOf(HEARTBEAT_ACK.toUByte())

    override fun toString(): String {
        return "HeartbeatAckMessage()"
    }
}
