@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.messages

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
            writeLED,
            index,
            red,
            green,
            blue
        )
    )
}