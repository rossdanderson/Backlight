@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.messages

/*
Message headers
 */
const val writeLED: UByte = 0u
const val writeAll: UByte = 1u
// TODO handshake, heartbeats, reconnection

interface Message {
    val backingArray: UByteArray
}