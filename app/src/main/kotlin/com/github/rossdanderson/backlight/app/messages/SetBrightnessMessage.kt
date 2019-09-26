@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.app.messages

import com.github.rossdanderson.backlight.app.messages.Header.SET_BRIGHTNESS

inline class SetBrightnessMessage(
    override val backingArray: UByteArray
) : Message {
    constructor(
        brightness: UByte
    ) : this(
        ubyteArrayOf(
            SET_BRIGHTNESS.toUByte(),
            brightness
        )
    )

    val brightness: UByte
        get() = backingArray[1]

    override fun toString(): String {
        return "SetBrightnessMessage(brightness='$brightness')"
    }
}
