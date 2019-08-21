@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.messages

object HeartbeatAckMessage : Message {
    override val backingArray: UByteArray = ubyteArrayOf(heartbeatAck)
}
