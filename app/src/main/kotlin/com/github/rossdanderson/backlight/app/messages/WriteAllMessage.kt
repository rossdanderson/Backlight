@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.app.messages

import com.github.rossdanderson.backlight.app.data.UColor
import com.github.rossdanderson.backlight.app.messages.Header.WRITE_ALL

inline class WriteAllMessage(
    override val backingArray: UByteArray,
) : Message {

    companion object {
        operator fun invoke(
            colors: Iterable<UColor>,
        ): WriteAllMessage {
            val uByteArray = UByteArray(colors.count() * 3 + 1)
            uByteArray[0] = WRITE_ALL.toUByte()
            colors.reversed().forEachIndexed { ledIndex, color ->
                uByteArray[(ledIndex * 3) + 1 + 0] = color.red
                uByteArray[(ledIndex * 3) + 1 + 1] = color.green
                uByteArray[(ledIndex * 3) + 1 + 2] = color.blue
            }
            return WriteAllMessage(uByteArray)
        }
    }

    val colors: List<UColor>
        get() = backingArray.drop(1).windowed(3, 3) { UColor(it[0], it[1], it[2]) }

    override fun toString(): String {
        return "WriteAllMessage(colors='$colors')"
    }
}
