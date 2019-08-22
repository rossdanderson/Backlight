@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.messages

import com.github.rossdanderson.backlight.messages.Header.WRITE_LED

inline class WriteLEDMessage(
    override val backingArray: UByteArray
) : Message {
    constructor(
        index: UByte,
        red: UByte,
        green: UByte,
        blue: UByte
    ) : this(
        ubyteArrayOf(
            WRITE_LED.toUByte(),
            index,
            red,
            green,
            blue
        )
    )
}
