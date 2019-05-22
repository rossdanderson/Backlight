package com.github.rossdanderson.backlight

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt

class FastRGB(image: BufferedImage) {

    private val width: Int = image.width
    private val pixels: IntArray = (image.raster.dataBuffer as DataBufferInt).data

    operator fun get(x: Int, y: Int): Color = Color(pixels[y * width + x])
}