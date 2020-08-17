package com.github.rossdanderson.backlight.app.data

const val redOffset = 2
const val greenOffset = 1
const val blueOffset = 0
const val alphaOffset = 3

enum class Color(val offset: Int) {
    RED(redOffset),
    GREEN(greenOffset),
    BLUE(blueOffset),
    ALPHA(alphaOffset)
}
