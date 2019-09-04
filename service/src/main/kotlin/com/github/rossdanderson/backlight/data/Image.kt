package com.github.rossdanderson.backlight.data

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

data class Image(
    val height: Int,
    val width: Int,
    private val pixels: IntArray
) {
    constructor(image: BufferedImage) : this(
        image.height,
        image.width,
        (image.raster.dataBuffer as DataBufferInt).data
    )

    operator fun get(x: Int, y: Int): Color = Color(pixels[y * width + x])

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (height != other.height) return false
        if (width != other.width) return false
        if (!pixels.contentEquals(other.pixels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = height
        result = 31 * result + width
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}