@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package com.github.rossdanderson.backlight.messages

import com.github.rossdanderson.backlight.Color

inline class WriteAllMessage(
    override val backingArray: UByteArray
) : Message {

    companion object {
        fun from(
            ledCount: Int,
            colors: Iterable<Color>
        ): WriteAllMessage {
            val uByteArray = UByteArray(ledCount * 3 + 1)
            uByteArray[0] = writeAll
            colors.forEachIndexed { ledIndex, color ->
                color.rgb.forEachIndexed { colorIndex, j ->
                    uByteArray[(ledIndex * 3) + colorIndex] = j
                }
            }
            return WriteAllMessage(uByteArray)
        }
    }
}