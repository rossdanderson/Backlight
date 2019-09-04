@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.data

inline class UColor(
    val rgb: UByteArray = UByteArray(3)
) {
    constructor(
        red: UByte,
        green: UByte,
        blue: UByte
    ) : this(ubyteArrayOf(red, green, blue))

    val red: UByte
        get() = rgb[0]

    val green: UByte
        get() = rgb[1]

    val blue: UByte
        get() = rgb[2]
}
