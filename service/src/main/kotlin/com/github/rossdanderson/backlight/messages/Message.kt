@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.messages

/*
Message headers
 */
const val writeLED: UByte = 0u
const val writeAll: UByte = 1u
const val heartbeat: UByte = 2u
const val heartbeatAck: UByte = 3u
const val handshakeRequest: UByte = 4u
const val handshakeResponse: UByte = 5u

interface Message {
    val backingArray: UByteArray

    companion object {
        fun from(uByteArray: UByteArray): Message {
            return when (val header = uByteArray[0]) {
                writeLED -> WriteLEDMessage(uByteArray)
                writeAll -> WriteAllMessage(uByteArray)
                heartbeat -> HeartbeatMessage
                heartbeatAck -> HeartbeatAckMessage
                handshakeRequest -> HandshakeRequestMessage
                handshakeResponse -> HandshakeResponseMessage(uByteArray)
                else -> throw IllegalArgumentException("Unknown message type $header")
            }
        }
    }
}