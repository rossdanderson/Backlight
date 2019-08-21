@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.messages

object HandshakeRequestMessage : Message {
    override val backingArray: UByteArray = ubyteArrayOf(handshakeRequest)
}