@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.messages

inline class HandshakeResponseMessage(
    override val backingArray: UByteArray
) : Message {

    companion object {
        private const val numLedsOffset = 1
    }

    val ledCount: Int
        get() = backingArray[numLedsOffset].toInt()

    override fun toString(): String {
        return "HandshakeResponseMessage(ledCount=$ledCount)"
    }
}
