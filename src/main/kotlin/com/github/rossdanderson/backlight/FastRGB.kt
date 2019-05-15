package com.github.rossdanderson.backlight

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

class FastRGB(image: BufferedImage) {

    private val width: Int = image.width
    private var pixelLength: Int = 0
    private val pixels: IntArray = (image.raster.dataBuffer as DataBufferInt).data

    init {
        pixelLength = 1
    }

    fun getRGB(x: Int, y: Int): Color {
        val pos = y * width + x
        return Color(pixels[pos])
    }
}