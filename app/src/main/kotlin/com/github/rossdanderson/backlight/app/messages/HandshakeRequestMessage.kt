@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.messages

import com.github.rossdanderson.backlight.app.messages.Header.HANDSHAKE_REQUEST

object HandshakeRequestMessage : Message {
    override val backingArray: UByteArray = ubyteArrayOf(HANDSHAKE_REQUEST.toUByte())

    override fun toString(): String {
        return "HandshakeRequestMessage()"
    }
}
