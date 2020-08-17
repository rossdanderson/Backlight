@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.data

inline class UColor(
    private val bgr: UByteArray = UByteArray(3)
) {
    constructor(
        red: UByte,
        green: UByte,
        blue: UByte
    ) : this(ubyteArrayOf(blue, green, red))

    val red: UByte
        get() = bgr[redOffset]

    val green: UByte
        get() = bgr[greenOffset]

    val blue: UByte
        get() = bgr[blueOffset]
}
