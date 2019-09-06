@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.app.messages

import com.github.rossdanderson.backlight.app.data.UColor
import com.github.rossdanderson.backlight.app.messages.Header.WRITE_LED

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

    val index: UByte
        get() = backingArray[1]

    val color: UColor
        get() = UColor(backingArray[2], backingArray[3], backingArray[4])

    override fun toString(): String {
        return "WriteLEDMessage(index='$index', color='$color')"
    }
}
