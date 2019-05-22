@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight

inline class Color(
    val rgb: UByteArray
) {
    constructor(red: UByte, green: UByte, blue: UByte) : this(ubyteArrayOf(red, green, blue))
}