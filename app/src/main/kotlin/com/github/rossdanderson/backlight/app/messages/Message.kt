@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.app.messages

import com.github.rossdanderson.backlight.app.messages.Header.*

enum class Header {
    WRITE_LED,
    WRITE_ALL,
    HEARTBEAT,
    HEARTBEAT_ACK,
    HANDSHAKE_REQUEST,
    HANDSHAKE_RESPONSE,
    PRINT,
    SET_BRIGHTNESS;

    fun toUByte(): UByte = ordinal.toUByte()
}

interface Message {
    val backingArray: UByteArray

    companion object {
        fun from(uByteArray: UByteArray): Message {
            return when (val header = values()[uByteArray[0].toInt()]) {
                WRITE_LED -> WriteLEDMessage(uByteArray)
                WRITE_ALL -> WriteAllMessage(uByteArray)
                HEARTBEAT -> HeartbeatMessage
                HEARTBEAT_ACK -> HeartbeatAckMessage
                HANDSHAKE_REQUEST -> HandshakeRequestMessage
                HANDSHAKE_RESPONSE -> HandshakeResponseMessage(uByteArray)
                PRINT -> PrintMessage(uByteArray)
                SET_BRIGHTNESS -> SetBrightnessMessage(uByteArray)
                else -> throw IllegalArgumentException("Unknown message type $header")
            }
        }
    }
}
