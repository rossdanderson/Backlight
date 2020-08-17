@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.github.rossdanderson.backlight.app.data

class ImageArray(
    val width: Int,
    val height: Int,
    private val byteArray: UByteArray,
) {
    operator fun get(x: Int, y: Int): UColor {
        val offset = offset(x, y)
        return UColor(byteArray.sliceArray(offset..(offset + 3)))
    }

    operator fun get(x: Int, y: Int, color: Color): UByte = byteArray[offset(x, y) + color.offset]

    operator fun set(x: Int, y: Int, color: Color, value: UByte) {
        byteArray[offset(x, y) + color.offset] = value
    }

    private fun offset(x: Int, y: Int) = y * width * 4 + x * 4

    fun copy() = ImageArray(width, height, byteArray.copyOf())
}
