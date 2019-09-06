@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.app.messages

import java.nio.charset.Charset

inline class PrintMessage(
    override val backingArray: UByteArray
) : Message {

    val contents: String
        get() {
            val bytes = backingArray.toByteArray()
            return String(bytes, 1, bytes.size - 1, Charset.forName("ASCII"))
        }

    override fun toString(): String {
        return "PrintMessage(contents='$contents')"
    }
}
